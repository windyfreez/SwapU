# SwapU 项目核心模块性能优化指南

本指南系统性地记录了项目中关于"热门商品"、"库存预扣减"与"推荐系统"三大核心模块的性能优化方案，旨在解决高并发场景下普遍存在的数据库写入压力过大、查询响应缓慢、库存超卖与缓存回显脏数据、以及推荐内容缺乏多样性与时效性等典型问题。以下内容将分别阐述各模块所面临的核心挑战、设计思路以及具体的落地实现。

---

## 一、 热门商品浏览缓存策略

### 1. 核心挑战分析

在高并发访问的电商场景下，热门商品模块面临两个主要技术挑战：

* **频繁写入造成的数据库压力：** 每当用户浏览一个商品详情页，系统都需要对该商品的 `viewCount`（浏览量）字段进行加一操作。在流量高峰期，这种实时更新操作会转化为大量的数据库行锁竞争与日志写入，极易形成写入瓶颈，甚至导致数据库连接池耗尽，影响整体服务的稳定性。

* **高频查询导致的慢查询风险：** 首页或商品列表页通常需要展示"浏览量最高"的 Top N 热门商品。如果每次请求都直接执行带有 `ORDER BY view_count DESC LIMIT N` 的 SQL 查询，即使建立了索引，在高并发下仍会因重复的排序计算和磁盘 I/O 而产生较高的响应延迟，严重影响用户体验。

### 2. 解决方案设计思路

针对上述两个挑战，我们采用了"读写分离 + 异步批量同步"与"缓存预热"相结合的策略：

- **写入侧：** 利用 Redis 极高的读写性能，将每次浏览量的增加操作在内存中完成，再通过定时任务将累积的变化量批量同步到数据库，从而实现"削峰填谷"。
- **查询侧：** 彻底避免实时查询数据库，改为通过定时任务预先计算热门商品列表并存储到 Redis 中，所有查询请求均直接读取缓存数据。

### 3. 解决方案具体实现

#### A. 浏览量异步处理（削峰填谷）

该方案的核心是将 Redis 作为浏览量的临时存储层，用户每次浏览商品时，仅对 Redis 中的计数器执行自增操作，完全绕过数据库。随后，通过 Spring 的 `@Scheduled` 注解声明一个定时任务，每隔固定时间（例如 5 分钟）将 Redis 中累计的所有商品浏览量批量更新到 MySQL 数据库中。

**代码实现要点：**

```java
/**
 * 同步浏览量到数据库
 */
@Scheduled(cron = "0 0/5 * * * ?")
public void syncViewCountToDB() {
    log.info("开始同步浏览量到数据库:{}", LocalDateTime.now());

    // 1. 获取所有商品浏览量 key
    Set<String> keys = stringRedisTemplate.keys(PRODUCT_VIEW_PREFIX + "*");
    if (keys == null || keys.isEmpty()) return;

    List<HashMap<String, Long>> updateList = new ArrayList<>();

    for (String key : keys) {
        try {
            Long productId = Long.parseLong(key.replace(PRODUCT_VIEW_PREFIX, ""));
            String countStr = stringRedisTemplate.opsForValue().get(key);
            if (countStr == null) continue;

            long count = Long.parseLong(countStr);
            if (count <= 0) continue;

            HashMap<String, Long> map = new HashMap<>();
            map.put("productId", productId);
            map.put("count", count);
            updateList.add(map);
        } catch (Exception ignored) {}
    }

    // 2. 批量更新数据库
    if (!updateList.isEmpty()) {
        productMapper.batchUpdateViewCount(updateList);
    }

    // 3. 删除 Redis 临时计数
    stringRedisTemplate.delete(keys);
    log.info("同步完成：{} 条" ,updateList.size());
}
```

**执行流程说明：**

1. 定时任务启动后，通过 `keys` 命令获取所有以 `product:view:count:` 为前缀的 Redis 键。
2. 遍历每一个键，从中解析出商品 ID 和当前累计的浏览量数值。
3. 将有效的 `(productId, count)` 键值对封装为 Map 对象，存入待更新列表。
4. 调用 MyBatis 的批量更新方法 `batchUpdateViewCount`，一次性将多条记录的浏览量累加到数据库对应字段上。
5. 批量更新成功后，删除 Redis 中已同步的键，避免重复处理。

