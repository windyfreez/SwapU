package com.itsean.campus_second_hand.mapper;

import com.itsean.campus_second_hand.entity.Category;
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
