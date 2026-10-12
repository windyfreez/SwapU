package com.itsean.swapu_admin.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改用户信用分时传递的数据模型
 */
@Data
@ApiModel(description = "修改用户信用分时传递的数据模型")
public class UserCreditScoreDTO implements Serializable {

    @ApiModelProperty(value = "信用分，取值范围0-100", example = "90")
    private Integer creditScore;

}
