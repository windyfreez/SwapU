package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.dto.FavoriteDTO;
import com.itsean.swapu_admin.dto.FavoritePageQueryDTO;
import com.itsean.swapu_admin.entity.Product;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

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
}
