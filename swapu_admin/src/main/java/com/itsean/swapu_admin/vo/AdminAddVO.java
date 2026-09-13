package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 新增管理员返回的数据格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "新增管理员返回的数据格式")
public class AdminAddVO implements Serializable {

    @ApiModelProperty("新增管理员的ID")
    private Long adminId;

}
