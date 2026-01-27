package com.OA.system.service;

import com.OA.system.dto.CompleteTaskRequest;
import com.OA.system.entity.LeaveApply;
import com.OA.system.dto.StartLeaveRequest;
import com.github.yulichang.base.MPJBaseService;

public interface LeaveService extends MPJBaseService<LeaveApply> {
    LeaveApply submit(StartLeaveRequest req);
}