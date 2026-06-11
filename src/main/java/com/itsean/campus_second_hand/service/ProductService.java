package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.dto.ProductDTO;
import com.itsean.campus_second_hand.dto.ProductListPageQueryDTO;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.vo.ProductDetailVO;
import com.itsean.campus_second_hand.vo.ProductVO;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {
    /**
     * 添加商品
     * @param productDTO
     * @return
     */
    ProductVO addProduct(ProductDTO productDTO);

    /**
     * 根据id获取商品信息
     * @param id
     * @return
     */
    ProductDetailVO getProductById(Long id);

    /**
     * 修改商品信息
     * @param productDTO
     */
    void update(ProductDTO productDTO);

    /**
     * 下架商品
     * @param id
     */
    void takedown(Long id);

    /**
     * 删除商品
     * @param id
     */
    void delete(Long id);

    /**
     * 分页查询商品
     * @param productListPageQueryDTO
     * @return
     */
    PageResult pageQuery(ProductListPageQueryDTO productListPageQueryDTO);

    /**
     * 分页查询所有商品
     * @param productListPageQueryDTO
     * @return
     */
    PageResult allPageQuery(ProductListPageQueryDTO productListPageQueryDTO);

    /**
     * 上架商品
     * @param id
     */
    void takeup(Long id);
}
