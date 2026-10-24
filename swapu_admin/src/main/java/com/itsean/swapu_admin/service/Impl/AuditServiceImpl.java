package com.itsean.swapu_admin.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itsean.pojo.PageResult;
import com.itsean.swapu_admin.constant.ErrorCodeConstant;
import com.itsean.swapu_admin.constant.MessageConstant;
import com.itsean.swapu_admin.constant.NumberConstant;
import com.itsean.swapu_admin.context.BaseContext;
import com.itsean.swapu_admin.dto.AuditRecordPageQueryDTO;
import com.itsean.swapu_admin.dto.CommentAuditPageQueryDTO;
import com.itsean.swapu_admin.dto.CommentReviewDTO;
import com.itsean.swapu_admin.dto.ProductAuditPageQueryDTO;
import com.itsean.swapu_admin.dto.ProductReviewDTO;
import com.itsean.swapu_admin.entity.AuditRecord;
import com.itsean.swapu_admin.entity.Comment;
import com.itsean.swapu_admin.entity.Product;
import com.itsean.swapu_admin.entity.User;
import com.itsean.swapu_admin.exception.AuditException;
import com.itsean.swapu_admin.mapper.AuditRecordMapper;
import com.itsean.swapu_admin.mapper.CommentMapper;
import com.itsean.swapu_admin.mapper.ProductMapper;
import com.itsean.swapu_admin.mapper.UserMapper;
import com.itsean.swapu_admin.service.AuditService;
import com.itsean.swapu_admin.vo.AuditRecordVO;
import com.itsean.swapu_admin.vo.CommentAuditVO;
import com.itsean.swapu_admin.vo.ProductAuditDetailVO;
import com.itsean.swapu_admin.vo.ProductAuditVO;
import com.itsean.swapu_admin.vo.SellerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 审核管理业务实现（管理端）
 */
