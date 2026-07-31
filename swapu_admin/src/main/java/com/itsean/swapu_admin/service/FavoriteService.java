package com.itsean.swapu_admin.service;

import com.itsean.swapu_admin.dto.FavoriteDTO;
import com.itsean.swapu_admin.dto.FavoritePageQueryDTO;
import com.itsean.swapu_admin.entity.result.PageResult;
import org.springframework.stereotype.Service;

@Service
public interface FavoriteService {

    /**
     * 添加收藏
     * @param favoriteDTO
     */
    void add(FavoriteDTO favoriteDTO);

    /**
     * 取消收藏
     * @param productId
     */
    void cancel(Long productId);

    /**
     * 获取收藏列表
     * @param favoritePageQueryDTO
     * @return
     */
    PageResult pageQuery(FavoritePageQueryDTO favoritePageQueryDTO);
}
