package com.itsean.swapu_admin.service;

import com.itsean.pojo.PageResult;
import com.itsean.swapu_admin.dto.AdminDTO;
import com.itsean.swapu_admin.dto.AdminLoginDTO;
import com.itsean.swapu_admin.dto.AdminPageQueryDTO;
import com.itsean.swapu_admin.dto.AdminPasswordDTO;
import com.itsean.swapu_admin.entity.Admin;

/**
 * 管理员模块业务接口
 */
public interface AdminService {

    /**
     * 管理员登录，校验账号密码与账号状态
     *
     * @param adminLoginDTO 登录参数
     * @return 校验通过的管理员信息，由 Controller 签发管理端 JWT
     */
    Admin login(AdminLoginDTO adminLoginDTO);

    /**
     * 新增管理员
     *
     * @param adminDTO 管理员信息
     * @return 新增管理员的ID
     */
    Long addAdmin(AdminDTO adminDTO);

    /**
     * 分页查询管理员列表
     *
     * @param adminPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQuery(AdminPageQueryDTO adminPageQueryDTO);

    /**
     * 修改管理员信息，传入密码时重置密码
     *
     * @param adminDTO 管理员信息
     */
    void update(AdminDTO adminDTO);

    /**
     * 启用/禁用管理员
     *
     * @param id     管理员ID
     * @param status 状态：1启用 0禁用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据id删除管理员
     *
     * @param id 管理员ID
     */
    void deleteById(Long id);

    /**
     * 管理员修改自己的密码
     *
     * @param adminPasswordDTO 原密码与新密码
     */
    void updatePassword(AdminPasswordDTO adminPasswordDTO);

    /**
     * 根据ID查询管理员详细信息
     * @param id
     * @return
     */
    Admin getById(Long id);
}
