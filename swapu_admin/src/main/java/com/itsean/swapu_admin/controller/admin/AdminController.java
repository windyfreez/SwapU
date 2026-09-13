package com.itsean.swapu_admin.controller.admin;

import com.itsean.pojo.PageResult;
import com.itsean.pojo.Result;
import com.itsean.swapu_admin.constant.JwtClaimsConstant;
import com.itsean.swapu_admin.context.BaseContext;
import com.itsean.swapu_admin.dto.AdminDTO;
import com.itsean.swapu_admin.dto.AdminLoginDTO;
import com.itsean.swapu_admin.dto.AdminPageQueryDTO;
import com.itsean.swapu_admin.dto.AdminPasswordDTO;
import com.itsean.swapu_admin.dto.AdminStatusDTO;
import com.itsean.swapu_admin.entity.Admin;
import com.itsean.swapu_admin.properties.JwtProperties;
import com.itsean.swapu_admin.service.AdminService;
import com.itsean.swapu_admin.utils.JwtUtil;
import com.itsean.swapu_admin.vo.AdminAddVO;
import com.itsean.swapu_admin.vo.AdminInfoVO;
import com.itsean.swapu_admin.vo.AdminLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员模块接口
 */
@RestController
@RequestMapping("/admin")
@Slf4j
@Api(tags = "管理员模块接口")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 管理员登录
     *
     * @param adminLoginDTO 登录参数
     * @return 管理端jwt令牌与登录管理员信息
     */
    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("管理员登录：{}", adminLoginDTO.getUsername());

        Admin admin = adminService.login(adminLoginDTO);

        //登录成功后签发管理端jwt令牌，使用 admin-secret-key
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        AdminInfoVO adminInfoVO = new AdminInfoVO();
        //只返回前端需要的字段，密码等敏感信息不外泄
        BeanUtils.copyProperties(admin, adminInfoVO);

        AdminLoginVO adminLoginVO = AdminLoginVO.builder()
                .token(token)
                .expireTime(jwtProperties.getAdminTtl())
                .adminInfo(adminInfoVO)
                .build();

        return Result.success(adminLoginVO);
    }

    /**
     * 新增管理员
     *
     * @param adminDTO 管理员信息
     * @return 新增管理员的ID
     */
    @PostMapping
    @ApiOperation("新增管理员")
    public Result<AdminAddVO> addAdmin(@RequestBody AdminDTO adminDTO) {
        log.info("新增管理员：{}", adminDTO.getUsername());

        Long adminId = adminService.addAdmin(adminDTO);

        return Result.success(new AdminAddVO(adminId));
    }

    /**
     * 分页查询管理员
     *
     * @param adminPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分页查询管理员")
    public Result<PageResult> pageQuery(AdminPageQueryDTO adminPageQueryDTO) {
        log.info("分页查询管理员：{}", adminPageQueryDTO);

        PageResult pageResult = adminService.pageQuery(adminPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 修改管理员信息
     *
     * @param adminDTO 管理员信息
     * @return 操作结果
     */
    @PutMapping
    @ApiOperation("修改管理员")
    public Result update(@RequestBody AdminDTO adminDTO) {
        log.info("修改管理员：{}", adminDTO.getId());

        adminService.update(adminDTO);

        return Result.success("管理员信息修改成功");
    }

    /**
     * 启用/禁用管理员
     *
     * @param id             管理员ID
     * @param adminStatusDTO 目标状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @ApiOperation("启用/禁用管理员")
    public Result updateStatus(@PathVariable Long id, @RequestBody AdminStatusDTO adminStatusDTO) {
        log.info("启用/禁用管理员：{}，状态：{}", id, adminStatusDTO.getStatus());

        adminService.updateStatus(id, adminStatusDTO.getStatus());

        return Result.success("管理员状态修改成功");
    }

    /**
     * 删除管理员
     *
     * @param id 管理员ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除管理员")
    public Result deleteById(@PathVariable Long id) {
        log.info("删除管理员：{}", id);

        adminService.deleteById(id);

        return Result.success("管理员删除成功");
    }

    /**
     * 管理员修改自己的密码
     *
     * @param adminPasswordDTO 原密码与新密码
     * @return 操作结果
     */
    @PutMapping("/password")
    @ApiOperation("管理员修改自己密码")
    public Result updatePassword(@RequestBody AdminPasswordDTO adminPasswordDTO) {
        log.info("管理员修改密码，adminId：{}", BaseContext.getCurrentId());

        adminService.updatePassword(adminPasswordDTO);

        return Result.success("密码修改成功");
    }

}
