package com.itsean.campus_second_hand.service.Impl;

import com.itsean.campus_second_hand.constant.MessageConstant;
import com.itsean.campus_second_hand.constant.NumberConstant;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.dto.PasswordDTO;
import com.itsean.campus_second_hand.dto.UserDTO;
import com.itsean.campus_second_hand.dto.UserLoginDTO;
import com.itsean.campus_second_hand.dto.UserRegisterDTO;
import com.itsean.campus_second_hand.entity.User;
import com.itsean.campus_second_hand.exception.AccountRepeatException;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.campus_second_hand.service.UserService;
import com.itsean.campus_second_hand.vo.UserRegisterVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 新用户注册
     * @param userRegisterDTO
     * @return
     */
    @Override
    public UserRegisterVO register(UserRegisterDTO userRegisterDTO) {

        String phone = userRegisterDTO.getPhone();
        String email = userRegisterDTO.getEmail();
        String studentId = userRegisterDTO.getStudentId();
        UserRegisterVO userRegisterVO = new UserRegisterVO();
        User user = new User();

        int phoneRepeat = userMapper.checkRepeatByPhone(phone);
        int emailRepeat = userMapper.checkRepeatByEmail(email);
        int studentIdRepeat = userMapper.checkRepeatByStudentId(studentId);

        if (phoneRepeat == 0 && emailRepeat == 0 && studentIdRepeat == 0) {
            BeanUtils.copyProperties(userRegisterDTO, userRegisterVO);
            BeanUtils.copyProperties(userRegisterDTO,user);

            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            user.setStatus(NumberConstant.ACCOUNT_STATUS_NORMAL);
            user.setCreditScore(NumberConstant.FULL_CREDIT_SCORE);
            user.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));
            userMapper.register(user);

        }else if(phoneRepeat == 1){
            throw new AccountRepeatException(MessageConstant.PHONE_ALREADY_EXIST);
        }else if(emailRepeat == 1){
            throw new AccountRepeatException(MessageConstant.EMAIL_ALREADY_EXIST);
        }else if(studentIdRepeat == 1){
            throw new AccountRepeatException(MessageConstant.STUDENT_ID_ALREADY_EXIST);
        }

        return userRegisterVO;
    }

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes());
        User user = userMapper.findByUsername(username);

        //账户不存在
        if(user == null){
            throw new AccountRepeatException(MessageConstant.USER_NOT_EXIST);
        }
        //密码不正确
        if(!user.getPassword().equals(password)){
            throw new AccountRepeatException(MessageConstant.PASSWORD_ERROR);
        }
        //账户已锁定
        if(user.getStatus().equals(NumberConstant.ACCOUNT_STATUS_LOCK)){
            throw new AccountRepeatException(MessageConstant.USER_LOCK);
        }

        return user;
    }

    /**
     * 获取用户信息
     * @return
     */
    @Override
    public User getInfo() {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.findById(userId);
        String phone = user.getPhone();

        user.setPhone(phone);
        //密码隐私保护
        user.setPassword("****");
        return user;
    }

    /**
     * 修改用户信息
     * @param userDTO
     */
    @Override
    public void update(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);

        user.setId(BaseContext.getCurrentId());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.update(user);
    }

    /**
     * 修改用户密码
     * @param passwordDTO
     */
    @Override
    public void updatePassword(PasswordDTO passwordDTO) {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.findById(userId);
        String oldPassword = passwordDTO.getOldPassword();
        String newPassword = passwordDTO.getNewPassword();
        String confirmPassword = passwordDTO.getConfirmPassword();

        //异常检验
        if (!DigestUtils.md5DigestAsHex(oldPassword.getBytes()).equals(user.getPassword())) {
            throw new AccountRepeatException(MessageConstant.OLD_PASSWORD_ERROR);
        }//旧密码和新密码相同
        if (oldPassword.equals(newPassword)) {
            throw new AccountRepeatException(MessageConstant.OLD_NEW_PASSWORD_SAME);
        }//确认密码与新密码不相同
        if (!newPassword.equals(confirmPassword)) {
            throw new AccountRepeatException(MessageConstant.CONFIRM_NEW_PASSWORD_NOT_SAME);
        }//新密码和确认密码不相同

        //更新密码并上传数据库
        String encryptedNewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(encryptedNewPassword);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     *  根据id查询用户信息
     * @param id
     * @return
     */
    @Override
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        return user;
    }
}
