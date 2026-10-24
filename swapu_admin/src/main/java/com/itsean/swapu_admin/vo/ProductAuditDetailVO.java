package com.itsean.swapu_admin.vo;

import com.itsean.swapu_admin.entity.Product;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品审核详情数据：商品完整信息 + 发布人信息 + 历史审核流水
 */
@Data
@ApiModel(description = "商品审核详情数据")
public class ProductAuditDetailVO implements Serializable {

    @ApiModelProperty("商品完整信息（含 description、quantity 等）")
    private Product product;

    @ApiModelProperty("发布人信息")
    private SellerVO seller;

    @ApiModelProperty("该商品历史审核流水（含 aiResult）")
    private List<AuditRecordVO> auditRecords;

}
