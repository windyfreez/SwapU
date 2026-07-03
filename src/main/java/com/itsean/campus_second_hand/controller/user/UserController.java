package com.itsean.campus_second_hand.controller.user;

import com.itsean.campus_second_hand.constant.JwtClaimsConstant;
import com.itsean.campus_second_hand.dto.PasswordDTO;
import com.itsean.campus_second_hand.dto.UserDTO;
import com.itsean.campus_second_hand.dto.UserLoginDTO;
import com.itsean.campus_second_hand.dto.UserRegisterDTO;
import com.itsean.campus_second_hand.entity.User;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.properties.JwtProperties;
import com.itsean.campus_second_hand.service.UserService;
import com.itsean.campus_second_hand.utils.JwtUtil;
import com.itsean.campus_second_hand.vo.UserLoginVO;
import com.itsean.campus_second_hand.vo.UserRegisterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


}
