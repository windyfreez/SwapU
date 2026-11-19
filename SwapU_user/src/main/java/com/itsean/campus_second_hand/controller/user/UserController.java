package com.itsean.campus_second_hand.controller.user;

import com.itsean.common.constant.JwtClaimsConstant;
import com.itsean.pojo.dto.PasswordDTO;
import com.itsean.pojo.dto.PayPasswordDTO;
import com.itsean.pojo.dto.UserDTO;
import com.itsean.pojo.dto.UserLoginDTO;
import com.itsean.pojo.dto.UserRegisterDTO;
import com.itsean.pojo.entity.User;
import com.itsean.pojo.Result;
import com.itsean.common.properties.JwtProperties;
import com.itsean.campus_second_hand.service.UserService;
import com.itsean.common.utils.JwtUtil;
import com.itsean.pojo.vo.UserLoginVO;
import com.itsean.pojo.vo.UserRegisterVO;
import com.itsean.pojo.vo.UserProfileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户模块接口")
public class   UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 新用户注册
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    @ApiOperation("新用户注册")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO){
        log.info("用户注册：{}",userRegisterDTO);
        UserRegisterVO userRegisterVO = userService.register(userRegisterDTO);
        return Result.success(userRegisterVO);
    }

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        User user = userService.login(userLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }

    /**
     * 获取当前用户详细信息
     * @return
     */
    @GetMapping("/info")
    @ApiOperation("获取当前用户详细信息")
    public Result<User> getInfo(){
        log.info("获取当前用户详细信息");
        User user = userService.getInfo();
        return Result.success(user);
    }

    /**
     * 修改用户信息
     * @param userDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改用户信息")
    public Result update(@RequestBody UserDTO userDTO){
        log.info("修改用户信息：{}",userDTO);
        userService.update(userDTO);
        return Result.success("用户信息修改成功");
    }

    /**
     * 修改密码
     * @param passwordDTO
     * @return
     */
    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result updatePassword(@RequestBody PasswordDTO passwordDTO){
        log.info("修改密码：{}",passwordDTO);
        userService.updatePassword(passwordDTO);
        return Result.success("密码修改成功");
    }

    /**
     * 根据id获取用户信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id获取用户信息")
    public Result<User> getUserById(@PathVariable Long id){
    	log.info("根据id获取用户信息：{}",id);
    	User user = userService.getUserById(id);
    	return Result.success(user);
    }

    /**
     * 根据id获取某个用户的公开主页信息
     * @param id
     * @return
     */
    @GetMapping("/{id}/profile")
    @ApiOperation("根据id获取某个用户的公开主页信息")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long id){
        log.info("根据id获取用户公开主页信息：{}",id);
        UserProfileVO userProfileVO = userService.getUserProfile(id);
        return Result.success(userProfileVO);
    }

    /**
     * 设置/重置支付密码（需用登录密码验证身份）
     * @param payPasswordDTO 登录密码 + 新支付密码
     * @return 设置结果
     */
    @PostMapping("/pay-password")
    @ApiOperation("设置或重置支付密码")
    public Result setPayPassword(@Valid @RequestBody PayPasswordDTO payPasswordDTO){
        log.info("设置支付密码");
        userService.setPayPassword(payPasswordDTO);
        return Result.success("支付密码设置成功");
    }

    /**
     * 查询当前用户是否已设置支付密码
     * @return hasPayPassword 标记
     */
    @GetMapping("/pay-password/status")
    @ApiOperation("查询是否已设置支付密码")
    public Result<Map<String, Boolean>> payPasswordStatus(){
        Map<String, Boolean> data = new HashMap<>();
        data.put("hasPayPassword", userService.hasPayPassword());
        return Result.success(data);
    }

}
