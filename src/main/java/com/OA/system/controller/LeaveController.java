package com.OA.system.controller;

import com.OA.system.dto.StartLeaveRequest;
import com.OA.system.entity.LeaveApply;
import com.OA.system.dto.ApiResponse;
import com.OA.system.repository.LeaveApplyRepository;
import com.OA.system.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/OA/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final LeaveApplyRepository leaveApplyRepository;

    /*发起请假*/
    @PostMapping("/start")
    public ApiResponse<LeaveApply> start(@Validated @RequestBody StartLeaveRequest req) {
        return ApiResponse.ok(leaveService.submit(req));
    }

    /*查询业务状态*/
    @GetMapping("/by-proc/{procInsId}")
    public ApiResponse<LeaveApply> getByProc(@PathVariable String procInsId) {
        Optional<LeaveApply> opt = leaveApplyRepository.findByProcessInstanceId(procInsId);
        return opt.map(ApiResponse::ok).orElseGet(() -> ApiResponse.fail("not found"));
    }
}
