package com.relfor.pcs.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.relfor.pcs.payroll.entity.DayWiseShiftsTiming;

@Repository
public interface DayWiseShiftsTimingRepository extends JpaRepository<DayWiseShiftsTiming, Long> {
}