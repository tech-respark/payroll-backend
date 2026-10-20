package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.TipPoolRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TipPoolRunRepository extends JpaRepository<TipPoolRun, Long> {
    @Query("SELECT DISTINCT t FROM TipPoolRun t LEFT JOIN FETCH t.shares WHERE t.tenantId = :tenantId AND t.storeId = :storeId ORDER BY t.periodFrom DESC")
    List<TipPoolRun> findByTenantIdAndStoreIdOrderByPeriodFromDesc(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);

    @Query("SELECT DISTINCT t FROM TipPoolRun t LEFT JOIN FETCH t.shares WHERE t.id = :id")
    Optional<TipPoolRun> findWithSharesById(@Param("id") Long id);

    List<TipPoolRun> findByTenantIdAndStoreIdAndStatusAndPeriodFromLessThanEqualAndPeriodToGreaterThanEqual(
            Long tenantId, Long storeId, String status, LocalDate end, LocalDate start);
}
