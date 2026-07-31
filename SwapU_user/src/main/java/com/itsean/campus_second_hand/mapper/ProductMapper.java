package com.itsean.campus_second_hand.mapper;

import com.itsean.campus_second_hand.dto.ProductListPageQueryDTO;
import com.itsean.campus_second_hand.entity.Product;
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
}
