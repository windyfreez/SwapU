package com.itsean.campus_second_hand.controller.user;

import com.itsean.campus_second_hand.entity.Category;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@Api(tags = "商品分类接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("获取所有分类")
    public Result<List<Category>> list(){
        log.info("获取所有分类");
        List<Category> categoryList = categoryService.list();
        return Result.success(categoryList);
    }
}
