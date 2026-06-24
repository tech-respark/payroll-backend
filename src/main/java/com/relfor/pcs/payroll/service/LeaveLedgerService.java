package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.entity.LeaveApplication;
import com.relfor.pcs.payroll.entity.LeaveTransactionLedger;
import com.relfor.pcs.payroll.entity.LeaveType;
import com.relfor.pcs.payroll.repository.LeaveTransactionLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LeaveLedgerService {

    private final LeaveTransactionLedgerRepository ledgerRepository;

    /**
     * Immutable read-only view of current balance.
     */
    public BigDecimal getAvailableBalance(Long personnelId, Long leaveTypeId, LocalDate asOfDate) {
        // Find the start of the year based on effective rules (assuming Jan 1st for now)
        LocalDate yearStart = LocalDate.of(asOfDate.getYear(), 1, 1);
        return ledgerRepository.calculateBalanceForYear(personnelId, leaveTypeId, yearStart);
    }

    /**
     * Immutable write operation. Never UPDATEs, only INSERTs.
     */
    @Transactional
    public void recordTransaction(Long personnelId, LeaveType type, BigDecimal value, 
                                  LeaveTransactionLedger.TransactionType txnType, 
                                  LeaveApplication application, LocalDate effectiveDate) {
        
        LeaveTransactionLedger ledger = LeaveTransactionLedger.builder()
                .personnelId(personnelId)
                .leaveType(type)
                .transactionValue(value)
                .transactionType(txnType)
                .leaveApplication(application)
                .effectiveDate(effectiveDate)
                .build();
                
        ledgerRepository.save(ledger);
    }
}
