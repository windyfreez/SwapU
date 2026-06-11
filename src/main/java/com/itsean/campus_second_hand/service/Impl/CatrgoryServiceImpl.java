package com.itsean.campus_second_hand.service.Impl;

import com.itsean.campus_second_hand.entity.Category;
import com.itsean.campus_second_hand.mapper.CategoryMapper;
import com.itsean.campus_second_hand.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatrgoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> list() {
        List<Category> categoryList = categoryMapper.list();
        return categoryList;
    }
}
