package com.itsean.campus_second_hand.controller.user;

import com.itsean.campus_second_hand.dto.ProductDTO;
import com.itsean.campus_second_hand.dto.ProductListPageQueryDTO;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.service.ProductService;
import com.itsean.campus_second_hand.vo.ProductDetailVO;
import com.itsean.campus_second_hand.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/product")
@Slf4j
@Api(tags = "商品接口")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 添加商品
     * @param productDTO
     * @return
     */
    @PostMapping
    @ApiOperation("添加商品")
    public Result<ProductVO> addProduct(@RequestBody ProductDTO productDTO){
        log.info("添加商品：{}",productDTO);
        ProductVO productVO = productService.addProduct(productDTO);
        return Result.success(productVO);
    }

    /**
     * 根据id获取商品信息
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("根据id获取商品信息")
    public Result<ProductDetailVO> getProductById(@PathVariable Long id){
    	log.info("根据id获取商品信息：{}",id);
    	ProductDetailVO productDetailVO = productService.getProductById(id);
    	return Result.success(productDetailVO);
    }

    /**
     * 修改商品信息
     * @param productDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改商品信息")
    public Result updateProduct(@RequestBody ProductDTO productDTO){
        log.info("修改商品信息：{}",productDTO);
        productService.update(productDTO);
        return Result.success();
    }

    /**
     * 下架商品
     * @param id
     * @return
     */
    @PutMapping("/{id}/off")
    @ApiOperation("下架商品")
    public Result takedown(@PathVariable Long id){
        log.info("下架商品：{}",id);
        productService.takedown(id);
        return Result.success();
    }

    /**
     * 上架商品
     * @param id
     * @return
     */
    @PutMapping("/{id}/on")
    @ApiOperation("上架商品")
    public Result takeup(@PathVariable Long id){
        log.info("上架商品：{}",id);
        productService.takeup(id);
        return Result.success();
    }

    /**
     * 删除商品
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除商品")
    public Result delete(@PathVariable Long id){
        log.info("删除商品：{}",id);
        productService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 分页获取当前用户所有商品
     * @param productListPageQueryDTO
     * @return
     */
    @ApiOperation("分页获取当前用户所有商品")
    @GetMapping("/my-products")
    public Result<PageResult> findAll(ProductListPageQueryDTO productListPageQueryDTO){
        log.info("分页获取当前用户所有商品：{}",productListPageQueryDTO);
        PageResult pageResult = productService.pageQuery(productListPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 分页获取所有商品
     * @param productListPageQueryDTO
     * @return
     */
    @ApiOperation("分页获取所有商品")
    @GetMapping("/list")
    public Result<PageResult> list(ProductListPageQueryDTO productListPageQueryDTO){
        log.info("分页获取所有商品：{}",productListPageQueryDTO);
        PageResult pageResult = productService.allPageQuery(productListPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取热门商品并存入Redis
     * @return
     */
    @ApiOperation("获取热门商品")
    @GetMapping("/hot")
    public Result hotProducts() {
        log.info("获取热门商品并存入Redis");
        List<Object> list = redisTemplate.opsForList().range("hot:products", 0, 19);
        return Result.success(list);
    }


}
