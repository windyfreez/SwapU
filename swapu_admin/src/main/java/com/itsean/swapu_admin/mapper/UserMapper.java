package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.dto.UserRegisterDTO;
import com.itsean.swapu_admin.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 检查手机号是否与已有用户重复
     * @param phone
     * @return
     */
    @Select("select count(*) from user where phone = #{phone}")
    int checkRepeatByPhone(String phone);

    /**
     * 检查邮箱是否与已有用户重复
     * @param email
     * @return
     */
    @Select("select count(*) from user where email = #{email}")
    int checkRepeatByEmail(String email);

    /**
     * 检查学号是否与已有用户重复
     * @param studentId
     * @return
     */
    @Select("select count(*) from user where student_id = #{studentId}")
    int checkRepeatByStudentId(String studentId);

    /**
     *  新用户注册
     * @param user
     * @return
     */
    @Insert("insert into user (student_id, username, password, avatar, phone, email, college, balance, credit_score, status, create_time, update_time, nickname)\n" +
            "values (#{studentId},#{username},#{password},#{avatar},#{phone},#{email},#{college},#{balance},#{creditScore},#{status},#{createTime},#{updateTime},#{nickname})")
    void register(User user);

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    @Select("select * from user where username = #{username}")
    User findByUsername(String username);

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{id}")
    User findById(Long userId);

    /**
     * 根据id修改用户信息
     * @param user
     */
    void update(User user);
}
