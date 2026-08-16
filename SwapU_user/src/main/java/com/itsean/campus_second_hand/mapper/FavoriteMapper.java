package com.itsean.campus_second_hand.mapper;

import com.itsean.campus_second_hand.dto.FavoriteDTO;
import com.itsean.campus_second_hand.dto.FavoritePageQueryDTO;
import com.itsean.campus_second_hand.entity.Product;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    /**
     * 添加收藏
     * @param favoriteDTO
     */
    @Insert("insert into favorite (user_id, product_id, create_time) values (#{userId},#{productId},#{createTime});")
    void add(FavoriteDTO favoriteDTO);

    /**
     * 取消收藏
     * @param productId
     * @param userId
     */
    @Delete("delete from favorite where product_id = #{productId} and user_id = #{userId};")
    void cancel(Long productId, Long userId);

    /**
     * 获取收藏列表
     * @param favoritePageQueryDTO
     * @param userId
     * @return
     */
    List<Product> pageQuery(FavoritePageQueryDTO favoritePageQueryDTO, Long userId);

    /**
     * 统计当前用户收藏的商品数量
     * @param userId
     * @return
     */
    @Select("select count(*) from favorite where user_id = #{userId}")
    Integer countByUserId(Long userId);
}
