package com.itsean.swapu_admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.constant.MessageConstant;
import com.itsean.swapu_admin.constant.NumberConstant;
import com.itsean.swapu_admin.constant.StringConstant;
import com.itsean.swapu_admin.context.BaseContext;
import com.itsean.swapu_admin.dto.CategoryDTO;
import com.itsean.swapu_admin.entity.Category;
import com.itsean.swapu_admin.entity.result.Result;
import com.itsean.swapu_admin.exception.CategoryException;
import com.itsean.swapu_admin.mapper.CategoryMapper;
import com.itsean.swapu_admin.mapper.ProductMapper;
import com.itsean.swapu_admin.service.CategoryService;
import com.itsean.swapu_admin.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取所有分类
     * @return
     */
    @Override
    public Result list() {
        String key = StringConstant.CATEGORY_LIST_PREFIX;
        String categoryJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(categoryJson)){
            List<Category> categoryList = JSONUtil.toList(categoryJson, Category.class);
            return Result.success(categoryList);
        }
        List<Category> categoryList = categoryMapper.list();

        if (CollUtil.isEmpty(categoryList)) {
            return Result.error(MessageConstant.CATEGORY_NOT_EXIST);
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(categoryList));
        return Result.success(categoryList);
    }

    /**
     * 获取全部分类（含停用），按 sort 升序
     * @return 分类列表数据
     */
    @Override
    public List<CategoryVO> listAll() {
        List<Category> categoryList = categoryMapper.listAll();

        //转换为 VO 返回
        return categoryList.stream()
                .map(this::toCategoryVO)
                .collect(Collectors.toList());
    }

    /**
     * 新增分类
     * @param categoryDTO 分类信息
     * @return 新增分类的ID
     */
    @Override
    public Long addCategory(CategoryDTO categoryDTO) {
        String name = checkName(categoryDTO.getName());

        //分类名称唯一性校验
        if (categoryMapper.findByName(name) != null) {
            throw categoryNameAlreadyExist();
        }

        Category category = new Category();
        category.setName(name);
        //未指定排序值时默认排在最前
        category.setSort(categoryDTO.getSort() == null ? NumberConstant.DEFAULT_CATEGORY_SORT : categoryDTO.getSort());
        //新增分类默认启用
        category.setStatus(NumberConstant.CATEGORY_STATUS_ENABLED);

        LocalDateTime now = LocalDateTime.now();
        category.setCreateTime(now);
        category.setUpdateTime(now);
        Long adminId = BaseContext.getCurrentId();
        category.setCreateUser(adminId);
        category.setUpdateUser(adminId);

        categoryMapper.insert(category);

        //分类数据更改后必须清除缓存中的脏数据，防止用户读到脏数据
        clearCategoryCache();
        log.info("新增分类成功，categoryId：{}，name：{}，操作人：{}", category.getId(), name, adminId);
        return category.getId();
    }

    /**
     * 修改分类名称或排序值
     * @param categoryDTO 分类信息
     */
    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        if (categoryDTO.getId() == null) {
            throw categoryNotExist();
        }
        //分类必须已存在
        if (categoryMapper.findById(categoryDTO.getId()) == null) {
            throw categoryNotExist();
        }

        Category category = new Category();
        category.setId(categoryDTO.getId());
        //传入名称时做格式与唯一性校验（排除自身）
        if (StringUtils.hasText(categoryDTO.getName())) {
            String name = checkName(categoryDTO.getName());
            Category sameNameCategory = categoryMapper.findByName(name);
            if (sameNameCategory != null && !sameNameCategory.getId().equals(categoryDTO.getId())) {
                throw categoryNameAlreadyExist();
            }
            category.setName(name);
        }
        category.setSort(categoryDTO.getSort());
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);
        //分类数据更改后必须清除缓存中的脏数据，防止用户读到脏数据
        clearCategoryCache();
        log.info("修改分类成功，categoryId：{}，操作人：{}", categoryDTO.getId(), BaseContext.getCurrentId());
    }

    /**
     * 启用/禁用分类
     * @param id     分类ID
     * @param status 状态：1启用 0禁用
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        if (!NumberConstant.CATEGORY_STATUS_ENABLED.equals(status)
                && !NumberConstant.CATEGORY_STATUS_DISABLED.equals(status)) {
            throw new CategoryException(MessageConstant.CATEGORY_STATUS_ERROR);
        }
        if (categoryMapper.findById(id) == null) {
            throw categoryNotExist();
        }

        //禁用前校验分类下是否还有在售商品，避免用户端出现无分类可归的在售商品
        if (NumberConstant.CATEGORY_STATUS_DISABLED.equals(status)) {
            int sellingCount = productMapper.countByCategoryIdAndStatus(id, NumberConstant.PRODUCT_STATUS_SELLING);
            if (sellingCount > 0) {
                throw new CategoryException(MessageConstant.CATEGORY_NOT_ALLOW_DISABLE,
                        ErrorCodeConstant.CATEGORY_HAS_PRODUCT);
            }
        }

        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);

        //分类数据更改后必须清除缓存中的脏数据，防止用户读到脏数据
        clearCategoryCache();
        log.info("修改分类状态成功，categoryId：{}，status：{}，操作人：{}", id, status, BaseContext.getCurrentId());
    }

    /**
     * 根据id删除分类
     * @param id 分类ID
     */
    @Override
    public void deleteById(Long id) {
        if (categoryMapper.findById(id) == null) {
            throw categoryNotExist();
        }
        //分类下仍有商品时不允许删除，避免商品失去分类归属
        int productCount = productMapper.countByCategoryId(id);
        if (productCount > 0) {
            throw new CategoryException(MessageConstant.CATEGORY_HAS_PRODUCT,
                    ErrorCodeConstant.CATEGORY_HAS_PRODUCT);
        }

        categoryMapper.deleteById(id);

        //分类数据更改后必须清除缓存中的脏数据，防止用户读到脏数据
        clearCategoryCache();
        log.info("删除分类成功，categoryId：{}，操作人：{}", id, BaseContext.getCurrentId());
    }

    /**
     * 清除分类列表缓存
     * 用户端分类列表走 Redis 缓存，管理端改动后必须失效，否则用户端会读到过期数据；
     * 缓存清除失败不影响已落库的分类改动，仅记录错误日志，避免写入成功后给前端返回失败
     */
    private void clearCategoryCache() {
        try {
            stringRedisTemplate.delete(StringConstant.CATEGORY_LIST_PREFIX);
        } catch (Exception ex) {
            log.error("分类列表缓存清除失败，缓存键：{}，请检查 Redis 是否可用", StringConstant.CATEGORY_LIST_PREFIX, ex);
        }
    }

    /**
     * 校验分类名称并去除首尾空格
     * @param name 分类名称
     * @return 去除首尾空格后的分类名称
     */
    private String checkName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new CategoryException(MessageConstant.CATEGORY_NAME_REQUIRED);
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > NumberConstant.CATEGORY_NAME_MAX_LENGTH) {
            throw new CategoryException(MessageConstant.CATEGORY_NAME_LENGTH_ERROR);
        }
        return trimmedName;
    }

    /**
     * 构造分类不存在异常
     *
     * @return 分类模块业务异常
     */
    private CategoryException categoryNotExist() {
        return new CategoryException(MessageConstant.CATEGORY_NOT_EXIST, ErrorCodeConstant.CATEGORY_NOT_EXIST);
    }

    /**
     * 构造分类名称已存在异常
     *
     * @return 分类模块业务异常
     */
    private CategoryException categoryNameAlreadyExist() {
        return new CategoryException(MessageConstant.CATEGORY_NAME_ALREADY_EXIST,
                ErrorCodeConstant.CATEGORY_NAME_ALREADY_EXIST);
    }

    /**
     * 实体转列表 VO
     *
     * @param category 分类实体
     * @return 分类列表数据
     */
    private CategoryVO toCategoryVO(Category category) {
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }
}
