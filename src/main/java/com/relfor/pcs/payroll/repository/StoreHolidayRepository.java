package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StoreHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StoreHolidayRepository extends JpaRepository<StoreHoliday, Long> {

    List<StoreHoliday> findByTenantIdAndStoreIdAndHolidayDateBetween(Long tenantId, Long storeId, LocalDate startDate, LocalDate endDate);
    
    List<StoreHoliday> findByTenantIdAndStoreIdAndHolidayDate(Long tenantId, Long storeId, LocalDate holidayDate);

    boolean existsByTenantIdAndStoreIdAndHolidayDate(Long tenantId, Long storeId, LocalDate holidayDate);
}
