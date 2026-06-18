package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByActive(Integer active);
    List<Role> findByActiveAndTenantId(Integer active, Long tenantId);
}
