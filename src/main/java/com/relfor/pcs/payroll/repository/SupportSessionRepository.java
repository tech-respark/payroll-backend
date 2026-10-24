package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.SupportSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SupportSessionRepository extends JpaRepository<SupportSession, Long> {

    Optional<SupportSession> findByTokenId(String tokenId);

    Optional<SupportSession> findByHandoffCode(String handoffCode);

    List<SupportSession> findByTenantIdAndExpiresAtAfterOrderByCreatedAtDesc(Long tenantId, Instant now);
}
