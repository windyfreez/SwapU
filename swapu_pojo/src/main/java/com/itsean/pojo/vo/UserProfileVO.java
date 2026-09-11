package com.itsean.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户公开主页信息VO：只暴露展示所需字段，不含手机号、邮箱、密码等隐私信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;//头像

    private Integer creditScore;//信用分

    private String college;//学院

    private Integer sellingCount;//在售商品个数

    private Integer soldCount;//已售出商品个数

}
