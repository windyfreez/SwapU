package com.itsean.swapu_admin.controller.admin;

import com.itsean.pojo.Result;
import com.itsean.swapu_admin.dto.CategoryDTO;
import com.itsean.swapu_admin.dto.CategoryStatusDTO;
import com.itsean.swapu_admin.service.CategoryService;
import com.itsean.swapu_admin.vo.CategoryAddVO;
import com.itsean.swapu_admin.vo.CategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类管理接口
 */
@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类管理接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 分类列表
     * @return 全部分类（含停用），按 sort 升序
     */
    @GetMapping("/list")
    @ApiOperation("分类列表")
    public Result<List<CategoryVO>> list() {
        log.info("查询分类列表");
        List<CategoryVO> categoryVOList = categoryService.listAll();
        return Result.success(categoryVOList);
    }

    /**
     * 新增分类
     * @param categoryDTO 分类信息
     * @return 新增分类的ID
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result<CategoryAddVO> add(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO.getName());
        Long categoryId = categoryService.addCategory(categoryDTO);
        return Result.success(new CategoryAddVO(categoryId));
    }

    /**
     * 修改分类
     * @param categoryDTO 分类信息
     * @return 操作结果
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类：{}", categoryDTO.getId());
        categoryService.updateCategory(categoryDTO);
        return Result.success("分类信息修改成功");
    }

    /**
     * 启用/禁用分类
     * @param id                分类ID
     * @param categoryStatusDTO 目标状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    @ApiOperation("启用/禁用分类")
    public Result updateStatus(@ApiParam(value = "分类ID", example = "1") @PathVariable Long id, @RequestBody CategoryStatusDTO categoryStatusDTO) {
        log.info("启用/禁用分类：{}，状态：{}", id, categoryStatusDTO.getStatus());
        categoryService.updateStatus(id, categoryStatusDTO.getStatus());
        return Result.success("分类状态修改成功");
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除分类")
    public Result deleteById(@ApiParam(value = "分类ID", example = "1") @PathVariable Long id) {
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success("分类删除成功");
    }

}
