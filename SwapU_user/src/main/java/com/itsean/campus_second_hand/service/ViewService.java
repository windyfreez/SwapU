package com.itsean.campus_second_hand.service;

import com.itsean.pojo.PageResult;
import org.springframework.stereotype.Service;

@Service
public interface ViewService {
    /**
     * 分页获取当前用户浏览过的商品
     * @param pageSize,pageNum
     * @return
     */
    PageResult listView(int pageNum, int pageSize);
}
