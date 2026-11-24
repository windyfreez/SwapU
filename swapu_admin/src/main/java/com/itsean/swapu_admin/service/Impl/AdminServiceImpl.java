package com.itsean.swapu_admin.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.common.captcha.CaptchaScene;
import com.itsean.common.captcha.CaptchaService;
import com.itsean.pojo.PageResult;
import com.itsean.swapu_admin.constant.AdminLogAction;
import com.itsean.swapu_admin.constant.AdminPermission;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.common.constant.MessageConstant;
import com.itsean.common.constant.NumberConstant;
import com.itsean.common.constant.StringConstant;
import com.itsean.common.context.BaseContext;
import com.itsean.swapu_admin.dto.AdminDTO;
import com.itsean.swapu_admin.dto.AdminLoginDTO;
import com.itsean.swapu_admin.dto.AdminPageQueryDTO;
import com.itsean.swapu_admin.dto.AdminPasswordDTO;
import com.itsean.pojo.entity.Admin;
import com.itsean.swapu_admin.exception.AdminException;
import com.itsean.swapu_admin.mapper.AdminMapper;
import com.itsean.swapu_admin.service.AdminOperationLogService;
import com.itsean.swapu_admin.service.AdminPermissionService;
import com.itsean.swapu_admin.service.AdminService;
import com.itsean.swapu_admin.vo.AdminMeVO;
import com.itsean.swapu_admin.vo.AdminVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    /**
     * 操作日志的对象类型：管理员
     */
    private static final String TARGET_TYPE_ADMIN = "ADMIN";

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private AdminPermissionService adminPermissionService;
    @Autowired
    private AdminOperationLogService adminOperationLogService;
    /**
     * 图形验证码服务：登录提交时先校验，通过后才查库比对密码
     */
    @Autowired
    private CaptchaService captchaService;

    /**
     * 管理员登录，校验账号密码与账号状态
     *
     * @param adminLoginDTO 登录参数
     * @return 校验通过的管理员信息
     */
    @Override
    public Admin login(AdminLoginDTO adminLoginDTO) {
        //先校验图形验证码：不通过就不查库，避免自动化脚本直接撞库
        captchaService.verify(CaptchaScene.ADMIN_LOGIN,
                adminLoginDTO.getCaptchaKey(), adminLoginDTO.getCaptchaCode());

        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        //参数缺失与账号密码错误返回同一提示，避免暴露账号是否注册
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw usernameOrPasswordError();
        }

        Admin admin = adminMapper.findByUsername(username);

        //账号不存在与密码错误不区分提示，防止账号枚举
        if (admin == null || admin.getPassword() == null || !BCrypt.checkpw(password, admin.getPassword())) {
            log.warn("管理员登录失败，账号或密码错误：{}", username);
            throw usernameOrPasswordError();
        }
        //账号已被禁用
        if (!isEnabled(admin)) {
            log.warn("管理员登录失败，账号已被禁用：{}", username);
            throw new AdminException(MessageConstant.ADMIN_ACCOUNT_DISABLED,
                    ErrorCodeConstant.ADMIN_ACCOUNT_DISABLED);
        }

        log.info("管理员登录成功：{}", username);
        return admin;
    }

    /**
     * 新增管理员
     *
     * @param adminDTO 管理员信息
     * @return 新增管理员的ID
     */
    @Override
    public Long addAdmin(AdminDTO adminDTO) {
        //仅超级管理员可新增管理员
        checkSuperAdmin();

        //参数校验：账号 2-20 字符、密码 6-20 位
        validateUsername(adminDTO.getUsername());
        validatePassword(adminDTO.getPassword());

        //账号唯一性校验
        if (adminMapper.findByUsername(adminDTO.getUsername()) != null) {
            throw new AdminException(MessageConstant.ADMIN_USERNAME_ALREADY_EXIST,
                    ErrorCodeConstant.ADMIN_USERNAME_ALREADY_EXIST);
        }

        //未指定角色时默认为普通管理员，并校验角色取值（只允许 1/2/3）
        Integer role = adminDTO.getRole() == null ? NumberConstant.ADMIN_ROLE_NORMAL : adminDTO.getRole();
        validateRole(role);

        Admin admin = new Admin();
        admin.setUsername(adminDTO.getUsername());
        //密码以 BCrypt 密文落库，禁止明文存储
        admin.setPassword(BCrypt.hashpw(adminDTO.getPassword()));
        admin.setName(adminDTO.getName());
        admin.setAvatar(adminDTO.getAvatar());
        admin.setRole(role);
        //新增账号默认启用
        admin.setStatus(NumberConstant.ADMIN_STATUS_ENABLED);

        LocalDateTime now = LocalDateTime.now();
        admin.setCreateTime(now);
        admin.setUpdateTime(now);

        adminMapper.insert(admin);
        log.info("新增管理员成功，adminId：{}，操作人：{}", admin.getId(), BaseContext.getCurrentId());
        adminOperationLogService.record(AdminLogAction.CREATE, TARGET_TYPE_ADMIN, admin.getId(),
                "username=" + admin.getUsername() + ", role=" + admin.getRole());
        return admin.getId();
    }

    /**
     * 分页查询管理员列表
     *
     * @param adminPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(AdminPageQueryDTO adminPageQueryDTO) {
        PageHelper.startPage(adminPageQueryDTO.getPageNum(), adminPageQueryDTO.getPageSize());

        List<Admin> admins = adminMapper.pageQuery(adminPageQueryDTO);

        Page<Admin> page = (Page<Admin>) admins;

        //转换为 VO 返回，避免密码等敏感字段外泄
        List<AdminVO> adminVOList = page.getResult().stream()
                .map(this::toAdminVO)
                .collect(Collectors.toList());

        return new PageResult(page.getTotal(), adminVOList);
    }

    /**
     * 修改管理员信息，传入密码时重置密码
     *
     * @param adminDTO 管理员信息
     */
    @Override
    public void update(AdminDTO adminDTO) {
        //仅超级管理员可修改管理员
        checkSuperAdmin();

        if (adminDTO.getId() == null) {
            throw new AdminException(MessageConstant.ADMIN_NOT_EXIST);
        }

        Admin existAdmin = adminMapper.findById(adminDTO.getId());
        if (existAdmin == null) {
            throw new AdminException(MessageConstant.ADMIN_NOT_EXIST);
        }

        //角色取值校验：不传表示不修改角色
        if (adminDTO.getRole() != null) {
            validateRole(adminDTO.getRole());
        }

        //不允许修改自己的角色：既防止把自己锁在系统外，也堵住自我提权的路径
        if (adminDTO.getId().equals(BaseContext.getCurrentId())
                && adminDTO.getRole() != null
                && !adminDTO.getRole().equals(existAdmin.getRole())) {
            log.warn("管理员尝试修改自己的角色，adminId：{}", adminDTO.getId());
            throw new AdminException(MessageConstant.ADMIN_CANNOT_CHANGE_SELF_ROLE,
                    ErrorCodeConstant.ADMIN_CANNOT_OPERATE_SELF);
        }

        //把最后一名启用的超级管理员降级为「任意非超管角色」，都会让平台失去超管入口
        boolean demotingSuperAdmin = adminDTO.getRole() != null
                && !NumberConstant.ADMIN_ROLE_SUPER.equals(adminDTO.getRole());
        if (isEnabledSuperAdmin(existAdmin) && demotingSuperAdmin) {
            checkNotLastSuperAdmin(adminDTO.getId());
        }

        Admin admin = new Admin();
        admin.setId(adminDTO.getId());
        admin.setName(adminDTO.getName());
        admin.setAvatar(adminDTO.getAvatar());
        admin.setRole(adminDTO.getRole());
        //传入密码时重置为密文，未传入则保持原密码不变
        if (StringUtils.hasText(adminDTO.getPassword())) {
            validatePassword(adminDTO.getPassword());
            admin.setPassword(BCrypt.hashpw(adminDTO.getPassword()));
        }
        admin.setUpdateTime(LocalDateTime.now());

        adminMapper.update(admin);
        log.info("修改管理员成功，adminId：{}，操作人：{}", adminDTO.getId(), BaseContext.getCurrentId());
        adminOperationLogService.record(AdminLogAction.UPDATE, TARGET_TYPE_ADMIN, adminDTO.getId(),
                buildUpdateDetail(existAdmin, adminDTO));
    }

    /**
     * 启用/禁用管理员
     *
     * @param id     管理员ID
     * @param status 状态：1启用 0禁用
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        //仅超级管理员可启用/禁用管理员
        checkSuperAdmin();

        if (!NumberConstant.ADMIN_STATUS_ENABLED.equals(status) && !NumberConstant.ADMIN_STATUS_DISABLED.equals(status)) {
            throw new AdminException(MessageConstant.ADMIN_STATUS_ERROR);
        }

        Admin existAdmin = adminMapper.findById(id);
        if (existAdmin == null) {
            throw new AdminException(MessageConstant.ADMIN_NOT_EXIST);
        }

        if (NumberConstant.ADMIN_STATUS_DISABLED.equals(status)) {
            //不能禁用自己，避免把自己锁在系统外
            if (id.equals(BaseContext.getCurrentId())) {
                throw new AdminException(MessageConstant.ADMIN_CANNOT_DISABLE_SELF,
                        ErrorCodeConstant.ADMIN_CANNOT_OPERATE_SELF);
            }
            //不能让平台失去最后一名启用的超级管理员
            if (isEnabledSuperAdmin(existAdmin)) {
                checkNotLastSuperAdmin(id);
            }
        }

        Admin admin = new Admin();
        admin.setId(id);
        admin.setStatus(status);
        admin.setUpdateTime(LocalDateTime.now());

        adminMapper.update(admin);
        log.info("修改管理员状态成功，adminId：{}，status：{}，操作人：{}", id, status, BaseContext.getCurrentId());
        adminOperationLogService.record(AdminLogAction.STATUS, TARGET_TYPE_ADMIN, id,
                "status:" + existAdmin.getStatus() + "->" + status);
    }

    /**
     * 根据id删除管理员
     *
     * @param id 管理员ID
     */
    @Override
    public void deleteById(Long id) {
        //仅超级管理员可删除管理员
        checkSuperAdmin();

        Admin existAdmin = adminMapper.findById(id);
        if (existAdmin == null) {
            throw new AdminException(MessageConstant.ADMIN_NOT_EXIST);
        }

        //不能删除自己
        if (id.equals(BaseContext.getCurrentId())) {
            throw new AdminException(MessageConstant.ADMIN_CANNOT_DELETE_SELF,
                    ErrorCodeConstant.ADMIN_CANNOT_OPERATE_SELF);
        }
        //不能让平台失去最后一名启用的超级管理员
        if (isEnabledSuperAdmin(existAdmin)) {
            checkNotLastSuperAdmin(id);
        }

        //逻辑删除：保留记录供审核流水追溯，同时把账号名改写为「原账号_del_{id}」以释放原账号名
        String releasedUsername = existAdmin.getUsername()
                + StringConstant.ADMIN_DELETED_USERNAME_SUFFIX + id;
        int affected = adminMapper.logicDeleteById(id, releasedUsername, LocalDateTime.now());
        if (affected != 1) {
            //并发场景下可能已被其他超管删除
            throw new AdminException(MessageConstant.ADMIN_NOT_EXIST);
        }
        log.info("删除管理员成功（逻辑删除），adminId：{}，释放账号名：{}，操作人：{}",
                id, releasedUsername, BaseContext.getCurrentId());
        adminOperationLogService.record(AdminLogAction.DELETE, TARGET_TYPE_ADMIN, id,
                "username=" + existAdmin.getUsername() + ", released=" + releasedUsername);
    }

    /**
     * 管理员修改自己的密码
     *
     * @param adminPasswordDTO 原密码与新密码
     */
    @Override
    public void updatePassword(AdminPasswordDTO adminPasswordDTO) {
        Long adminId = BaseContext.getCurrentId();
        Admin admin = adminMapper.findById(adminId);
        if (admin == null) {
            throw new AdminException(MessageConstant.ADMIN_NOT_EXIST);
        }

        String oldPassword = adminPasswordDTO.getOldPassword();
        String newPassword = adminPasswordDTO.getNewPassword();

        //原密码校验
        if (!StringUtils.hasText(oldPassword) || admin.getPassword() == null
                || !BCrypt.checkpw(oldPassword, admin.getPassword())) {
            throw new AdminException(MessageConstant.ADMIN_OLD_PASSWORD_ERROR);
        }
        //新旧密码不能相同
        if (oldPassword.equals(newPassword)) {
            throw new AdminException(MessageConstant.ADMIN_OLD_NEW_PASSWORD_SAME);
        }
        //新密码强度校验
        validatePassword(newPassword);

        Admin updateAdmin = new Admin();
        updateAdmin.setId(adminId);
        updateAdmin.setPassword(BCrypt.hashpw(newPassword));
        updateAdmin.setUpdateTime(LocalDateTime.now());

        adminMapper.update(updateAdmin);
        log.info("管理员修改密码成功，adminId：{}", adminId);
        adminOperationLogService.record(AdminLogAction.RESET_PASSWORD, TARGET_TYPE_ADMIN, adminId, "修改自己的密码");
    }

    /**
     * 根据ID查询某管理员详细信息
     * @param id
     * @return
     */
    @Override
    public Admin getById(Long id) {
        Admin admin = adminMapper.findById(id);
        if (admin == null) {
            throw new AdminException(MessageConstant.ADMIN_NOT_EXIST);
        }

        //本人可以查自己的信息；查他人需要管理员管理权限
        boolean self = id.equals(BaseContext.getCurrentId());
        if (!self && !adminPermissionService.hasPermission(AdminPermission.ADMIN_MANAGE)) {
            log.warn("无权限查看其他管理员信息，adminId：{}，targetId：{}", BaseContext.getCurrentId(), id);
            throw new AdminException(MessageConstant.ADMIN_PERMISSION_DENIED, ErrorCodeConstant.FORBIDDEN);
        }
        return admin;
    }

    /**
     * 查询当前登录管理员的信息与权限
     *
     * @return 当前管理员信息 + 权限点列表
     */
    @Override
    public AdminMeVO currentAdmin() {
        Long adminId = BaseContext.getCurrentId();
        Admin admin = adminMapper.findById(adminId);
        if (admin == null) {
            throw new AdminException(MessageConstant.ADMIN_NOT_EXIST);
        }

        //权限服务内部会校验启用状态：被禁用后即使 token 未过期，这里也会抛 4102
        Set<AdminPermission> permissions = adminPermissionService.currentPermissions();

        AdminMeVO adminMeVO = new AdminMeVO();
        adminMeVO.setId(admin.getId());
        adminMeVO.setUsername(admin.getUsername());
        adminMeVO.setName(admin.getName());
        adminMeVO.setAvatar(admin.getAvatar());
        adminMeVO.setRole(admin.getRole());
        adminMeVO.setRoleLabel(roleLabel(admin.getRole()));
        adminMeVO.setPermissions(permissions.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList()));
        return adminMeVO;
    }

    /**
     * 校验角色取值，只允许 1/2/3
     *
     * @param role 角色值
     */
    private void validateRole(Integer role) {
        if (!AdminPermission.isKnownRole(role)) {
            throw new AdminException(MessageConstant.ADMIN_ROLE_ERROR, ErrorCodeConstant.ADMIN_ROLE_ERROR);
        }
    }

    /**
     * 拼装「修改管理员」的变更摘要，用于操作日志
     * <p>
     * 只记录字段级差异（不含密码明文，只标记 password:reset），便于事后审计。
     *
     * @param existAdmin 修改前的管理员
     * @param adminDTO   本次提交的修改内容
     * @return 变更摘要，无变更时返回「无字段变更」
     */
    private String buildUpdateDetail(Admin existAdmin, AdminDTO adminDTO) {
        StringBuilder detail = new StringBuilder();
        if (adminDTO.getRole() != null && !adminDTO.getRole().equals(existAdmin.getRole())) {
            appendPart(detail, "role:" + existAdmin.getRole() + "->" + adminDTO.getRole());
        }
        if (StringUtils.hasText(adminDTO.getName()) && !adminDTO.getName().equals(existAdmin.getName())) {
            appendPart(detail, "name:" + existAdmin.getName() + "->" + adminDTO.getName());
        }
        if (StringUtils.hasText(adminDTO.getAvatar())) {
            appendPart(detail, "avatar:updated");
        }
        if (StringUtils.hasText(adminDTO.getPassword())) {
            appendPart(detail, "password:reset");
        }
        return detail.length() == 0 ? "无字段变更" : detail.toString();
    }

    /**
     * 以逗号分隔拼接变更片段
     *
     * @param builder 目标
     * @param part    片段
     */
    private void appendPart(StringBuilder builder, String part) {
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(part);
    }

    /**
     * 角色值转中文名称
     *
     * @param role 角色值
     * @return 角色名称，未知角色返回「未知角色」
     */
    private String roleLabel(Integer role) {
        if (NumberConstant.ADMIN_ROLE_SUPER.equals(role)) {
            return StringConstant.ADMIN_ROLE_SUPER_DESC;
        }
        if (NumberConstant.ADMIN_ROLE_OPERATION.equals(role)) {
            return StringConstant.ADMIN_ROLE_OPERATION_DESC;
        }
        if (NumberConstant.ADMIN_ROLE_NORMAL.equals(role)) {
            return StringConstant.ADMIN_ROLE_NORMAL_DESC;
        }
        return "未知角色";
    }

    /**
     * 校验当前登录人是否为启用状态的超级管理员，非超管一律按无权限处理
     */
    private void checkSuperAdmin() {
        Admin currentAdmin = adminMapper.findById(BaseContext.getCurrentId());
        //账号不存在、已被禁用或不是超级管理员，统一返回无权限
        if (currentAdmin == null || !isEnabled(currentAdmin)
                || !NumberConstant.ADMIN_ROLE_SUPER.equals(currentAdmin.getRole())) {
            log.warn("非超级管理员尝试执行管理员管理操作，adminId：{}", BaseContext.getCurrentId());
            throw new AdminException(MessageConstant.ADMIN_NO_PERMISSION, ErrorCodeConstant.UNAUTHORIZED);
        }
    }

    /**
     * 校验除指定管理员外，是否仍存在启用的超级管理员
     *
     * @param excludeId 需要排除的管理员ID
     */
    private void checkNotLastSuperAdmin(Long excludeId) {
        int remainCount = adminMapper.countByRoleAndStatusExcludeId(excludeId,
                NumberConstant.ADMIN_ROLE_SUPER, NumberConstant.ADMIN_STATUS_ENABLED);
        if (remainCount == 0) {
            throw new AdminException(MessageConstant.ADMIN_MUST_KEEP_ONE_SUPER,
                    ErrorCodeConstant.ADMIN_MUST_KEEP_ONE_SUPER);
        }
    }

    /**
     * 校验账号长度（2-20字符）
     *
     * @param username 账号
     */
    private void validateUsername(String username) {
        if (username == null || username.length() < NumberConstant.ADMIN_USERNAME_MIN_LENGTH
                || username.length() > NumberConstant.ADMIN_USERNAME_MAX_LENGTH) {
            throw new AdminException(MessageConstant.ADMIN_USERNAME_LENGTH_ERROR);
        }
    }

    /**
     * 校验密码长度（6-20位）
     *
     * @param password 密码
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < NumberConstant.ADMIN_PASSWORD_MIN_LENGTH
                || password.length() > NumberConstant.ADMIN_PASSWORD_MAX_LENGTH) {
            throw new AdminException(MessageConstant.ADMIN_PASSWORD_LENGTH_ERROR);
        }
    }

    /**
     * 判断管理员账号是否处于启用状态
     *
     * @param admin 管理员信息
     * @return true 启用
     */
    private boolean isEnabled(Admin admin) {
        return NumberConstant.ADMIN_STATUS_ENABLED.equals(admin.getStatus());
    }

    /**
     * 判断管理员是否为启用状态的超级管理员
     *
     * @param admin 管理员信息
     * @return true 是启用的超级管理员
     */
    private boolean isEnabledSuperAdmin(Admin admin) {
        return isEnabled(admin) && NumberConstant.ADMIN_ROLE_SUPER.equals(admin.getRole());
    }

    /**
     * 实体转列表 VO
     *
     * @param admin 管理员实体
     * @return 管理员列表数据
     */
    private AdminVO toAdminVO(Admin admin) {
        AdminVO adminVO = new AdminVO();
        BeanUtils.copyProperties(admin, adminVO);
        return adminVO;
    }

    /**
     * 构造账号或密码错误异常
     *
     * @return 管理员模块业务异常
     */
    private AdminException usernameOrPasswordError() {
        return new AdminException(MessageConstant.ADMIN_USERNAME_OR_PASSWORD_ERROR,
                ErrorCodeConstant.ADMIN_USERNAME_OR_PASSWORD_ERROR);
    }

}
