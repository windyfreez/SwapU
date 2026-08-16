package com.itsean.campus_second_hand.controller.user;

import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.service.ViewService;
import com.itsean.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/view-history")
@Slf4j
@Api(tags = "浏览记录相关接口")
public class ViewController {

    @Autowired
    private ViewService viewService;

    @ApiOperation("分页获取当前用户浏览过的商品")
    @GetMapping("/list")
    public Result<PageResult> listView(int pageNum, int pageSize){
        log.info("分页获取浏览过的商品,id:{}", BaseContext.getCurrentId());
        PageResult pageResult = viewService.listView(pageNum, pageSize);
        return Result.success(pageResult);
    }
}
