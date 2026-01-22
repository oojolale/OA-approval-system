package com.OA.system.service;

import com.OA.system.entity.LeaveApply;
import com.OA.system.dto.StartLeaveRequest;
public interface LeaveService {
    LeaveApply submit(StartLeaveRequest req);
}