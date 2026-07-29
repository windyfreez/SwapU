package com.itsean.campus_second_hand.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.itsean.campus_second_hand.entity.Category;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.mapper.CategoryMapper;
import com.itsean.campus_second_hand.service.CategoryService;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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
        String key = "category:list:";
        String categoryJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(categoryJson)){
            List<Category> categoryList = JSONUtil.toList(categoryJson, Category.class);
            return Result.success(categoryList);
        }
        List<Category> categoryList = categoryMapper.list();

        if (CollUtil.isEmpty(categoryList)) {
            return Result.error("商品类型不存在");
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(categoryList));
        return Result.success(categoryList);
    }
}
