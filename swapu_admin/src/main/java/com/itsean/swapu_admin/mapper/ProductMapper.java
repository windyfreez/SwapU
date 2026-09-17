package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.dto.ProductListPageQueryDTO;
import com.itsean.swapu_admin.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ProductMapper {

    /**
     * 添加商品
     * @param product
     */
    void addProduct(Product product);

    /**
     * 根据id获取商品信息
     * @param id
     * @return
     */
    @Select("select * from product where id = #{id}")
    @ResultMap("ProductResultMap")
    Product getProductById(Long id);

    /**
     * 修改商品信息
     * @param product
     */
    void update(Product product);

    /**
     * 删除商品
     * @param id
     * @return
     */
    @Delete("delete from product where id = #{id}")
    void deleteById(Long id);

    /**
     * 分页查询当前用户商品
     * @param productListPageQueryDTO
     * @return
     */
    List<Product> pageQuery(ProductListPageQueryDTO productListPageQueryDTO);

    /**
     * 分页查询所有商品
     * @param productListPageQueryDTO
     * @return
     */
    List<Product> allPageQuery(ProductListPageQueryDTO productListPageQueryDTO);

    /**
     * 批量更新浏览量
     * @param list
     */
    void batchUpdateViewCount(@Param("list") List<HashMap<String, Long>> list);

    /**
     * 查询最热门商品
     * @param limit
     * @return
     */
    List<Product> selectHotProducts(@Param("limit") int limit);

    /**
     * 统计分类下的商品数量，用于删除分类前的占用校验
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    @Select("select count(*) from product where category_id = #{categoryId}")
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 统计分类下指定状态的商品数量，用于禁用分类前的在售商品校验
     *
     * @param categoryId 分类ID
     * @param status     商品状态
     * @return 商品数量
     */
    @Select("select count(*) from product where category_id = #{categoryId} and status = #{status}")
    int countByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") Integer status);
}
