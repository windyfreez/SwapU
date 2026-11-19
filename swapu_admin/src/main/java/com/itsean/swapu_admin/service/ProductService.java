package com.itsean.swapu_admin.service;

import com.itsean.pojo.dto.ProductDTO;
import com.itsean.pojo.dto.ProductListPageQueryDTO;
import com.itsean.pojo.PageResult;
import com.itsean.pojo.Result;
import com.itsean.pojo.vo.ProductDetailVO;
import com.itsean.pojo.vo.ProductVO;
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

    /**
     * 获取top20热门商品
     * @return
     */
    Result top20List();
}
