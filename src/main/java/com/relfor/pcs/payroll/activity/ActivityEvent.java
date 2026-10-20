package com.relfor.pcs.payroll.activity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Domain event passed to {@link ActivityRecorder}. Request-scoped actor, tenant,
 * store, IP, and correlation are filled automatically when omitted.
 */
public final class ActivityEvent {

    private ActivityActorType actorType;
    private Long actorStaffId;
    private String actorUsername;
    private String actorDisplayName;
    private Long tenantId;
    private Long storeId;
    private ActivityModule module;
    private String action;
    private String resourceType;
    private String resourceId;
    private String resourceLabel;
    private String summary;
    private ActivityOutcome outcome = ActivityOutcome.SUCCESS;
    private final Map<String, Object> context = new LinkedHashMap<>();

    public static Builder builder() {
        return new Builder();
    }

    public ActivityActorType getActorType() {
        return actorType;
    }

    public Long getActorStaffId() {
        return actorStaffId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getActorDisplayName() {
        return actorDisplayName;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public ActivityModule getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceLabel() {
        return resourceLabel;
    }

    public String getSummary() {
        return summary;
    }

    public ActivityOutcome getOutcome() {
        return outcome;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public static final class Builder {
        private final ActivityEvent event = new ActivityEvent();

        public Builder actorType(ActivityActorType actorType) {
            event.actorType = actorType;
            return this;
        }

        public Builder actorStaffId(Long actorStaffId) {
            event.actorStaffId = actorStaffId;
            return this;
        }

        public Builder actorUsername(String actorUsername) {
            event.actorUsername = actorUsername;
            return this;
        }

        public Builder actorDisplayName(String actorDisplayName) {
            event.actorDisplayName = actorDisplayName;
            return this;
        }

        public Builder tenantId(Long tenantId) {
            event.tenantId = tenantId;
            return this;
        }

        public Builder storeId(Long storeId) {
            event.storeId = storeId;
            return this;
        }

        public Builder module(ActivityModule module) {
            event.module = module;
            return this;
        }

        public Builder action(String action) {
            event.action = action;
            return this;
        }

        public Builder resourceType(String resourceType) {
            event.resourceType = resourceType;
            return this;
        }

        public Builder resourceId(Object resourceId) {
            event.resourceId = resourceId == null ? null : String.valueOf(resourceId);
            return this;
        }

        public Builder resourceLabel(String resourceLabel) {
            event.resourceLabel = resourceLabel;
            return this;
        }

        public Builder summary(String summary) {
            event.summary = summary;
            return this;
        }

        public Builder outcome(ActivityOutcome outcome) {
            event.outcome = outcome == null ? ActivityOutcome.SUCCESS : outcome;
            return this;
        }

        public Builder context(String key, Object value) {
            if (key != null && !key.isBlank() && value != null) {
                event.context.put(key, value);
            }
            return this;
        }

        public Builder context(Map<String, ?> values) {
            if (values != null) {
                values.forEach(this::context);
            }
            return this;
        }

        public ActivityEvent build() {
            if (event.action == null || event.action.isBlank()) {
                throw new IllegalArgumentException("Activity action is required");
            }
            if (event.module == null) {
                event.module = ActivityActions.moduleFor(event.action);
            }
            if (event.summary == null || event.summary.isBlank()) {
                event.summary = event.action;
            }
            return event;
        }
    }
}
