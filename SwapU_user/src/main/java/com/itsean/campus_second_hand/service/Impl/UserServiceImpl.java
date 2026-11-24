package com.itsean.campus_second_hand.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.itsean.common.captcha.CaptchaScene;
import com.itsean.common.captcha.CaptchaService;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.pojo.dto.PasswordDTO;
import com.itsean.pojo.dto.PayPasswordDTO;
import com.itsean.pojo.dto.UserDTO;
import com.itsean.pojo.dto.UserLoginDTO;
import com.itsean.pojo.dto.UserRegisterDTO;
import com.itsean.pojo.entity.User;
import com.itsean.common.exception.AccountRepeatException;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.UserMapper;
import com.itsean.campus_second_hand.service.UserService;
import com.itsean.pojo.vo.UserRegisterVO;
import com.itsean.pojo.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    /**
     * 图形验证码服务：登录与注册提交时先校验，通过后才继续后面的业务
     */
    @Autowired
    private CaptchaService captchaService;

    /**
     * 新用户注册
     * @param userRegisterDTO
     * @return
     */
    @Override
    public UserRegisterVO register(UserRegisterDTO userRegisterDTO) {
        //先校验图形验证码，避免注册接口被批量刷号
        captchaService.verify(CaptchaScene.USER_REGISTER,
                userRegisterDTO.getCaptchaKey(), userRegisterDTO.getCaptchaCode());

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
            //支付密码单独存一份 BCrypt 密文：初始值与登录密码相同，便于新用户直接支付，
            //但两者是独立字段，之后改登录密码不会影响已单独设置过的支付密码
            user.setPayPassword(BCrypt.hashpw(userRegisterDTO.getPassword()));
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
        //先校验图形验证码：不通过就不查库，避免脚本直接撞库
        captchaService.verify(CaptchaScene.USER_LOGIN,
                userLoginDTO.getCaptchaKey(), userLoginDTO.getCaptchaCode());

        String username = userLoginDTO.getUsername();
        String password = DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes());
        User user = userMapper.findByUsername(username);

        //账号不存在与密码错误返回同一提示：既能少暴露信息，也能避免被用来枚举账号
        if(user == null || !user.getPassword().equals(password)){
            throw new AccountRepeatException(MessageConstant.LOGIN_CREDENTIALS_ERROR);
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
        //支付密码密文绝不外传（实体上也有 @JsonIgnore 兜底）
        user.setPayPassword(null);
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

        //兼容处理：如果用户从未单独设置过支付密码（仍与旧登录密码相同），
        //改登录密码时同步更新支付密码，避免「改完密码就无法支付」；一旦单独设置过则保持独立
        if (user.getPayPassword() != null && BCrypt.checkpw(oldPassword, user.getPayPassword())) {
            user.setPayPassword(BCrypt.hashpw(newPassword));
        }

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

    /**
     * 根据id查询某个用户的公开主页信息
     * @param id
     * @return
     */
    @Override
    public UserProfileVO getUserProfile(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new AccountRepeatException(MessageConstant.USER_NOT_EXIST);
        }

        UserProfileVO userProfileVO = new UserProfileVO();
        //只拷贝公开字段，手机号、邮箱、密码等隐私信息不对外暴露
        BeanUtils.copyProperties(user, userProfileVO);
        //补全在售与已售出的商品个数
        userProfileVO.setSellingCount(productMapper.countByUserIdAndStatus(id, NumberConstant.PRODUCT_STATUS_SELLING));
        userProfileVO.setSoldCount(productMapper.countByUserIdAndStatus(id, NumberConstant.PRODUCT_STATUS_SOLD_OUT));

        return userProfileVO;
    }

    /**
     * 设置/重置支付密码：先用登录密码确认身份，再以 BCrypt 落库
     * @param payPasswordDTO 登录密码 + 新支付密码
     */
    @Override
    public void setPayPassword(PayPasswordDTO payPasswordDTO) {
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountRepeatException(MessageConstant.USER_NOT_EXIST);
        }

        //登录密码校验通过，才认为操作人是账号本人
        String loginPasswordHash = DigestUtils.md5DigestAsHex(payPasswordDTO.getLoginPassword().getBytes());
        if (!loginPasswordHash.equals(user.getPassword())) {
            log.warn("用户{}设置支付密码失败：登录密码不正确", userId);
            throw new AccountRepeatException(MessageConstant.PASSWORD_ERROR);
        }

        user.setPayPassword(BCrypt.hashpw(payPasswordDTO.getPayPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        log.info("用户{}已更新支付密码", userId);
    }

    /**
     * 当前登录用户是否已初始化支付密码
     * @return true 已设置
     */
    @Override
    public boolean hasPayPassword() {
        User user = userMapper.findById(BaseContext.getCurrentId());
        return user != null && user.getPayPassword() != null && !user.getPayPassword().isEmpty();
    }
}
