package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    /**
     * 获取所有分类
     * @return
     */
    List<Category> list();
}
