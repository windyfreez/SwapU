package com.itsean.campus_second_hand.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.entity.Product;
import com.itsean.campus_second_hand.mapper.ProductMapper;
import com.itsean.campus_second_hand.mapper.UserBehaviorLogMapper;
import com.itsean.campus_second_hand.service.ViewService;
import com.itsean.pojo.PageResult;
import com.itsean.pojo.entity.UserBehaviorLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ViewServiceImpl implements ViewService {

    @Autowired
    private UserBehaviorLogMapper userBehaviorLogMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 分页获取当前用户浏览过的商品
     * @return
     */
    @Override
    public PageResult listView(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Long currentId = BaseContext.getCurrentId();
        log.info("当前ID:{}", currentId);

        List<UserBehaviorLog> behaviorLogList = userBehaviorLogMapper.batchList(currentId);

        //用 PageInfo 获取分页的统计信息
        PageInfo<UserBehaviorLog> pageInfo = new PageInfo<>(behaviorLogList);

        //将行为日志列表转换为 Product 列表
        List<Product> productList = new ArrayList<>();
        for (UserBehaviorLog behaviorLog : behaviorLogList) {
            Product product = productMapper.getProductById(behaviorLog.getProductId());
            if (product != null) {
                productList.add(product);
            }
        }

        //使用 PageInfo 的 total 和转换后的 productList 构造返回结果
        return new PageResult(pageInfo.getTotal(), productList);
    }
}
