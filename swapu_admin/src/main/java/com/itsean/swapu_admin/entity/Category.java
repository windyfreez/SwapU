package com.itsean.swapu_admin.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 商品分类实体，对应 category 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long id;//分类ID

    private String name;//分类名称

    private Integer sort;//排序值，越小越靠前

    private Integer status;//状态：1启用 0禁用

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;//创建时间

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;//更新时间

    private Long createUser;//创建人ID

    private Long updateUser;//更新人ID

}