@Service
@Slf4j
public class AuditServiceImpl implements AuditService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private AuditRecordMapper auditRecordMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询商品审核列表
     *
     * @param productAuditPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryProducts(ProductAuditPageQueryDTO productAuditPageQueryDTO) {
        normalizePage(productAuditPageQueryDTO);

        //审核列表默认查询待审核商品
        if (productAuditPageQueryDTO.getStatus() == null) {
            productAuditPageQueryDTO.setStatus(NumberConstant.PRODUCT_STATUS_CHECKED);
        }
        validateProductAuditStatus(productAuditPageQueryDTO.getStatus());

        log.info("分页查询商品审核列表：{}", productAuditPageQueryDTO);
        PageHelper.startPage(productAuditPageQueryDTO.getPageNum(), productAuditPageQueryDTO.getPageSize());

        List<ProductAuditVO> productList = productMapper.auditPageQuery(productAuditPageQueryDTO);
        //图片列表统一返回数组，避免前端对 null 做额外判空
        productList.forEach(product -> {
            if (product.getImages() == null) {
                product.setImages(Collections.emptyList());
            }
        });
        Page<ProductAuditVO> page = (Page<ProductAuditVO>) productList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 查询商品审核详情（商品完整信息 + 发布人信息 + 历史审核流水）
     *
     * @param id 商品ID
     * @return 商品审核详情
     */
    @Override
    public ProductAuditDetailVO getProductAuditDetail(Long id) {
        log.info("查询商品审核详情：id={}", id);
        Product product = productMapper.getProductById(id);
        if (product == null) {
            throw productNotExist();
        }

        //发布人信息：用户被删除时 seller 为空，不影响商品详情展示
        SellerVO seller = null;
        if (product.getUserId() != null) {
            User user = userMapper.findById(product.getUserId());
            if (user != null) {
                seller = new SellerVO();
                BeanUtils.copyProperties(user, seller);
            }
        }

        ProductAuditDetailVO detailVO = new ProductAuditDetailVO();
        detailVO.setProduct(product);
        detailVO.setSeller(seller);
        detailVO.setAuditRecords(listAuditRecords(NumberConstant.AUDIT_TYPE_PRODUCT, id));
        return detailVO;
    }

    /**
     * 审核商品（通过/驳回）
     *
     * @param productReviewDTO 审核参数
     */
    @Override
    @Transactional
    public void reviewProduct(ProductReviewDTO productReviewDTO) {
        if (productReviewDTO == null || productReviewDTO.getProductId() == null) {
            throw new AuditException(MessageConstant.AUDIT_PRODUCT_ID_REQUIRED, ErrorCodeConstant.AUDIT_PARAM_ERROR);
        }
        validateReviewResult(productReviewDTO.getResult());

        Product product = productMapper.getProductById(productReviewDTO.getProductId());
        if (product == null) {
            throw productNotExist();
        }

        //驳回必须填写原因；通过时原因不落库，避免无意义数据
        String reason = normalizeReason(productReviewDTO.getResult(), productReviewDTO.getReason());

        //只有待审核商品允许审核，防止已上架/已驳回商品被重复审核
        if (product.getStatus() == null || product.getStatus() != NumberConstant.PRODUCT_STATUS_CHECKED) {
            throw productStatusError();
        }

        Integer targetStatus = productReviewDTO.getResult() == NumberConstant.AUDIT_RESULT_PASS
                ? NumberConstant.PRODUCT_STATUS_SELLING
                : NumberConstant.PRODUCT_STATUS_AUDIT_REJECTED;

        //带状态条件的更新：并发下只有一个请求能成功流转
        int rows = productMapper.updateStatusFromExpected(productReviewDTO.getProductId(), targetStatus,
                NumberConstant.PRODUCT_STATUS_CHECKED, LocalDateTime.now());
        if (rows == 0) {
            throw productStatusError();
        }

        saveAuditRecord(NumberConstant.AUDIT_TYPE_PRODUCT, productReviewDTO.getProductId(),
                productReviewDTO.getResult(), reason);
        log.info("商品审核完成，productId：{}，result：{}，reviewerId：{}",
                productReviewDTO.getProductId(), productReviewDTO.getResult(), BaseContext.getCurrentId());
    }

    /**
     * 分页查询评论审核列表
     *
     * @param commentAuditPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryComments(CommentAuditPageQueryDTO commentAuditPageQueryDTO) {
        normalizePage(commentAuditPageQueryDTO);

        //审核列表默认查询待审核评论
        if (commentAuditPageQueryDTO.getStatus() == null) {
            commentAuditPageQueryDTO.setStatus(NumberConstant.COMMENT_STATUS_PENDING);
        }
        validateCommentAuditStatus(commentAuditPageQueryDTO.getStatus());

        log.info("分页查询评论审核列表：{}", commentAuditPageQueryDTO);
        PageHelper.startPage(commentAuditPageQueryDTO.getPageNum(), commentAuditPageQueryDTO.getPageSize());

        List<CommentAuditVO> commentList = commentMapper.auditPageQuery(commentAuditPageQueryDTO);
        //图片列表统一返回数组，避免前端对 null 做额外判空
        commentList.forEach(comment -> {
            if (comment.getImages() == null) {
                comment.setImages(Collections.emptyList());
            }
        });
        Page<CommentAuditVO> page = (Page<CommentAuditVO>) commentList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 审核评论（通过/驳回）
     *
     * @param commentReviewDTO 审核参数
     */
    @Override
    @Transactional
    public void reviewComment(CommentReviewDTO commentReviewDTO) {
        if (commentReviewDTO == null || commentReviewDTO.getCommentId() == null) {
            throw new AuditException(MessageConstant.AUDIT_COMMENT_ID_REQUIRED, ErrorCodeConstant.AUDIT_PARAM_ERROR);
        }
        validateReviewResult(commentReviewDTO.getResult());

        Comment comment = commentMapper.findById(commentReviewDTO.getCommentId());
        if (comment == null) {
            throw new AuditException(MessageConstant.AUDIT_COMMENT_NOT_EXIST, ErrorCodeConstant.AUDIT_TARGET_NOT_EXIST);
        }

        //驳回必须填写原因；通过时原因不落库
        String reason = normalizeReason(commentReviewDTO.getResult(), commentReviewDTO.getReason());

        //只有待审核评论允许审核
        if (comment.getStatus() == null || comment.getStatus() != NumberConstant.COMMENT_STATUS_PENDING) {
            throw commentStatusError();
        }

        Integer targetStatus = commentReviewDTO.getResult() == NumberConstant.AUDIT_RESULT_PASS
                ? NumberConstant.COMMENT_STATUS_PUBLISHED
                : NumberConstant.COMMENT_STATUS_REJECTED;

        //带状态条件的更新：并发下只有一个请求能成功流转
        int rows = commentMapper.updateStatusFromExpected(commentReviewDTO.getCommentId(), targetStatus,
                NumberConstant.COMMENT_STATUS_PENDING);
        if (rows == 0) {
            throw commentStatusError();
        }

        saveAuditRecord(NumberConstant.AUDIT_TYPE_COMMENT, commentReviewDTO.getCommentId(),
                commentReviewDTO.getResult(), reason);
        log.info("评论审核完成，commentId：{}，result：{}，reviewerId：{}",
                commentReviewDTO.getCommentId(), commentReviewDTO.getResult(), BaseContext.getCurrentId());
    }

    /**
     * 按条件分页查询审核流水
     *
     * @param auditRecordPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQueryRecords(AuditRecordPageQueryDTO auditRecordPageQueryDTO) {
        normalizePage(auditRecordPageQueryDTO);
        validateRecordFilters(auditRecordPageQueryDTO);

        log.info("分页查询审核流水：{}", auditRecordPageQueryDTO);
        PageHelper.startPage(auditRecordPageQueryDTO.getPageNum(), auditRecordPageQueryDTO.getPageSize());

        List<AuditRecordVO> recordList = auditRecordMapper.pageQuery(auditRecordPageQueryDTO);
        Page<AuditRecordVO> page = (Page<AuditRecordVO>) recordList;
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 写入审核流水
     *
     * @param type     审核类型：1商品 2评论 3头像
     * @param targetId 被审对象ID
     * @param result   审核结论：1通过 2驳回
     * @param reason   驳回原因（通过时为 null）
     */
    private void saveAuditRecord(Integer type, Long targetId, Integer result, String reason) {
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setType(type);
        auditRecord.setTargetId(targetId);
        auditRecord.setResult(result);
        auditRecord.setReason(reason);
        //AI 审核接入前人工审核流水不写 AI 结论
        auditRecord.setAiResult(null);
        auditRecord.setReviewerId(BaseContext.getCurrentId());
        auditRecord.setReviewTime(LocalDateTime.now());
        auditRecordMapper.insert(auditRecord);
    }

    /**
     * 查询某个被审对象的历史审核流水
     *
     * @param type     审核类型
     * @param targetId 被审对象ID
     * @return 审核流水列表
     */
    private List<AuditRecordVO> listAuditRecords(Integer type, Long targetId) {
        return auditRecordMapper.listByTarget(type, targetId);
    }

    /**
     * 规范化分页参数，避免 PageHelper 收到非法值
     */
    private void normalizePage(ProductAuditPageQueryDTO dto) {
        if (dto.getPageNum() == null || dto.getPageNum() < 1) {
            dto.setPageNum(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
    }

    /**
     * 规范化分页参数，避免 PageHelper 收到非法值
     */
    private void normalizePage(CommentAuditPageQueryDTO dto) {
        if (dto.getPageNum() == null || dto.getPageNum() < 1) {
            dto.setPageNum(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
    }

    /**
     * 规范化分页参数，避免 PageHelper 收到非法值
     */
    private void normalizePage(AuditRecordPageQueryDTO dto) {
        if (dto.getPageNum() == null || dto.getPageNum() < 1) {
            dto.setPageNum(1);
        }
        if (dto.getPageSize() == null || dto.getPageSize() < 1) {
            dto.setPageSize(10);
        }
    }

    /**
     * 校验商品审核列表状态筛选值
     *
     * @param status 审核状态
     */
    private void validateProductAuditStatus(Integer status) {
        if (status != NumberConstant.PRODUCT_STATUS_CHECKED
                && status != NumberConstant.PRODUCT_STATUS_SELLING
                && status != NumberConstant.PRODUCT_STATUS_AUDIT_REJECTED) {
            throw new AuditException(MessageConstant.AUDIT_STATUS_FILTER_ERROR, ErrorCodeConstant.AUDIT_PARAM_ERROR);
        }
    }

    /**
     * 校验评论审核列表状态筛选值
     *
     * @param status 审核状态
     */
    private void validateCommentAuditStatus(Integer status) {
        if (status != NumberConstant.COMMENT_STATUS_PENDING
                && status != NumberConstant.COMMENT_STATUS_PUBLISHED
                && status != NumberConstant.COMMENT_STATUS_REJECTED) {
            throw new AuditException(MessageConstant.AUDIT_STATUS_FILTER_ERROR, ErrorCodeConstant.AUDIT_PARAM_ERROR);
        }
    }

    /**
     * 校验审核结论取值
     *
     * @param result 审核结论：1通过 2驳回
     */
    private void validateReviewResult(Integer result) {
        if (result == null
                || (result != NumberConstant.AUDIT_RESULT_PASS && result != NumberConstant.AUDIT_RESULT_REJECT)) {
            throw new AuditException(MessageConstant.AUDIT_RESULT_ERROR, ErrorCodeConstant.AUDIT_PARAM_ERROR);
        }
    }

    /**
     * 规范化驳回原因：驳回必填且不超过 500 字符，通过时置空
     *
     * @param result 审核结论
     * @param reason 原始驳回原因
     * @return 落库的驳回原因
     */
    private String normalizeReason(Integer result, String reason) {
        if (result != NumberConstant.AUDIT_RESULT_REJECT) {
            return null;
        }
        String trimmedReason = reason == null ? null : reason.trim();
        if (trimmedReason == null || trimmedReason.isEmpty()) {
            throw new AuditException(MessageConstant.AUDIT_REASON_REQUIRED, ErrorCodeConstant.AUDIT_REASON_REQUIRED);
        }
        if (trimmedReason.length() > NumberConstant.AUDIT_REASON_MAX_LENGTH) {
            throw new AuditException(MessageConstant.AUDIT_REASON_TOO_LONG, ErrorCodeConstant.AUDIT_PARAM_ERROR);
        }
        return trimmedReason;
    }

    /**
     * 校验审核流水筛选值
     *
     * @param dto 分页查询条件
     */
    private void validateRecordFilters(AuditRecordPageQueryDTO dto) {
        if (dto.getType() != null
                && dto.getType() != NumberConstant.AUDIT_TYPE_PRODUCT
                && dto.getType() != NumberConstant.AUDIT_TYPE_COMMENT
                && dto.getType() != NumberConstant.AUDIT_TYPE_AVATAR) {
            throw new AuditException(MessageConstant.AUDIT_TYPE_FILTER_ERROR, ErrorCodeConstant.AUDIT_PARAM_ERROR);
        }
        if (dto.getResult() != null
                && dto.getResult() != NumberConstant.AUDIT_RESULT_PASS
                && dto.getResult() != NumberConstant.AUDIT_RESULT_REJECT) {
            throw new AuditException(MessageConstant.AUDIT_RESULT_ERROR, ErrorCodeConstant.AUDIT_PARAM_ERROR);
        }
    }

    /**
     * 构造商品不存在异常
     *
     * @return 审核模块业务异常
     */
    private AuditException productNotExist() {
        return new AuditException(MessageConstant.AUDIT_PRODUCT_NOT_EXIST, ErrorCodeConstant.AUDIT_TARGET_NOT_EXIST);
    }

    /**
     * 构造商品状态不可审核异常
     *
     * @return 审核模块业务异常
     */
    private AuditException productStatusError() {
        return new AuditException(MessageConstant.AUDIT_PRODUCT_STATUS_ERROR, ErrorCodeConstant.AUDIT_STATUS_ERROR);
    }

    /**
     * 构造评论状态不可审核异常
     *
     * @return 审核模块业务异常
     */
    private AuditException commentStatusError() {
        return new AuditException(MessageConstant.AUDIT_COMMENT_STATUS_ERROR, ErrorCodeConstant.AUDIT_STATUS_ERROR);
    }

}
