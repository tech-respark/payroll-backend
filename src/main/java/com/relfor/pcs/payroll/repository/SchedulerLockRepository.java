package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.SchedulerLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SchedulerLockRepository extends JpaRepository<SchedulerLock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM SchedulerLock l WHERE l.lockName = :lockName")
    Optional<SchedulerLock> findByLockNameForUpdate(@Param("lockName") String lockName);
}
