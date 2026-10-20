package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StoreDailySales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoreDailySalesRepository extends JpaRepository<StoreDailySales, Long> {
    Optional<StoreDailySales> findByTenantIdAndStoreIdAndSaleDate(Long tenantId, Long storeId, LocalDate saleDate);
    List<StoreDailySales> findByTenantIdAndStoreIdAndSaleDateBetween(Long tenantId, Long storeId, LocalDate from, LocalDate to);
}
