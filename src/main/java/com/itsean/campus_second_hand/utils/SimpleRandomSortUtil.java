package com.itsean.campus_second_hand.utils;

import com.itsean.campus_second_hand.entity.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 商品分页查询随机排序工作类
 */
public class SimpleRandomSortUtil {

    public static List<Product> weightedRandomSort(List<Product> products) {
        if (products == null || products.size() <= 10) {
            return products;
        }

        List<Product> sorted = new ArrayList<>(products);
        sorted.sort((p1, p2) -> {
            int view1 = p1.getViewCount() != null ? p1.getViewCount() : 0;
            int view2 = p2.getViewCount() != null ? p2.getViewCount() : 0;
            return Integer.compare(view2, view1);
        });

        int topSize = Math.min(sorted.size() / 3, 20);
        List<Product> topProducts = sorted.subList(0, topSize);
        List<Product> restProducts = sorted.subList(topSize, sorted.size());

        Collections.shuffle(topProducts, new Random());

        if (!restProducts.isEmpty()) {
            Collections.shuffle(restProducts, new Random());
        }

        List<Product> result = new ArrayList<>();
        result.addAll(topProducts);
        result.addAll(restProducts);

        return result;
    }
}
