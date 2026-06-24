package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LeaveTransactionLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface LeaveTransactionLedgerRepository extends JpaRepository<LeaveTransactionLedger, Long> {

    /**
     * Calculates the active balance for an employee's specific leave type.
     * Restricting this query to scan since the start of the current calendar year prevents
     * scanning thousands of rows for legacy personnel.
     */
    @Query("SELECT COALESCE(SUM(l.transactionValue), 0.0) FROM LeaveTransactionLedger l " +
           "WHERE l.staffId = :staffId " +
           "AND l.leaveType.id = :leaveTypeId " +
           "AND l.effectiveDate >= :yearStart")
    BigDecimal calculateBalanceForYear(@Param("staffId") Long staffId, 
                                       @Param("leaveTypeId") Long leaveTypeId, 
                                       @Param("yearStart") LocalDate yearStart);
}
