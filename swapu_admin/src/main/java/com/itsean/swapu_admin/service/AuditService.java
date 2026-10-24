package com.itsean.swapu_admin.service;

import com.itsean.pojo.PageResult;
import com.itsean.swapu_admin.dto.AuditRecordPageQueryDTO;
import com.itsean.swapu_admin.dto.CommentAuditPageQueryDTO;
import com.itsean.swapu_admin.dto.CommentReviewDTO;
import com.itsean.swapu_admin.dto.ProductAuditPageQueryDTO;
import com.itsean.swapu_admin.dto.ProductReviewDTO;
import com.itsean.swapu_admin.vo.ProductAuditDetailVO;

/**
 * 审核管理业务接口（管理端）
 */
public interface AuditService {

    /**
     * 分页查询商品审核列表
     *
     * @param productAuditPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQueryProducts(ProductAuditPageQueryDTO productAuditPageQueryDTO);

    /**
     * 查询商品审核详情
     *
     * @param id 商品ID
     * @return 商品完整信息 + 发布人信息 + 历史审核流水
     */
    ProductAuditDetailVO getProductAuditDetail(Long id);

    /**
     * 审核商品（通过/驳回）
     *
     * @param productReviewDTO 审核参数
     */
    void reviewProduct(ProductReviewDTO productReviewDTO);

    /**
     * 分页查询评论审核列表
     *
     * @param commentAuditPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQueryComments(CommentAuditPageQueryDTO commentAuditPageQueryDTO);

    /**
     * 审核评论（通过/驳回）
     *
     * @param commentReviewDTO 审核参数
     */
    void reviewComment(CommentReviewDTO commentReviewDTO);

    /**
     * 按条件分页查询审核流水
     *
     * @param auditRecordPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult pageQueryRecords(AuditRecordPageQueryDTO auditRecordPageQueryDTO);

}
