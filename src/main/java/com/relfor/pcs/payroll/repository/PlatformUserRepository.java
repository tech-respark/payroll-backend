package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PlatformUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformUserRepository extends JpaRepository<PlatformUser, Long> {
    Optional<PlatformUser> findByUsernameIgnoreCase(String username);
    Optional<PlatformUser> findByEmailIgnoreCase(String email);
    List<PlatformUser> findAllByOrderByIdAsc();
    long countByRoleAndActiveTrue(PlatformUser.Role role);
}
