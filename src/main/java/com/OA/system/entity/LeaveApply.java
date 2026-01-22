package com.OA.system.entity;

import lombok.Data;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "leave_apply")
@Data
public class LeaveApply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicantId;
    private Integer days;
    private String reason;

    private String processInstanceId;
    private String businessKey;
    private String status; // DRAFT/PROCESSING/FINISHED

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) status = "DRAFT";
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}
