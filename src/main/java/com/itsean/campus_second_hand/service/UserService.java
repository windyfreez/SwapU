package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.dto.PasswordDTO;
import com.itsean.campus_second_hand.dto.UserDTO;
import com.itsean.campus_second_hand.dto.UserLoginDTO;
import com.itsean.campus_second_hand.dto.UserRegisterDTO;
import com.itsean.campus_second_hand.entity.User;
import com.itsean.campus_second_hand.vo.UserRegisterVO;

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
}