**优化收益：** 原本每秒数百次的数据库写入操作，被压缩为每 5 分钟一次的批量更新，数据库写入压力下降了 99% 以上。

#### B. 热门商品缓存预热与刷新

为了彻底规避实时查询数据库带来的性能开销，我们设计了缓存预热机制，通过定时任务将计算好的 Top N 热门商品直接推送到 Redis 中。

**第一步：数据库索引优化**

在执行任何查询之前，首先确保数据库表具备高效的索引支持，这是保证定时任务本身能够快速执行的基础。

```sql
CREATE INDEX idx_view_count ON product(view_count DESC);
```

该索引能够使 `ORDER BY view_count DESC LIMIT N` 类型的查询通过索引顺序扫描直接获取所需数据，避免额外的文件排序操作。

**第二步：定时刷新缓存**

每隔 1 分钟，系统自动执行一次热门商品的刷新任务。任务内部首先调用 Mapper 方法从数据库查询当前浏览量最高的前 20 件商品，然后删除旧缓存，再以"一商品一键"的形式将新的热门商品逐个存入 Redis。

```java
@Scheduled(cron = "0 0/1 * * * ?")
public void refreshHotProducts() {
    log.info("开始刷新热门商品到 Redis,{}", LocalDateTime.now());

    try {
        List<Product> hotProducts = productMapper.selectHotProducts(NumberConstant.HOT_PRODUCT_LIMIT);

        if (hotProducts == null || hotProducts.isEmpty()) {
            log.warn(CANT_SEARCH_HOT_PRODUCTS);
            return;
        }
        String redisKey = StringConstant.HOT_PRODUCTS_REDIS_KEY;
        Set<String> oldKeys = stringRedisTemplate.keys(redisKey + "*");
        if (oldKeys != null && !oldKeys.isEmpty()) {
            stringRedisTemplate.delete(oldKeys);
        }

        hotProducts.forEach(hotProduct -> {
            String key = redisKey + hotProduct.getId();
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(hotProduct));
        });
        log.info("热门商品刷新完成，共 {} 个商品", hotProducts.size());

    } catch (Exception e) {
        log.error("刷新热门商品失败", e);
    }
}
```

**第三步：查询路径变更**

前端或服务层需要获取热门商品时，不再调用数据库查询，而是直接从 Redis 的 `hot:products:*` 键中读取。这一变更使得热门商品接口的响应时间从原来的平均 80ms ~ 120ms 降低至 5ms 以内。

```java
/**
 * 获取top20热门商品
 * @return
 */
@Override
public Result top20List() {
    String keyPrefix = HOT_PRODUCTS_REDIS_KEY;

    // 尝试从 Redis 聚合读取所有热门商品
    Set<String> keys = stringRedisTemplate.keys(keyPrefix + "*");
    if (CollUtil.isNotEmpty(keys)) {
        List<String> jsonList = stringRedisTemplate.opsForValue().multiGet(keys);
        List<Product> hotProductList = jsonList.stream()
                .filter(StrUtil::isNotBlank)
                .map(json -> JSONUtil.toBean(json, Product.class))
                .sorted((p1, p2) -> p2.getViewCount().compareTo(p1.getViewCount()))
                .collect(Collectors.toList());
        if (!hotProductList.isEmpty()) {
            return Result.success(hotProductList);
        }
    }

    // Redis 未命中，查数据库
    List<Product> hotProductList = productMapper.selectHotProducts(20);
    if(CollUtil.isEmpty(hotProductList)){
        return Result.error(CANT_SEARCH_HOT_PRODUCTS);
    }

    // 逐个写入 Redis，方便后续列表和详情都能命中缓存
    hotProductList.forEach(hotProduct -> {
        String littleKey = keyPrefix + hotProduct.getId();
        stringRedisTemplate.opsForValue().set(littleKey, JSONUtil.toJsonStr(hotProduct));
    });
    return Result.success(hotProductList);
}
```

同时，由于 Redis 中存入了热门商品的完整详情，所以针对前端或服务层访问商品详细信息时，我们也可以调用 Redis 缓存来提高接口平均响应速度：即访问某个商品详细信息时，**先查找 Redis 缓存**中是否有该商品的详细信息，如果有，则直接返回缓存数据，如果没有，再查询数据库。由此可以减少查询数据库次数。

