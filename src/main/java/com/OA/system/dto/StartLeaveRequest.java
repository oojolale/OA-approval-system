package com.OA.system.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
@Data
public class StartLeaveRequest {
    @NotNull
    private Long applicantId;
    @NotNull
    @Min(1)
    private Integer days;
    private String reason;
}