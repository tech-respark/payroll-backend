package com.relfor.pcs.payroll.entity;

import com.relfor.pcs.payroll.activity.ActivityActorType;
import com.relfor.pcs.payroll.activity.ActivityModule;
import com.relfor.pcs.payroll.activity.ActivityOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * Append-only activity event. Application code must not update or delete rows.
 * MySQL triggers reject UPDATE/DELETE once {@code ActivityLogSchemaGuard} has run.
 */
@Entity
@Table(name = "activity_log", indexes = {
        @Index(name = "idx_activity_tenant_time", columnList = "tenant_id, occurred_at"),
        @Index(name = "idx_activity_store_time", columnList = "tenant_id, store_id, occurred_at"),
        @Index(name = "idx_activity_module_time", columnList = "tenant_id, module, occurred_at"),
        @Index(name = "idx_activity_action_time", columnList = "tenant_id, action, occurred_at"),
        @Index(name = "idx_activity_actor_time", columnList = "tenant_id, actor_staff_id, occurred_at"),
        @Index(name = "idx_activity_resource", columnList = "tenant_id, resource_type, resource_id, occurred_at")
})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @Column(name = "store_id", updatable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20, updatable = false)
    private ActivityActorType actorType;

    @Column(name = "actor_staff_id", updatable = false)
    private Long actorStaffId;

    @Column(name = "actor_username", length = 128, updatable = false)
    private String actorUsername;

    @Column(name = "actor_display_name", length = 255, updatable = false)
    private String actorDisplayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", nullable = false, length = 40, updatable = false)
    private ActivityModule module;

    @Column(name = "action", nullable = false, length = 80, updatable = false)
    private String action;

    @Column(name = "resource_type", length = 40, updatable = false)
    private String resourceType;

    @Column(name = "resource_id", length = 64, updatable = false)
    private String resourceId;

    @Column(name = "resource_label", length = 255, updatable = false)
    private String resourceLabel;

    @Column(name = "summary", nullable = false, length = 500, updatable = false)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 20, updatable = false)
    private ActivityOutcome outcome;

    @Column(name = "correlation_id", length = 64, updatable = false)
    private String correlationId;

    @Column(name = "request_method", length = 12, updatable = false)
    private String requestMethod;

    @Column(name = "request_path", length = 255, updatable = false)
    private String requestPath;

    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 255, updatable = false)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_json", columnDefinition = "json", updatable = false)
    private Map<String, Object> contextJson;

    public Long getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public ActivityActorType getActorType() {
        return actorType;
    }

    public void setActorType(ActivityActorType actorType) {
        this.actorType = actorType;
    }

    public Long getActorStaffId() {
        return actorStaffId;
    }

    public void setActorStaffId(Long actorStaffId) {
        this.actorStaffId = actorStaffId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public void setActorUsername(String actorUsername) {
        this.actorUsername = actorUsername;
    }

    public String getActorDisplayName() {
        return actorDisplayName;
    }

    public void setActorDisplayName(String actorDisplayName) {
        this.actorDisplayName = actorDisplayName;
    }

    public ActivityModule getModule() {
        return module;
    }

    public void setModule(ActivityModule module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceLabel() {
        return resourceLabel;
    }

    public void setResourceLabel(String resourceLabel) {
        this.resourceLabel = resourceLabel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ActivityOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(ActivityOutcome outcome) {
        this.outcome = outcome;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Map<String, Object> getContextJson() {
        return contextJson;
    }

    public void setContextJson(Map<String, Object> contextJson) {
        this.contextJson = contextJson;
    }
}
