package com.OA.system.service.impl;

import com.OA.system.dto.CompleteTaskRequest;
import com.OA.system.entity.LeaveApply;
import com.OA.system.dto.StartLeaveRequest;
import com.OA.system.entity.LocationRequest;
import com.OA.system.mapper.LeaveApplyMapper;
import com.OA.system.mapper.LocationRequestMapper;
import com.OA.system.repository.LeaveApplyRepository;
import com.OA.system.service.LeaveService;
import com.OA.system.service.LocationRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl extends ServiceImpl<LeaveApplyMapper, LeaveApply> implements LeaveService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final LeaveApplyRepository leaveApplyRepository;
    private final LeaveApplyMapper leaveApplyMapper;

    @Override
    @Transactional
    public LeaveApply submit(StartLeaveRequest req) {
        // 1) 保存业务草稿
        LeaveApply apply = new LeaveApply();
        apply.setApplicantId(req.getApplicantId());
        apply.setDays(req.getDays());
        apply.setReason(req.getReason());
        apply.setStatus("DRAFT");
        leaveApplyRepository.save(apply);

        // 2) 启动流程
        String businessKey = "LEAVE-" + apply.getId();
        Map<String, Object> vars = new HashMap<>();
        vars.put("applicantId", req.getApplicantId());
        vars.put("days", req.getDays());
        vars.put("reason", req.getReason());
        // 经理固定为 manager1（简单演示）
        vars.put("manager", "manager1");

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("leave_demo", businessKey, vars);

        // 3) 回写业务关联
        apply.setBusinessKey(businessKey);
        apply.setProcessInstanceId(pi.getId());
        apply.setStatus("PROCESSING");
        leaveApplyRepository.save(apply);

        return apply;
    }
}
