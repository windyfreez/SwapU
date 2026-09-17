package com.itsean.swapu_admin.service;

import com.itsean.swapu_admin.dto.CategoryDTO;
import com.itsean.swapu_admin.entity.Category;
import com.itsean.swapu_admin.entity.result.Result;
import com.itsean.swapu_admin.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    /**
     * 获取所有分类
     * @return
     */
    Result list();

    /**
     * 获取全部分类（含停用），按 sort 升序
     * @return 分类列表数据
     */
    List<CategoryVO> listAll();

    /**
     * 新增分类
     * @param categoryDTO 分类信息
     * @return 新增分类的ID
     */
    Long addCategory(CategoryDTO categoryDTO);

    /**
     * 修改分类名称或排序值
     * @param categoryDTO 分类信息
     */
    void updateCategory(CategoryDTO categoryDTO);

    /**
     * 启用/禁用分类
     * @param id     分类ID
     * @param status 状态：1启用 0禁用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据id删除分类
     * @param id 分类ID
     */
    void deleteById(Long id);
}
