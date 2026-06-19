package com.itsean.campus_second_hand.controller.user;

import com.itsean.campus_second_hand.dto.FavoriteDTO;
import com.itsean.campus_second_hand.dto.FavoritePageQueryDTO;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.service.FavoriteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
@Slf4j
@Api(tags = "收藏接口")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加收藏
     * @param favoriteDTO
     * @return
     */
    @ApiOperation("添加收藏")
    @PostMapping("/add")
    private Result add(@RequestBody FavoriteDTO favoriteDTO){
        log.info("添加收藏：{}",favoriteDTO);
        favoriteService.add(favoriteDTO);
        return Result.success("收藏成功");
    }

    /**
     * 取消收藏
     * @param productId
     * @return
     */
    @ApiOperation("取消收藏")
    @DeleteMapping("/cancel")
    public Result cancel(@RequestParam Long productId){
        log.info("取消收藏：{}",productId);
        favoriteService.cancel(productId);
        return Result.success("取消成功");
    }

    /**
     * 获取收藏列表
     * @param favoritePageQueryDTO
     * @return
     */
    @ApiOperation("获取收藏列表")
    @GetMapping("/list")
    public Result<PageResult> list(FavoritePageQueryDTO favoritePageQueryDTO){
    	log.info("获取收藏列表：{}",favoritePageQueryDTO);
        PageResult pageResult = favoriteService.pageQuery(favoritePageQueryDTO);
    	return Result.success(pageResult);
    }
}