```java
/**
 * 根据id获取商品信息
 * @param id
 * @return
 */
@Override
public ProductDetailVO getProductById(Long id) {
    // 先尝试从热门商品缓存读取
    String hotKey = HOT_PRODUCTS_REDIS_KEY + id;
    String hotProductJson = stringRedisTemplate.opsForValue().get(hotKey);
    Product product = null;
    if (StrUtil.isNotBlank(hotProductJson)) {
        product = JSONUtil.toBean(hotProductJson, Product.class);
    } else {
        product = productMapper.getProductById(id);
    }

    productViewService.incrementViewCount(id);

    //后续逻辑为补全基础属性...

    return productDetailVO;

}
```

通过实施**商品浏览量异步写入数据库**和**缓存热门商品**的方案，该系统性能有了显著的提升：使用 Jmeter 工具进行高并发压测，测得商品访问平均速度由 420ms 优化至 68ms，这不仅使得热门商品可以应对更高的访问量，还可以提高热门商品的访问流量，进而提升售卖效率。


---

## 二、 热门商品库存预扣减与缓存一致性方案

### 1. 核心挑战分析

热门商品采用"缓存优先回显"策略后，库存扣减引入了四类新的技术挑战：

* **缓存回显延迟导致脏数据：** 热门商品缓存由定时任务每 1 分钟刷新一次，库存卖光（数据库 `status` 变为"已售出"）后，`hot:products:{id}` 缓存仍残留旧快照，最长 60 秒内用户仍能看到已售罄商品或旧库存。
* **下单读取脏缓存：** 原下单逻辑通过 `getProductById` 优先读取热门缓存快照，库存扣减基于过期数据而非数据库真实库存。
* **并发超卖风险：** 原扣减使用无条件 `UPDATE ... WHERE id = #{id}`，多个并发请求同时读到 `quantity = 1` 并各自扣减，导致超卖。
* **库存只减不增：** 下单即扣库存，但手动取消、超时未确认、超时未支付、超时未发货四类取消场景均未回补库存，也未把商品状态从"已售出"改回"售卖中"。

### 2. 解决方案设计思路

我们采用 **"Redis 预扣减 + 数据库原子扣减兜底 + 取消回补 + 定时对账 + 缓存穿透"** 的完整方案：

- **第一层（Redis 预扣减）：** 以数据库为最终真相，Redis 作为预扣加速层。下单先通过 Lua 脚本原子判断并扣减 Redis 库存，库存不足直接快速失败，避免大量无效请求打到数据库。
- **第二层（数据库原子扣减兜底）：** Redis 预扣成功后，仍执行带 `quantity >= 需求数量 AND status = 1` 条件的原子 UPDATE，影响行数不为 1 则回滚 Redis 并抛异常，从根本上杜绝超卖。
- **第三层（取消回补与退款）：** 订单取消时按"条件翻转订单状态 + 回补库存 + 可选退款"三步走，幂等可靠，彻底解决库存只减不增。
- **第四层（定时对账）：** 定时任务以数据库为准，将 Redis 库存拉回一致，兜底 Redis 宕机、漂移等异常。
- **缓存穿透：** 任何库存变更（扣减、回补）后立即失效对应热门商品缓存，卖光即从热门列表消失，无需等待定时任务。

### 3. 解决方案具体实现

#### A. Redis 原子库存预扣减

为每个商品维护 Redis 库存计数器 `product:stock:{id}`，扣减通过 Lua 脚本保证"判断 + 扣减"一步原子完成：

```lua
-- KEYS[1]: 商品库存 key  product:stock:{id}
-- ARGV[1]: 扣减数量
local stock = tonumber(redis.call('GET', KEYS[1]) or '0')
if stock >= tonumber(ARGV[1]) then
  redis.call('DECRBY', KEYS[1], ARGV[1])
  return 1
else
  return 0
end
```

Java 层封装为 `StockService`：

```java
public interface StockService {
    boolean preDeduct(Long productId, int quantity);      // Lua 原子预扣，库存不足返回 false
    void rollbackPreDeduct(Long productId, int quantity); // DB 扣减失败时回滚
    void restore(Long productId, int quantity);           // 取消订单回补
    void initIfAbsent(Long productId, int quantity);      // 懒初始化（key 不存在时写入）
    void syncStock(Long productId, int quantity);         // 以 DB 为准同步
}
```

