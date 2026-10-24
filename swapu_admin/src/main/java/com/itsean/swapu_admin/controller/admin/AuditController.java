package com.itsean.swapu_admin.controller.admin;

import com.itsean.pojo.PageResult;
import com.itsean.pojo.Result;
import com.itsean.swapu_admin.dto.AuditRecordPageQueryDTO;
import com.itsean.swapu_admin.dto.CommentAuditPageQueryDTO;
import com.itsean.swapu_admin.dto.CommentReviewDTO;
import com.itsean.swapu_admin.dto.ProductAuditPageQueryDTO;
import com.itsean.swapu_admin.dto.ProductReviewDTO;
import com.itsean.swapu_admin.service.AuditService;
import com.itsean.swapu_admin.vo.ProductAuditDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审核管理接口（管理端）
 */
@RestController
@RequestMapping("/admin/audit")
@Slf4j
@Api(tags = "审核管理接口")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * 商品审核-分页列表
     *
     * @param productAuditPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/product/page")
    @ApiOperation("商品审核-分页列表")
    public Result<PageResult> pageQueryProducts(ProductAuditPageQueryDTO productAuditPageQueryDTO) {
        log.info("商品审核分页查询：{}", productAuditPageQueryDTO);
        PageResult pageResult = auditService.pageQueryProducts(productAuditPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 商品审核-详情
     *
     * @param id 商品ID
     * @return 商品完整信息 + 发布人信息 + 历史审核流水
     */
    @GetMapping("/product/{id}")
    @ApiOperation("商品审核-详情")
    public Result<ProductAuditDetailVO> getProductAuditDetail(
            @ApiParam(value = "商品ID", example = "10") @PathVariable Long id) {
        log.info("商品审核详情查询：id={}", id);
        ProductAuditDetailVO detailVO = auditService.getProductAuditDetail(id);
        return Result.success(detailVO);
    }

    /**
     * 商品审核-通过/驳回
     *
     * @param productReviewDTO 审核参数
     * @return 操作结果
     */
    @PostMapping("/product/review")
    @ApiOperation("商品审核-通过/驳回")
    public Result reviewProduct(@RequestBody ProductReviewDTO productReviewDTO) {
        log.info("商品审核：{}", productReviewDTO);
        auditService.reviewProduct(productReviewDTO);
        return Result.success();
    }

    /**
     * 评论审核-分页列表
     *
     * @param commentAuditPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/comment/page")
    @ApiOperation("评论审核-分页列表")
    public Result<PageResult> pageQueryComments(CommentAuditPageQueryDTO commentAuditPageQueryDTO) {
        log.info("评论审核分页查询：{}", commentAuditPageQueryDTO);
        PageResult pageResult = auditService.pageQueryComments(commentAuditPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 评论审核-通过/驳回
     *
     * @param commentReviewDTO 审核参数
     * @return 操作结果
     */
    @PostMapping("/comment/review")
    @ApiOperation("评论审核-通过/驳回")
    public Result reviewComment(@RequestBody CommentReviewDTO commentReviewDTO) {
        log.info("评论审核：{}", commentReviewDTO);
        auditService.reviewComment(commentReviewDTO);
        return Result.success();
    }

    /**
     * 审核流水查询
     *
     * @param auditRecordPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/record/page")
    @ApiOperation("审核流水查询")
    public Result<PageResult> pageQueryRecords(AuditRecordPageQueryDTO auditRecordPageQueryDTO) {
        log.info("审核流水分页查询：{}", auditRecordPageQueryDTO);
        PageResult pageResult = auditService.pageQueryRecords(auditRecordPageQueryDTO);
        return Result.success(pageResult);
    }

}
