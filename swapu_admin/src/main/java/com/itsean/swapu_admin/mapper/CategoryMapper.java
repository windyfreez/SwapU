package com.itsean.swapu_admin.mapper;

import com.itsean.swapu_admin.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 获取所有启用的分类（用户端）
     * @return
     */
    List<Category> list();

    /**
     * 获取全部分类，含停用（管理端）
     *
     * @return 分类列表，按 sort 升序
     */
    List<Category> listAll();

    /**
     * 根据id查询分类信息
     *
     * @param id 分类ID
     * @return 分类信息，不存在时返回 null
     */
    @Select("select * from category where id = #{id}")
    @ResultMap("CategoryResultMap")
    Category findById(Long id);

    /**
     * 根据名称查询分类，用于分类名称唯一性校验
     *
     * @param name 分类名称
     * @return 分类信息，不存在时返回 null
     */
    @Select("select * from category where name = #{name}")
    @ResultMap("CategoryResultMap")
    Category findByName(String name);

    /**
     * 新增分类，并回填自增主键
     *
     * @param category 分类信息
     */
    void insert(Category category);

    /**
     * 动态修改分类信息（只更新非 null 字段）
     *
     * @param category 分类信息
     */
    void update(Category category);

    /**
     * 根据id删除分类
     *
     * @param id 分类ID
     */
    @Delete("delete from category where id = #{id}")
    void deleteById(Long id);

}
