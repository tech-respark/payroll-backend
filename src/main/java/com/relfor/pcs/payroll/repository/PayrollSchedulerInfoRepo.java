package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.dto.PayrollSchedulerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.repository.query.Param;

@Repository
public interface PayrollSchedulerInfoRepo extends JpaRepository<PayrollSchedulerInfo, Long> {

    @Query("SELECT p FROM PayrollSchedulerInfo p WHERE p.invocationTime BETWEEN :startTime AND :endTime AND p.isProcessed IS NULL AND p.event = :event")
    List<PayrollSchedulerInfo> findAllByInvocationTimeAndEvent(
            @Param("startTime") Instant startTime, 
            @Param("endTime") Instant endTime, 
            @Param("event") String event);

}