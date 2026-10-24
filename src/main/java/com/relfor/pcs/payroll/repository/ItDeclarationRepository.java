package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.ItDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItDeclarationRepository extends JpaRepository<ItDeclaration, Long> {
    List<ItDeclaration> findByTenantIdAndStoreIdOrderByIdDesc(Long tenantId, Long storeId);
    List<ItDeclaration> findByTenantIdAndStoreIdAndStaffIdOrderByIdDesc(Long tenantId, Long storeId, Long staffId);
    Optional<ItDeclaration> findFirstByTenantIdAndStoreIdAndStaffIdAndFinancialYearAndStatus(
            Long tenantId, Long storeId, Long staffId, String financialYear, String status);
}
