package com.itsean.swapu_admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员分页列表数据（不含密码等敏感字段）
 */
@Data
@ApiModel(description = "管理员分页列表数据")
public class AdminVO implements Serializable {

    @ApiModelProperty("管理员ID")
    private Long id;

    @ApiModelProperty("账号")
    private String username;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("角色：1超级 2普通")
    private Integer role;

    @ApiModelProperty("状态：1启用 0禁用")
    private Integer status;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