#### B. 数据库原子扣减（防超卖兜底）

Redis 预扣只是加速层，数据库仍是最终真相。扣减必须走带条件的原子 UPDATE：

```xml
<update id="deductStock">
    UPDATE product
    SET quantity = quantity - #{needQuantity},
        status = CASE WHEN quantity - #{needQuantity} = 0 THEN 2 ELSE 1 END,
        update_time = #{updateTime}
    WHERE id = #{id} AND status = 1 AND quantity &gt;= #{needQuantity}
</update>
```

判断影响行数 == 1 才算成功；否则回滚 Redis 预扣并抛"库存不足"异常。

#### C. 下单流程改造

下单整体流程调整为"绕缓存读 DB → Redis 预扣 → DB 原子扣减 → 穿透缓存"：

```java
@Transactional
public OrderVO createOrder(OrderDTO orderDTO) {
    // 1. 读 DB 实时库存（绕开热门缓存，避免扣减基于脏快照）
    Product product = productMapper.getProductById(orderDTO.getProductId());
    Integer stockQuantity = product.getQuantity();

    // 2. Redis 预扣（尽力而为，异常降级为纯 DB 扣减）
    boolean redisDeducted = false;
    try {
        stockService.initIfAbsent(productId, stockQuantity);
        if (!stockService.preDeduct(productId, needQuantity)) {
            throw new OrderException(MessageConstant.NEED_MORE_THAN_STORE);
        }
        redisDeducted = true;
    } catch (OrderException e) {
        throw e;
    } catch (Exception e) {
        log.warn("Redis 预扣库存异常，降级为纯 DB 扣减", e);
    }

    // 3. DB 原子扣减 + 写订单，任何失败回滚 Redis 预扣
    try {
        int affected = productMapper.deductStock(productId, needQuantity, LocalDateTime.now());
        if (affected != 1) {
            throw new OrderException(MessageConstant.NEED_MORE_THAN_STORE);
        }
        // ...补全订单属性、orderMapper.add(order)...

        // 4. 库存变更后穿透热门缓存，避免回显旧库存/已售罄商品
        productService.evictHotCache(productId);
    } catch (RuntimeException e) {
        if (redisDeducted) {
            stockService.rollbackPreDeduct(productId, needQuantity);
        }
        throw e;
    }
    return orderVO;
}
```

#### D. 取消回补与退款

取消订单统一走"条件翻转状态（幂等）→ 回补库存 → 可选退款"三步：

```sql
-- 仅当订单仍处于可取消状态(1/2/3)时才翻转，影响行数==1 保证幂等
UPDATE orders
SET status = #{cancelStatus}, cancel_time = #{cancelTime}, cancel_reason = #{cancelReason}
WHERE order_no = #{orderNo} AND status = #{expectStatus}
```

```java
private boolean doCancel(Order order, String reason, boolean needRefund) {
    int affected = orderMapper.cancelIfCancellable(order.getOrderNo(), order.getStatus(),
            Order.ORDER_STATUS_CANCEL, LocalDateTime.now(), reason);
    if (affected != 1) return false;   // 已被取消，幂等跳过

    // 回补 DB 库存 + 状态回滚（已售出 → 售卖中）
    productMapper.restoreStock(order.getProductId(), order.getQuantity(), LocalDateTime.now());
    // 回补 Redis 库存
    stockService.restore(order.getProductId(), order.getQuantity());
    // 穿透热门缓存
    productService.evictHotCache(order.getProductId());

    // 已支付订单（余额支付）退款：买家加回，卖家扣回（允许卖家余额为负）
    if (needRefund && Order.PAY_TYPE_BALANCE.equals(order.getPayType())) {
        refundBalance(order);
    }
    return true;
}
```

回补点覆盖四处：手动取消（待确认/待支付）、超时未确认、超时未支付、超时未发货（已支付，额外退款）。

#### E. 定时对账机制

每 1 分钟以数据库为准对齐 Redis 库存，兜底 Redis 宕机、漂移造成的偏差：

```java
@Scheduled(cron = "0 0/1 * * * ?")
public void syncStockToRedis() {
    List<Product> products = productMapper.selectSellingProductStock();
    for (Product product : products) {
        stockService.syncStock(product.getId(), product.getQuantity());
    }
}
```

