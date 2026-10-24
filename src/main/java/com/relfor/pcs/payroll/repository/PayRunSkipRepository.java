package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PayRunSkip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayRunSkipRepository extends JpaRepository<PayRunSkip, Long> {
    List<PayRunSkip> findByPayRunId(Long payRunId);
    Optional<PayRunSkip> findByPayRunIdAndStaffId(Long payRunId, Long staffId);
    void deleteByPayRunIdAndStaffId(Long payRunId, Long staffId);
}
