package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StaffSalesEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StaffSalesEntryRepository extends JpaRepository<StaffSalesEntry, Long> {
    List<StaffSalesEntry> findByTenantIdAndStoreIdAndSaleDateBetween(Long tenantId, Long storeId, LocalDate from, LocalDate to);
    List<StaffSalesEntry> findByTenantIdAndStoreIdAndStaffIdAndSaleDateBetween(Long tenantId, Long storeId, Long staffId, LocalDate from, LocalDate to);
}
