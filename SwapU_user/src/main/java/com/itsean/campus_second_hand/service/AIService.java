package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.dto.AiChatDTO;
import com.itsean.campus_second_hand.vo.AiChatVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 客服服务
 */
public interface AIService {

    /**
     * 向大模型发起一次客服问答，等全部生成完成后一次性返回
     * @param aiChatDTO 提问参数
     * @return AI 回答
     */
    AiChatVO chat(AiChatDTO aiChatDTO);

    /**
     * 向大模型发起一次客服问答，边生成边通过 SSE 推送给前端
     * <p>
     * 校验与限流在返回前同步完成，失败会直接抛业务异常走全局异常处理；
     * 真正的模型调用在独立线程池里执行，避免占用 Tomcat 请求线程。
     * @param aiChatDTO 提问参数
     * @return SSE 事件流（delta 增量 / done 结束 / error 失败）
     */
    SseEmitter chatStream(AiChatDTO aiChatDTO);
}
