package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StaffPasswordResetOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface StaffPasswordResetOTPRepository extends JpaRepository<StaffPasswordResetOTP, Long> {
    
    @Query("SELECT o FROM StaffPasswordResetOTP o WHERE o.staffId = :staffId AND o.expired = false AND o.expiryTime > :now")
    List<StaffPasswordResetOTP> findValidOtpsByStaffId(Long staffId, Instant now);
    
    Optional<StaffPasswordResetOTP> findByResetTokenAndExpiredFalse(String resetToken);
}
