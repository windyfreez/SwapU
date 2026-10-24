package com.itsean.swapu_admin.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品发布人（卖家）信息
 */
@Data
@ApiModel(description = "商品发布人信息")
public class SellerVO implements Serializable {

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("用户账号")
    private String username;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("学院")
    private String college;

    @ApiModelProperty("信用分")
    private Integer creditScore;

}
