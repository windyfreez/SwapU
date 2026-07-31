package com.itsean.swapu_admin.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.itsean.swapu_admin.entity.Category;
import com.itsean.swapu_admin.entity.result.Result;
import com.itsean.swapu_admin.mapper.CategoryMapper;
import com.itsean.swapu_admin.service.CategoryService;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.itsean.swapu_admin.constant.MessageConstant.CATEGORY_NOT_EXIST;
import static com.itsean.swapu_admin.constant.StringConstant.CATEGORY_LIST_PREFIX;

@Service
public class CatrgoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取所有分类
     * @return
     */
    @Override
    public Result list() {
        String key = CATEGORY_LIST_PREFIX;
        String categoryJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(categoryJson)){
            List<Category> categoryList = JSONUtil.toList(categoryJson, Category.class);
            return Result.success(categoryList);
        }
        List<Category> categoryList = categoryMapper.list();

        if (CollUtil.isEmpty(categoryList)) {
            return Result.error(CATEGORY_NOT_EXIST);
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(categoryList));
        return Result.success(categoryList);
    }
}
