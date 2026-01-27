package com.OA.system.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
@Data
public class CompleteTaskRequest {
    @NotBlank
    private String taskId;
    @NotBlank
    private String approver; // 办理人，如 manager1
    private String comment;  // 审批意见
    private String procInsId; // processInstanceId
}