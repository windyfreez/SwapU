package com.itsean.campus_second_hand.service;

import com.itsean.pojo.dto.PasswordDTO;
import com.itsean.pojo.dto.PayPasswordDTO;
import com.itsean.pojo.dto.UserDTO;
import com.itsean.pojo.dto.UserLoginDTO;
import com.itsean.pojo.dto.UserRegisterDTO;
import com.itsean.pojo.entity.User;
import com.itsean.pojo.vo.UserRegisterVO;
import com.itsean.pojo.vo.UserProfileVO;

public interface UserService {

    /**
     * 新用户注册
     * @param userRegisterDTO
     * @return
     */
    UserRegisterVO register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);

    /**
     * 获取用户信息
     * @return
     */
    User getInfo();

    /**
     * 修改用户信息
     * @param userDTO
     */
    void update(UserDTO userDTO);

    /**
     *  修改用户密码
     * @param passwordDTO
     */
    void updatePassword(PasswordDTO passwordDTO);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User getUserById(Long id);

    /**
     * 根据id查询某个用户的公开主页信息
     * @param id
     * @return
     */
    UserProfileVO getUserProfile(Long id);

    /**
     * 设置/重置支付密码（需先用登录密码验证身份）
     * @param payPasswordDTO 登录密码 + 新支付密码
     */
    void setPayPassword(PayPasswordDTO payPasswordDTO);

    /**
     * 当前登录用户是否已初始化支付密码
     * @return true 已设置
     */
    boolean hasPayPassword();
}
