package com.itsean.swapu_admin.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.itsean.common.captcha.CaptchaScene;
import com.itsean.common.captcha.CaptchaService;
import com.itsean.common.captcha.CaptchaVO;
import com.itsean.pojo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端图形验证码接口
 * <p>
 * 管理端只有登录入口，没有注册，因此场景固定为 admin-login。
 */
@RestController
@RequestMapping("/admin/captcha")
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
     * 获取管理端登录验证码
     * @param request 请求对象，用于取客户端 IP 做生成限流
     * @return 验证码标识与 base64 图片
     */
    @GetMapping("/image")
    @ApiOperation("获取管理端登录验证码")
    public Result<CaptchaVO> image(HttpServletRequest request) {
        return Result.success(captchaService.generate(CaptchaScene.ADMIN_LOGIN, resolveIp(request)));
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
