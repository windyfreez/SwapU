package com.itsean.pojo.dto;

import lombok.Data;

/**
 * 评论分页查询请求参数
 */
@Data
public class CommentPageQueryDTO {

    private Integer page = 1;//页码

    private Integer pageSize = 10;//每页记录数

}
