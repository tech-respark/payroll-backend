package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PlatformProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformProductRepository extends JpaRepository<PlatformProduct, Long> {
    Optional<PlatformProduct> findByCodeIgnoreCase(String code);
    Optional<PlatformProduct> findFirstByDefaultProductTrue();
    List<PlatformProduct> findAllByActiveTrueOrderBySortOrderAscIdAsc();
    List<PlatformProduct> findAllByOrderBySortOrderAscIdAsc();
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
