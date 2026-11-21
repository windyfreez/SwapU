package com.itsean.swapu_admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.itsean.common.context.BaseContext;
import com.itsean.pojo.PageResult;
import com.itsean.pojo.entity.Admin;
import com.itsean.swapu_admin.constant.AdminLogAction;
import com.itsean.swapu_admin.dto.AdminOperationLogPageQueryDTO;
import com.itsean.swapu_admin.entity.AdminOperationLog;
import com.itsean.swapu_admin.mapper.AdminMapper;
import com.itsean.swapu_admin.mapper.AdminOperationLogMapper;
import com.itsean.swapu_admin.vo.AdminOperationLogVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理员操作日志服务测试
 */
@ExtendWith(MockitoExtension.class)
class AdminOperationLogServiceTest {

    private static final Long ADMIN_ID = 1L;

    @Mock
    private AdminOperationLogMapper adminOperationLogMapper;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private AdminOperationLogServiceImpl adminOperationLogService;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(ADMIN_ID);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
        // pageQuery 里调用了 PageHelper.startPage，清掉线程变量避免污染其他用例
        PageHelper.clearPage();
    }

    @Test
    @DisplayName("没有登录上下文时不记录日志（如定时任务线程）")
    void skipWhenNoLoginContext() {
        BaseContext.removeCurrentId();

        adminOperationLogService.record(AdminLogAction.CREATE, "ADMIN", 5L, "username=bob");

        verify(adminOperationLogMapper, never()).insert(any(AdminOperationLog.class));
    }

    @Test
    @DisplayName("记录日志：写入操作人快照、动作、对象与摘要")
    void recordBuildsLog() {
        Admin operator = new Admin();
        operator.setId(ADMIN_ID);
        operator.setUsername("root");
        when(adminMapper.findById(ADMIN_ID)).thenReturn(operator);

        adminOperationLogService.record(AdminLogAction.STATUS, "ADMIN", 5L, "status:1->0");

        ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
        verify(adminOperationLogMapper).insert(captor.capture());

        AdminOperationLog saved = captor.getValue();
        assertEquals(ADMIN_ID, saved.getAdminId());
        assertEquals("root", saved.getAdminUsername());
        assertEquals("STATUS", saved.getAction());
        assertEquals("ADMIN", saved.getTargetType());
        assertEquals(5L, saved.getTargetId());
        assertEquals("status:1->0", saved.getDetail());
        assertNotNull(saved.getCreateTime());
        // 非 Web 线程取不到请求，IP 为空是预期行为
        assertNull(saved.getIp());
    }

    @Test
    @DisplayName("日志写入失败不阻断业务（异常被吞掉并打错误日志）")
    void recordSwallowsFailure() {
        when(adminMapper.findById(ADMIN_ID)).thenReturn(new Admin());
        doThrow(new RuntimeException("日志表不存在"))
                .when(adminOperationLogMapper).insert(any(AdminOperationLog.class));

        assertDoesNotThrow(() ->
                adminOperationLogService.record(AdminLogAction.DELETE, "ADMIN", 5L, "username=bob"));
    }

    @Test
    @DisplayName("超长摘要被截断到 500 字符，避免整条日志写入失败")
    void recordTruncatesLongDetail() {
        when(adminMapper.findById(ADMIN_ID)).thenReturn(new Admin());
        String longDetail = "x".repeat(600);

        adminOperationLogService.record(AdminLogAction.UPDATE, "ADMIN", 5L, longDetail);

        ArgumentCaptor<AdminOperationLog> captor = ArgumentCaptor.forClass(AdminOperationLog.class);
        verify(adminOperationLogMapper).insert(captor.capture());
        assertEquals(500, captor.getValue().getDetail().length());
        assertTrue(captor.getValue().getDetail().endsWith("..."));
    }

    @Test
    @DisplayName("分页查询：补默认分页参数，并把动作码翻译成中文描述")
    void pageQueryNormalizesAndTranslates() {
        AdminOperationLogVO vo = new AdminOperationLogVO();
        vo.setId(1L);
        vo.setAction("CREATE");
        List<AdminOperationLogVO> rows = new ArrayList<>();
        rows.add(vo);
        when(adminOperationLogMapper.pageQuery(any(AdminOperationLogPageQueryDTO.class))).thenReturn(rows);

        AdminOperationLogPageQueryDTO queryDTO = new AdminOperationLogPageQueryDTO();
        PageResult pageResult = adminOperationLogService.pageQuery(queryDTO);

        // 缺省分页参数被补成 1 / 10
        assertEquals(1, queryDTO.getPageNum());
        assertEquals(10, queryDTO.getPageSize());
        assertEquals(1, pageResult.getTotal());
        assertEquals("新增管理员", vo.getActionDesc());
    }

    @Test
    @DisplayName("分页查询：pageSize 超过 100 被压到 100")
    void pageQueryCapsPageSize() {
        when(adminOperationLogMapper.pageQuery(any(AdminOperationLogPageQueryDTO.class)))
                .thenReturn(new ArrayList<>());

        AdminOperationLogPageQueryDTO queryDTO = new AdminOperationLogPageQueryDTO();
        queryDTO.setPageNum(2);
        queryDTO.setPageSize(999);
        adminOperationLogService.pageQuery(queryDTO);

        assertEquals(2, queryDTO.getPageNum());
        assertEquals(100, queryDTO.getPageSize());
    }
}