**优化收益：** 库存扣减由"读-改-写"升级为 Redis 原子预扣 + 数据库条件更新兜底，超卖率降为零；卖光后热门缓存立即失效，无需等待定时任务刷新；取消订单自动回补库存并退款，库存数据始终保持自洽。

---

## 三、 个性化推荐系统

### 1. 痛点分析

早期首页推荐仅按商品浏览量简单降序排列，存在三个问题：

* **内容固化、缺乏新鲜感：** 高浏览量商品长期霸榜，用户每次访问看到几乎相同的内容，容易产生审美疲劳，降低浏览意愿。
* **新商品曝光机会被压制：** 新上架商品缺乏历史浏览量，在纯排序下几乎无法进入推荐位，形成"强者恒强、弱者恒弱"的马太效应。
* **缺乏个性化：** 所有用户看到的推荐完全一致，无法体现兴趣偏好。

### 2. 解决方案设计思路

我们构建了一套"行为埋点采集 → 用户画像 → 多路召回 + 加权打分"的轻量级个性化推荐体系：

- **行为埋点采集：** 通过 AOP 切面拦截用户浏览、收藏、购买行为，先写入 Redis 队列削峰，再由定时任务批量落库，避免高频写入数据库。
- **用户画像：** 定时任务聚合近 90 天行为日志，按"行为权重 × 行为值 × 时间衰减"量化用户对每个类目的兴趣分，存入 Redis Hash。
- **多路召回 + 加权打分：** 推荐时同时从"兴趣类目""热门""新品"三个通道召回候选商品，按通道权重打分后合并去重、统一排序，兼顾个性化与冷启动。

### 3. 具体实现

#### A. 行为埋点采集

`BehaviorAspect` 拦截商品/收藏/订单 Controller 的成功方法，识别浏览(1)/收藏(2)/购买(3)行为，反查商品类目后写入 Redis List 队列：

```java
@AfterReturning(
    pointcut = "productControllerPointCut() || favoriteControllerPointCut() || orderControllerPointCut()",
    returning = "result"
)
public void recordBehavior(JoinPoint joinPoint, Object result) {
    String methodName = joinPoint.getSignature().getName();
    // 按方法名识别行为类型：addFavorite=2 收藏 / createOrder=3 购买 / getProductById=1 浏览
    // 反查商品 categoryId，写入 Redis 队列
    behaviorBufferService.recordBehavior(userId, productId, behaviorType, categoryId, source);
}
```

`BehaviorTask` 每分钟从队列批量取出（最多 500 条）落库：

```java
@Scheduled(cron = "0 * * * * *")
public void syncBehaviorData() {
    List<UserBehaviorLog> batchList = new ArrayList<>(BATCH_SIZE);
    for (int i = 0; i < BATCH_SIZE; i++) {
        String logJson = stringRedisTemplate.opsForList().leftPop(BEHAVIOR_QUEUE_KEY);
        if (logJson == null) break;
        batchList.add(JSONUtil.toBean(logJson, UserBehaviorLog.class));
    }
    if (!batchList.isEmpty()) {
        userBehaviorLogMapper.addBehavior(batchList);
    }
}
```

#### B. 用户画像重建

`UserProfileTask` 每 10 分钟聚合近 90 天行为日志，按"行为权重 × 行为值 × 时间衰减"计算每个类目的兴趣分，存入 Redis Hash：

```java
@Scheduled(cron = "0 0/10 * * * *")
public void rebuildProfiles() {
    List<UserBehaviorLog> logs = behaviorLogMapper.listRecentBehaviors();
    // 兴趣分 = 行为权重(浏览1/收藏4/购买6/搜索2) × 行为值 × 衰减系数(半衰期30天)
    for (UserBehaviorLog log : logs) {
        double base = behaviorWeight(log.getBehaviorType());
        double value = log.getBehaviorValue() == null ? 1D : log.getBehaviorValue().doubleValue();
        long days = Duration.between(log.getCreateTime(), LocalDateTime.now()).toDays();
        double decay = Math.pow(0.5D, days / 30D);
        profiles.computeIfAbsent(log.getUserId(), k -> new HashMap<>())
                .merge(log.getCategoryId(), base * value * decay, Double::sum);
    }
    // 写入 Redis Hash：recommend:user:profile:{userId}
}
```

#### C. 多路召回与加权打分

