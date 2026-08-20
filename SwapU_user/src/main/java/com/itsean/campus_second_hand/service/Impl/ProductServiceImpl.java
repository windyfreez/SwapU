package com.itsean.campus_second_hand.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.constant.StringConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.dto.ProductDTO;
import com.itsean.campus_second_hand.dto.ProductListPageQueryDTO;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.entity.User;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.campus_second_hand.service.ProductService;
import com.itsean.campus_second_hand.service.ProductViewService;
import com.itsean.campus_second_hand.utils.SimpleRandomSortUtil;
import com.itsean.campus_second_hand.vo.ProductDetailVO;
import com.itsean.campus_second_hand.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.itsean.campus_second_hand.constant.MessageConstant.CANT_SEARCH_HOT_PRODUCTS;
import static com.itsean.campus_second_hand.constant.StringConstant.HOT_PRODUCTS_REDIS_KEY;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    @Resource
    private ProductViewService productViewService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 添加商品
     * @param productDTO
     * @return
     */
    @Override
    public ProductVO addProduct(ProductDTO productDTO) {
        Product product = new Product();
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productDTO,product);

        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setStatus(NumberConstant.PRODUCT_STATUS_CHECKED);
        product.setUserId(BaseContext.getCurrentId());
        product.setViewCount(NumberConstant.DEFAULT_VIEW_COUNT);

        productMapper.addProduct(product);
        BeanUtils.copyProperties(product,productVO);
        return productVO;
    }

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

        ProductDetailVO productDetailVO = new ProductDetailVO();
        BeanUtils.copyProperties(product,productDetailVO);

        //补全卖家信息属性
        Long userId = product.getUserId();
        ProductDetailVO.SellerInfo sellerInfo = new ProductDetailVO.SellerInfo();
        User user = userMapper.findById(userId);

        //属性拷贝卖家信息
        BeanUtils.copyProperties(user,sellerInfo);
        productDetailVO.setSellerInfo(sellerInfo);

        //补全商品成色描述属性信息
        Integer productCondition = product.getProductCondition();
        switch (productCondition) {
            case 1:
                productDetailVO.setProductConditionDesc(StringConstant.PRODUCT_CONDITION_NEW);
                break;
            case 2:
                productDetailVO.setProductConditionDesc(StringConstant.PRODUCT_CONDITION_ALMOST_NEW);
                break;
            case 3:
                productDetailVO.setProductConditionDesc(StringConstant.PRODUCT_CONDITION_HAS_USED);
                break;
            case 4:
                productDetailVO.setProductConditionDesc(StringConstant.PRODUCT_CONDITION_STRONG_USED);
                break;
            default:
                productDetailVO.setProductConditionDesc(StringConstant.PRODUCT_CONDITION_NOT_EXIST);
                break;
        }

        //补全商品状态描述属性信息
        Integer status = product.getStatus();
        switch (status) {
            case 0:
                productDetailVO.setStatusDesc(StringConstant.PRODUCT_STATUS_CHECKED_DESC);
                break;
            case 1:
                productDetailVO.setStatusDesc(StringConstant.PRODUCT_STATUS_SELLING_DESC);
                break;
            case 2:
                productDetailVO.setStatusDesc(StringConstant.PRODUCT_STATUS_SOLD_OUT_DESC);
                break;
            case 3:
                productDetailVO.setStatusDesc(StringConstant.PRODUCT_STATUS_DOWN_DESC);
                break;
        }


        return productDetailVO;

    }

    /**
     * 修改商品信息
     * @param productDTO
     */
    @Override
    public void update(ProductDTO productDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTO,product);
        //补全基础属性
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);
    }

    /**
     * 下架商品
     * @param id
     */
    @Override
    public void takedown(Long id) {
        Product product = productMapper.getProductById(id);
        //修改商品状态
        product.setStatus(NumberConstant.PRODUCT_STATUS_DOWN);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);
    }

    /**
     * 删除商品
     * @param id
     */
    @Override
    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    /**
     * 分页查询当前用户的商品
      * @param productListPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(ProductListPageQueryDTO productListPageQueryDTO) {
        productListPageQueryDTO.setUserId(BaseContext.getCurrentId());
        PageHelper.startPage(productListPageQueryDTO.getPage(),productListPageQueryDTO.getPageSize());

        List<Product> products = productMapper.pageQuery(productListPageQueryDTO);

        //分页查询时只需要展示封面图而非所有图片
        products.forEach(product -> {
            List<String> images = product.getImages();
            String firstImage = images.get(0);
            product.setImages(Collections.singletonList(firstImage));
        });

        Page<Product> page = (Page<Product>) products;

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 分页查询所有商品
     * @param productListPageQueryDTO
     * @return
     */
    @Override
    public PageResult allPageQuery(ProductListPageQueryDTO productListPageQueryDTO) {
        log.info("当前用户id：{}",BaseContext.getCurrentId());
        productListPageQueryDTO.setUserId(BaseContext.getCurrentId());
        log.info("设置后的userId：{}", productListPageQueryDTO.getUserId());

        //判断是否使用默认排序
        boolean useDefaultSort = (productListPageQueryDTO.getSort() == null ||
                productListPageQueryDTO.getSort().isEmpty());

        PageHelper.startPage(productListPageQueryDTO.getPage(),productListPageQueryDTO.getPageSize());

        List<Product> products = productMapper.allPageQuery(productListPageQueryDTO);

        //如果请求中未指定排序顺序，则对数据库中查到的数据进行随机排序
        if (useDefaultSort && products != null && products.size() > 10) {
            products = SimpleRandomSortUtil.weightedRandomSort(products);
        }

        //分页查询时只需要展示封面图而非所有图片
        products.forEach(product -> {
            List<String> images = product.getImages();
            String firstImage = images.get(0);
            product.setImages(Collections.singletonList(firstImage));
        });

        //强转类型封装为Page类型
        Page<Product> page = (Page<Product>) products;

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 上架商品
     * @param id
     */
    @Override
    public void takeup(Long id) {
        Product product = productMapper.getProductById(id);
        //修改商品状态
        product.setStatus(NumberConstant.PRODUCT_STATUS_UP);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.update(product);
    }

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

    /**
     * 推荐算法
     * @param limit
     * @return
     */
    @Override
    public Result recommend(Integer limit) {
        int size = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        Long userId = BaseContext.getCurrentId();
        Map<Object, Object> profile = userId == null ? Collections.emptyMap() :
                stringRedisTemplate.opsForHash().entries(StringConstant.USER_PROFILE_REDIS_KEY_PREFIX + userId);

        List<Long> categoryIds = profile.entrySet().stream()
                .filter(e -> !"_updatedAt".equals(String.valueOf(e.getKey())))
                .sorted((a, b) -> Double.compare(parseScore(b.getValue()), parseScore(a.getValue())))
                .limit(5).map(e -> Long.valueOf(String.valueOf(e.getKey()))).collect(Collectors.toList());

        Map<Long, Product> products = new LinkedHashMap<>();
        Map<Long, Double> scores = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            for (Product p : productMapper.selectRecommendProducts(categoryIds, size * 3, userId)) {
                products.putIfAbsent(p.getId(), p);
                scores.merge(p.getId(), 0.6, Double::sum);
            }
        }
        for (Product p : productMapper.selectHotProducts(size * 2)) {
            if (userId == null || !userId.equals(p.getUserId())) {
                products.putIfAbsent(p.getId(), p);
                scores.merge(p.getId(), categoryIds.isEmpty() ? 0.7 : 0.3, Double::sum);
            }
        }
        for (Product p : productMapper.selectNewProducts(size * 2, userId)) {
            products.putIfAbsent(p.getId(), p);
            scores.merge(p.getId(), categoryIds.isEmpty() ? 0.3 : 0.2, Double::sum);
        }
        List<Product> result = products.values().stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .sorted(Comparator.comparingDouble((Product p) -> scores.getOrDefault(p.getId(), 0D)).reversed()
                        .thenComparing(Product::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(size).collect(Collectors.toList());
        return Result.success(result);
    }

    private double parseScore(Object value) {
        try {
            return Double.parseDouble(String.valueOf(value));
        }
        catch (Exception e) {
            return 0D;
        }
    }

}
