package com.itsean.swapu_admin.service;

import com.itsean.swapu_admin.entity.Category;
import com.itsean.swapu_admin.entity.result.Result;
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
