package com.itsean.campus_second_hand.controller.user;

import cn.hutool.core.util.StrUtil;
import com.itsean.common.captcha.CaptchaScene;
import com.itsean.common.captcha.CaptchaService;
import com.itsean.common.captcha.CaptchaVO;
import com.itsean.pojo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 图形验证码接口
 * <p>
 * 登录与注册页在提交前先调用这里拿图和 key，提交时把 key 与用户输入的字符一起回传。
 */
@RestController
@RequestMapping("/captcha")
@Slf4j
@Api(tags = "图形验证码接口")
public class CaptchaController {

    /**
     * X-Forwarded-For 里第一个地址才是客户端真实 IP
     */
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    @Autowired
    private CaptchaService captchaService;

    /**
     * 获取图形验证码
     * @param scene 使用场景：user-login（默认）/ user-register
     * @param request 请求对象，用于取客户端 IP 做生成限流
     * @return 验证码标识与 base64 图片
     */
    @GetMapping("/image")
    @ApiOperation("获取图形验证码")
    public Result<CaptchaVO> image(
            @ApiParam(value = "使用场景", example = "user-login")
            @RequestParam(defaultValue = "user-login") String scene,
            HttpServletRequest request) {
        CaptchaScene captchaScene = CaptchaScene.of(scene);
        return Result.success(captchaService.generate(captchaScene, resolveIp(request)));
    }

    /**
     * 取客户端 IP：优先 X-Forwarded-For 的第一段，否则用直连地址
     * @param request 请求对象
     * @return 客户端 IP
     */
    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
        if (StrUtil.isNotBlank(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
