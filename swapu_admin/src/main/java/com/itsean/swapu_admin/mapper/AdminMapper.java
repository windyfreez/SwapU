package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.dto.AdminPageQueryDTO;
import com.itsean.swapu_admin.entity.Admin;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminMapper {

    /**
     * 根据账号查询管理员信息（含密码密文，用于登录校验与账号唯一性校验）
     *
     * @param username 管理员账号
     * @return 管理员信息，不存在时返回 null
     */
    @Select("select * from admin where username = #{username}")
    Admin findByUsername(String username);

    /**
     * 根据id查询管理员信息
     *
     * @param id 管理员ID
     * @return 管理员信息，不存在时返回 null
     */
    @Select("select * from admin where id = #{id}")
    Admin findById(Long id);

    /**
     * 新增管理员，并回填自增主键
     *
     * @param admin 管理员信息
     */
    @Insert("insert into admin (username, password, name, avatar, role, status, create_time, update_time)\n" +
            "values (#{username},#{password},#{name},#{avatar},#{role},#{status},#{createTime},#{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Admin admin);

    /**
     * 统计除指定管理员外，指定角色且指定状态的管理员数量
     * 用于「至少保留一名启用的超级管理员」校验，结果为 0 说明该管理员是最后一名符合条件的账号
     *
     * @param excludeId 需要排除的管理员ID
     * @param role      角色
     * @param status    状态
     * @return 符合条件的管理员数量
     */
    @Select("select count(*) from admin where role = #{role} and status = #{status} and id != #{excludeId}")
    int countByRoleAndStatusExcludeId(@Param("excludeId") Long excludeId,
                                      @Param("role") Integer role,
                                      @Param("status") Integer status);

    /**
     * 动态修改管理员信息（只更新非 null 字段）
     *
     * @param admin 管理员信息
     */
    void update(Admin admin);

    /**
     * 根据id删除管理员
     *
     * @param id 管理员ID
     */
    @Delete("delete from admin where id = #{id}")
    void deleteById(Long id);

    /**
     * 分页查询管理员列表
     *
     * @param adminPageQueryDTO 分页查询条件
     * @return 当前页管理员列表
     */
    List<Admin> pageQuery(AdminPageQueryDTO adminPageQueryDTO);

}
