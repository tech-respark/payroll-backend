package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.StoreStaffRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreStaffRoleRepository extends JpaRepository<StoreStaffRole, Long> {
    
    List<StoreStaffRole> findByStaffIdAndActive(Long staffId, Integer active);
    boolean existsByStaffIdAndRoleIdAndActive(Long staffId, Long roleId, Integer active);

    @Query("SELECT r.name FROM StoreStaffRole ssr JOIN Role r ON ssr.roleId = r.id " +
           "WHERE ssr.staffId = :staffId AND ssr.active = 1 AND r.active = 1")
    List<String> findActiveRoleNamesByStaffId(@Param("staffId") Long staffId);

    @Query("SELECT r FROM StoreStaffRole ssr JOIN Role r ON ssr.roleId = r.id " +
           "WHERE ssr.staffId = :staffId AND ssr.active = 1 AND r.active = 1")
    List<com.relfor.pcs.payroll.entity.Role> findActiveRolesByStaffId(@Param("staffId") Long staffId);
}
