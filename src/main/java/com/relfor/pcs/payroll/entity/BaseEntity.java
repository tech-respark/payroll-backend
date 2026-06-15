package com.relfor.pcs.payroll.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(name = "system_created_on", updatable = false)
    private Instant systemCreatedOn;

    @UpdateTimestamp
    @Column(name = "system_updated_on")
    private Instant systemUpdatedOn;

    public Instant getSystemCreatedOn() {
        return systemCreatedOn;
    }

    public Instant getSystemUpdatedOn() {
        return systemUpdatedOn;
    }
}