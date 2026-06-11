package com.itsean.campus_second_hand.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.dto.FavoriteDTO;
import com.itsean.campus_second_hand.dto.FavoritePageQueryDTO;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.entity.result.PageResult;
import com.itsean.campus_second_hand.mapper.FavoriteMapper;
import com.itsean.campus_second_hand.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    /**
     * 添加收藏
     * @param favoriteDTO
     */
    @Override
    public void add(FavoriteDTO favoriteDTO) {
        log.info("当前用户id：{}", BaseContext.getCurrentId());
        favoriteDTO.setUserId(BaseContext.getCurrentId());
        favoriteDTO.setCreateTime(LocalDateTime.now());
        favoriteMapper.add(favoriteDTO);
    }

    /**
     * 取消收藏
     * @param productId
     */
    @Override
    public void cancel(Long productId) {
        Long userId = BaseContext.getCurrentId();
        log.info("当前用户id：{}", userId);
        favoriteMapper.cancel(productId,userId);
    }

    /**
     * 获取收藏列表
     * @param favoritePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(FavoritePageQueryDTO favoritePageQueryDTO) {
        PageHelper.startPage(favoritePageQueryDTO.getPage(),favoritePageQueryDTO.getPageSize());

        Long userId = BaseContext.getCurrentId();
        log.info("当前用户id：{}", BaseContext.getCurrentId());

        List<Product> products = favoriteMapper.pageQuery(favoritePageQueryDTO,userId);

        products.forEach(product -> {
            List<String> images = product.getImages();
            String firstImage = images.get(0);
            product.setImages(Collections.singletonList(firstImage));
        });

        Page<Product> page = (Page<Product>) products;
        return new PageResult(page.getTotal(), page.getResult());
    }
}
