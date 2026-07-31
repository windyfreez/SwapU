package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 获取所有分类
     * @return
     */
    List<Category> list();
}
