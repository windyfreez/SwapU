package com.itsean.campus_second_hand.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itsean.campus_second_hand.entity.Category;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.mapper.CategoryMapper;
import com.itsean.campus_second_hand.properties.DeepSeekProperties;
import com.itsean.campus_second_hand.properties.ReRankProperties;
import com.itsean.campus_second_hand.service.ReRankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 推荐商品语义重排序实现：用 hutool 的 HttpRequest 调用 DeepSeek，
 * 让大模型按用户兴趣对召回候选集重排，任何异常都静默回退为召回分排序
 */
@Service
@Slf4j
public class ReRankServiceImpl implements ReRankService {

    /**
     * DeepSeek 对话补全接口路径
     */
    private static final String CHAT_COMPLETION_PATH = "/chat/completions";

    /**
     * 重排序人设：只做排序，只回 JSON
     */
    private static final String SYSTEM_PROMPT = "你是二手交易平台的推荐重排序助手。"
            + "请根据用户兴趣对候选商品做相关性排序，把最符合用户兴趣的商品排在前面。"
            + "只输出一个 JSON 对象，格式为 {\"ranking\": [按相关性从高到低排列的商品id]}，"
            + "不要输出任何解释、代码块或其他内容。";

    /**
     * 单个商品标题截断长度，避免标题过长撑爆请求体
     */
    private static final int TITLE_MAX_LENGTH = 30;

    /**
     * 单个商品描述截断长度
     */
    private static final int DESC_MAX_LENGTH = 60;

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    @Autowired
    private ReRankProperties reRankProperties;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 使用大模型对候选商品做语义重排序，任何异常都回退为原顺序
     * @param candidates 待重排候选商品（已按召回分粗排）
     * @param interestCategoryIds 用户兴趣分类 id（按兴趣度降序），为空则不做重排
     * @return 重排后的商品列表
     */
    @Override
    public List<Product> reRank(List<Product> candidates, List<Long> interestCategoryIds) {
        // 关闭开关、候选太少或没有兴趣画像时，重排没有意义，直接原样返回
        if (!reRankProperties.isEnabled()
                || CollUtil.isEmpty(candidates)
                || candidates.size() <= 1
                || CollUtil.isEmpty(interestCategoryIds)) {
            return candidates;
        }

        try {
            Map<Long, String> categoryNames = loadCategoryNames();
            String requestBody = buildRequestBody(candidates, interestCategoryIds, categoryNames);
            JSONObject responseJson = doRequest(requestBody);
            List<Long> ranking = parseRanking(responseJson);
            return applyRanking(candidates, ranking);
        } catch (Exception e) {
            // 重排序是锦上添花，任何失败都不能影响推荐主流程，回退为召回分排序
            log.warn("大模型语义重排序失败，回退为召回分排序：{}", e.getMessage());
            return candidates;
        }
    }

    /**
     * 加载全部分类 id -> 名称映射，供 prompt 与候选商品描述使用
     * @return 分类映射
     */
    private Map<Long, String> loadCategoryNames() {
        List<Category> categories = categoryMapper.list();
        Map<Long, String> names = new LinkedHashMap<>();
        if (CollUtil.isEmpty(categories)) {
            return names;
        }
        for (Category category : categories) {
            names.putIfAbsent(category.getId(), category.getName());
        }
        return names;
    }

    /**
     * 拼装重排序请求体：兴趣分类 + 候选商品清单，要求模型回 JSON 数组
     * @param candidates 候选商品
     * @param interestCategoryIds 兴趣分类 id（降序）
     * @param categoryNames 分类映射
     * @return 请求体 JSON 字符串
     */
    private String buildRequestBody(List<Product> candidates, List<Long> interestCategoryIds,
                                    Map<Long, String> categoryNames) {
        // 兴趣分类描述，按兴趣度降序拼接，供模型感知用户偏好
        List<String> interests = new ArrayList<>();
        for (Long categoryId : interestCategoryIds) {
            interests.add(categoryNames.getOrDefault(categoryId, "分类" + categoryId));
        }

        // 候选商品只取前 maxCandidates 个，控制请求体大小与费用
        int max = reRankProperties.getMaxCandidates();
        List<Product> top = candidates.size() > max ? candidates.subList(0, max) : candidates;

        StringBuilder sb = new StringBuilder();
        sb.append("用户感兴趣的商品分类（按兴趣度从高到低）：").append(String.join("、", interests)).append("。\n\n");
        sb.append("候选商品列表：\n");
        for (Product p : top) {
            sb.append("id=").append(p.getId());
            sb.append("，标题=").append(truncate(p.getTitle(), TITLE_MAX_LENGTH));
            sb.append("，分类=").append(categoryNames.getOrDefault(p.getCategoryId(), "未知"));
            sb.append("，价格=").append(p.getPrice());
            String desc = truncate(p.getDescription(), DESC_MAX_LENGTH);
            if (StrUtil.isNotBlank(desc)) {
                sb.append("，描述=").append(desc);
            }
            sb.append("\n");
        }
        sb.append("\n请按相关性从高到低输出这些商品的 id 排序，必须包含所有候选商品 id。");

        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().set("role", "system").set("content", SYSTEM_PROMPT));
        messages.add(new JSONObject().set("role", "user").set("content", sb.toString()));

