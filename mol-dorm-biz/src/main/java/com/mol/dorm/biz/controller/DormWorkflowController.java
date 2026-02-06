package com.mol.dorm.biz.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.mol.common.core.util.R;
import com.mol.dorm.biz.entity.DormWorkflow;
import com.mol.dorm.biz.service.DormWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 宿舍业务流程控制器 (C端：学生/教工专用)
 * <p>
 * 核心逻辑：
 * 1. 提供入住、换房、互换、退宿、留校申请入口。
 * 2. 强制身份核验，所有申请均关联当前登录人。
 * </p>
 */
@Tag(name = "业务申请流程")
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class DormWorkflowController {
    
    private final DormWorkflowService workflowService;
    
    /**
     * 🛡️ 强制身份核验 (防刁民核心)
     * 无论前端传什么，userId、username 等关键字段必须在 Service 中根据数据库重新获取或在此处强制覆盖。
     */
    private void fillCurrentUser(DormWorkflow app) {
        // 强制绑定当前登录用户 ID，防止“水平越权”
        app.setUserId(StpUtil.getLoginIdAsLong());
    }
    
    @Operation(summary = "提交入住申请", description = "场景：新生报到、新入职教工、因事回校居住")
    @SaCheckLogin
    @PostMapping("/submit/check-in")
    public R<String> submitCheckIn(@RequestBody DormWorkflow app) {
        fillCurrentUser(app);
        // 显式指定类型，增强 Service 容错性
        app.setType(DormWorkflow.TYPE_CHECK_IN_NEW);
        workflowService.submitCheckIn(app);
        return R.ok("入住申请已提交，请留意驾驶舱审批状态");
    }
    
    @Operation(summary = "提交换房申请 (分配)", description = "场景：想换到其他房间的空床位，由宿管指定或随机分配")
    @SaCheckLogin
    @PostMapping("/submit/room-change")
    public R<String> submitRoomChange(@RequestBody DormWorkflow app) {
        fillCurrentUser(app);
        app.setType(DormWorkflow.TYPE_EXCHANGE_ADMIN);
        workflowService.submitRoomChange(app);
        return R.ok("换房申请已提交");
    }
    
    @Operation(summary = "提交互换申请 (找人)", description = "场景：学生A想与学生B互换位置（需同性别、同身份）")
    @SaCheckLogin
    @PostMapping("/submit/mutual-exchange")
    public R<String> submitMutualExchange(@RequestBody DormWorkflow app) {
        if (app.getTargetBedId() == null) {
            return R.fail("必须指定互换的目标床位ID");
        }
        fillCurrentUser(app);
        app.setType(DormWorkflow.TYPE_EXCHANGE_MUTUAL);
        workflowService.submitMutualExchange(app);
        return R.ok("互换申请已提交，等待管理员核准身份");
    }
    
    @Operation(summary = "提交退宿申请", description = "场景：正常离校、因病/因事休学退宿、毕业退宿")
    @SaCheckLogin
    @PostMapping("/submit/check-out")
    public R<String> submitCheckOut(@RequestBody DormWorkflow app) {
        fillCurrentUser(app);
        app.setType(DormWorkflow.TYPE_CHECK_OUT);
        workflowService.submitCheckOut(app);
        return R.ok("退宿申请已提交");
    }
    
    @Operation(summary = "提交假期留校申请", description = "注意：假期申请需填写紧急联系人及起止时间")
    @SaCheckLogin
    @PostMapping("/submit/holiday")
    public R<String> submitHoliday(@RequestBody DormWorkflow app) {
        fillCurrentUser(app);
        app.setType(DormWorkflow.TYPE_HOLIDAY_STAY);
        workflowService.submitHolidayStay(app);
        return R.ok("假期留校申请已提交，请确保联系电话通畅");
    }
    
}