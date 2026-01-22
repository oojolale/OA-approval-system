package com.OA.system.repository;

import com.OA.system.entity.LeaveApply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface LeaveApplyRepository extends JpaRepository<LeaveApply, Long> {
    Optional<LeaveApply> findByProcessInstanceId(String processInstanceId);
}