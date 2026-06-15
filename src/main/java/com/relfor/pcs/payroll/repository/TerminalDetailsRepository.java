package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.TerminalDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalDetailsRepository extends JpaRepository<TerminalDetails,Long> {
}