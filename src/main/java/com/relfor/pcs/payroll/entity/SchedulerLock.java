package com.relfor.pcs.payroll.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "scheduler_lock")
public class SchedulerLock {

    @Id
    @Column(name = "lock_name", length = 64)
    private String lockName;

    @Column(name = "locked_until", nullable = false)
    private Instant lockedUntil;

    @Column(name = "locked_by", length = 128)
    private String lockedBy;

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }
}