        JSONObject body = new JSONObject()
                .set("model", deepSeekProperties.getModel())
                .set("messages", messages)
                .set("stream", false)
                .set("max_tokens", deepSeekProperties.getMaxTokens())
                // 强制 JSON 输出，便于解析出排序结果
                .set("response_format", new JSONObject().set("type", "json_object"));

        // 重排序不需要思考过程，显式关闭以加快速度、节省费用
        body.set("thinking", new JSONObject().set("type", "disabled"));

        return body.toString();
    }

    /**
     * 发起 HTTP 请求并取回 JSON，失败抛异常交由上层回退
     * @param requestBody 请求体 JSON 字符串
     * @return 响应 JSON
     */
    private JSONObject doRequest(String requestBody) {
        String apiKey = deepSeekProperties.getApiKey();
        if (StrUtil.isBlank(apiKey)) {
            log.warn("未配置 DeepSeek api-key，跳过语义重排序");
            throw new RuntimeException("DeepSeek api-key 未配置");
        }

        String url = deepSeekProperties.getBaseUrl() + CHAT_COMPLETION_PATH;
        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey.trim())
                // hutool 默认按 UTF-8 编码请求体，第二个参数是 Content-Type 而不是字符集
                .body(requestBody, "application/json")
                .setConnectionTimeout(reRankProperties.getConnectTimeout())
                .setReadTimeout(reRankProperties.getReadTimeout())
                .execute()) {

            int status = response.getStatus();
            String body = response.body();
            if (status != 200) {
                log.warn("DeepSeek 重排序接口返回异常，status: {}，body: {}", status, body);
                throw new RuntimeException("DeepSeek 重排序返回异常 status=" + status);
            }
            return JSONUtil.parseObj(body);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("调用 DeepSeek 重排序失败，url: {}", url);
            throw new RuntimeException("调用 DeepSeek 重排序失败", e);
        }
    }

    /**
     * 从响应中解析出模型给出的商品 id 排序
     * @param responseJson 响应 JSON
     * @return 按相关性降序的商品 id 列表
     */
    private List<Long> parseRanking(JSONObject responseJson) {
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("重排序响应缺少 choices");
        }

        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        String content = message == null ? null : message.getStr("content");
        if (StrUtil.isBlank(content)) {
            throw new RuntimeException("重排序响应内容为空");
        }

        List<Long> ranking = extractRanking(content.trim());
        if (ranking.isEmpty()) {
            throw new RuntimeException("重排序结果为空");
        }
        return ranking;
    }

    /**
     * 从模型返回文本中提取 id 列表，兼容对象与裸数组两种格式
     * @param content 模型返回文本
     * @return id 列表
     */
    private List<Long> extractRanking(String content) {
        // 优先按 {"ranking": [...]} 解析
        try {
            JSONObject obj = JSONUtil.parseObj(content);
            JSONArray arr = obj.getJSONArray("ranking");
            if (arr != null && !arr.isEmpty()) {
                return toLongList(arr);
            }
        } catch (Exception ignore) {
            // 不是对象格式，继续尝试裸数组
        }

        // 兜底：尝试把文本整体当作数组解析
        try {
            return toLongList(JSONUtil.parseArray(content));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 把 JSON 数组元素逐个转成 Long，跳过无法识别的元素
     * @param arr JSON 数组
     * @return Long 列表
     */
    private List<Long> toLongList(JSONArray arr) {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            try {
                result.add(Long.valueOf(String.valueOf(arr.get(i))));
            } catch (Exception ignore) {
                // 忽略无法识别的 id
            }
        }
        return result;
    }

    /**
     * 按模型给出的顺序重排候选商品，漏掉的候选按原顺序补到末尾
     * @param candidates 原候选商品
     * @param ranking 模型给出的 id 排序
     * @return 重排后的商品列表
     */
    private List<Product> applyRanking(List<Product> candidates, List<Long> ranking) {
        Map<Long, Product> byId = new LinkedHashMap<>();
        for (Product p : candidates) {
            byId.putIfAbsent(p.getId(), p);
        }

        List<Product> reordered = new ArrayList<>();
        for (Long id : ranking) {
            Product p = byId.remove(id);
            if (p != null) {
                reordered.add(p);
            }
        }

        // 模型漏掉或幻觉出的 id 之外的候选，按原顺序补在末尾，避免丢商品
        reordered.addAll(byId.values());
        return reordered;
    }

    /**
     * 按长度截断文本，超长部分用省略号代替
     * @param text 原文
     * @param max 最大长度
     * @return 截断后的文本
     */
    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max) + "…";
    }
}
