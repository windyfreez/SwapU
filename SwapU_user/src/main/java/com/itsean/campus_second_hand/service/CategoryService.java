package com.itsean.campus_second_hand.service;

import com.itsean.pojo.entity.Category;
import com.itsean.pojo.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    /**
     * 获取所有分类
     * @return
     */
    Result list();
}