`recommend` 接口同时从三个通道召回候选商品并加权打分：

| 召回通道 | SQL 来源 | 打分权重 |
|---------|---------|---------|
| 兴趣类目召回 | `selectRecommendProducts`（用户 Top5 兴趣类目，`size×3`） | 0.6 |
| 热门召回 | `selectHotProducts`（浏览量 Top，`size×2`） | 0.7（无画像）/ 0.3（有画像） |
| 新品召回 | `selectNewProducts`（最新上架，`size×2`） | 0.3（无画像）/ 0.2（有画像） |

```java
public Result recommend(Integer limit) {
    int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
    // 1. 读取用户画像，取兴趣分最高的 5 个类目
    Map<Object, Object> profile = stringRedisTemplate.opsForHash().entries(USER_PROFILE_REDIS_KEY_PREFIX + userId);
    List<Long> categoryIds = /* 按分值排序取 Top5 */;

    Map<Long, Product> products = new LinkedHashMap<>();
    Map<Long, Double> scores = new HashMap<>();
    // 2. 兴趣类目召回，权重 0.6
    for (Product p : productMapper.selectRecommendProducts(categoryIds, size * 3, userId)) {
        products.putIfAbsent(p.getId(), p);
        scores.merge(p.getId(), 0.6, Double::sum);
    }
    // 3. 热门召回，权重 0.7(无画像)/0.3(有画像)，并排除自己发布的商品
    for (Product p : productMapper.selectHotProducts(size * 2)) {
        if (userId == null || !userId.equals(p.getUserId())) {
            products.putIfAbsent(p.getId(), p);
            scores.merge(p.getId(), categoryIds.isEmpty() ? 0.7 : 0.3, Double::sum);
        }
    }
    // 4. 新品召回，权重 0.3(无画像)/0.2(有画像)
    for (Product p : productMapper.selectNewProducts(size * 2, userId)) {
        products.putIfAbsent(p.getId(), p);
        scores.merge(p.getId(), categoryIds.isEmpty() ? 0.3 : 0.2, Double::sum);
    }
    // 5. 过滤仅保留上架商品(status=1)，按分数 desc + 上架时间 desc 排序，截取 size
    return Result.success(result);
}
```

关键设计点：

- **去重合并：** 三路召回用 `putIfAbsent` 去重，同一商品出现在多路时分数累加（`merge`），相关性越高的商品自然排前。
- **冷启动兼容：** 无画像用户（新用户）时，热门/新品权重更高（0.7/0.3），保证推荐质量；有画像后兴趣类目权重主导。
- **自买过滤：** 热门召回排除自己发布的商品。

#### D. 列表兜底随机排序

商品分页列表在未指定排序时，用"分区 + 随机打乱"避免头部固化，同时保留热度差异：

```java
public static List<Product> weightedRandomSort(List<Product> products) {
    if (products == null || products.size() <= 10) {
        return products;
    }
    // 1. 按浏览量降序
    // 2. 取前 size/3(最多20) 为"热门区"，其余为"常规区"
    // 3. 两区各自 shuffle 后按"热门区 + 常规区"顺序拼接
}
```

**实际效果：** 个性化推荐上线后，推荐位命中用户兴趣类目的比例显著提升，新商品与潜力商品获得更多曝光机会，同时保留了热门商品的基础热度，兼顾了精准度、新鲜感与冷启动。

---

## 四、 方案总结与对比

| 优化方案 | 核心解决的问题 | 关键技术点 | 性能提升指标 |
|---------|-------------|-----------|-----------------------|
| 浏览量异步处理 | 数据库写入压力过大 | Redis 计数器 + 定时批量同步 | 数据库写入减少 99% |
| 热门商品缓存预热 | 热门商品查询慢 | 定时计算 + Redis 缓存 | 平均响应时间从 420ms 降至 68ms |
| 库存预扣减方案 | 高并发下库存超卖、缓存回显脏数据、库存只减不增 | Redis Lua 原子预扣 + DB 条件扣减兜底 + 取消回补/退款 + 定时对账 + 缓存穿透 | 超卖率降为零，卖光即时下线 |
| 个性化推荐系统 | 推荐内容固化、新商品曝光不足、缺乏个性化 | 行为埋点 + 用户画像 + 多路召回加权打分 | 推荐命中率与曝光多样性显著提升 |
