package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityActorType;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityModule;
import com.relfor.pcs.payroll.activity.ActivityNames;
import com.relfor.pcs.payroll.activity.ActivityOutcome;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityRedactor;
import com.relfor.pcs.payroll.activity.ActivityRequestContext;
import com.relfor.pcs.payroll.dto.ActivityLogDTO;
import com.relfor.pcs.payroll.entity.ActivityLog;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.repository.ActivityLogRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.security.CustomUserDetails;
import com.relfor.pcs.payroll.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ActivityLogService implements ActivityRecorder {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);
    private static final ZoneId STORE_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ActivityLogRepository activityLogRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;

    public ActivityLogService(
            ActivityLogRepository activityLogRepository,
            PersonnelDetailsRepository personnelDetailsRepository) {
        this.activityLogRepository = activityLogRepository;
        this.personnelDetailsRepository = personnelDetailsRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void record(ActivityEvent event) {
        ActivityLog row = new ActivityLog();
        row.setOccurredAt(Instant.now());
        row.setAction(trim(event.getAction(), 80));
        row.setModule(event.getModule() != null ? event.getModule() : ActivityActions.moduleFor(event.getAction()));
        row.setOutcome(event.getOutcome() != null ? event.getOutcome() : ActivityOutcome.SUCCESS);
        row.setSummary(trim(event.getSummary(), 500));
        row.setResourceType(trim(event.getResourceType(), 40));
        row.setResourceId(trim(event.getResourceId(), 64));
        row.setResourceLabel(trim(event.getResourceLabel(), 255));
        row.setContextJson(ActivityRedactor.redact(event.getContext()));

        fillActor(row, event);
        fillScope(row, event);
        fillRequest(row);

        if (row.getTenantId() == null) {
            row.setTenantId(0L);
        }
        activityLogRepository.save(row);
    }

    @Override
    public void recordSafely(ActivityEvent event) {
        try {
            record(event);
        } catch (Exception ex) {
            log.error("Failed to persist activity event {}", event != null ? event.getAction() : "unknown", ex);
        }
    }

    @Transactional(readOnly = true)
    public ActivityLogDTO.Page search(
            Long tenantId,
            Long storeId,
            String module,
            String action,
            Long actorStaffId,
            String resourceType,
            String resourceId,
            String query,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {
        Long scopedTenant = SecurityUtils.getTenantId(tenantId);
        Long scopedStore = SecurityUtils.getStoreId(storeId);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Specification<ActivityLog> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), scopedTenant));
            if (scopedStore != null && scopedStore > 0) {
                predicates.add(cb.equal(root.get("storeId"), scopedStore));
            }
            if (StringUtils.hasText(module)) {
                try {
                    predicates.add(cb.equal(root.get("module"), ActivityModule.valueOf(module.trim().toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException ignored) {
                    predicates.add(cb.disjunction());
                }
            }
            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(root.get("action"), action.trim()));
            }
            if (actorStaffId != null) {
                predicates.add(cb.equal(root.get("actorStaffId"), actorStaffId));
            }
            if (StringUtils.hasText(resourceType)) {
                predicates.add(cb.equal(root.get("resourceType"), resourceType.trim()));
            }
            if (StringUtils.hasText(resourceId)) {
                predicates.add(cb.equal(root.get("resourceId"), resourceId.trim()));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from.atStartOfDay(STORE_ZONE).toInstant()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("occurredAt"), to.plusDays(1).atStartOfDay(STORE_ZONE).toInstant()));
            }
            if (StringUtils.hasText(query)) {
                String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("summary")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("actorDisplayName"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("actorUsername"), "")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("resourceLabel"), "")), like),
                        cb.like(cb.lower(root.get("action")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<ActivityLog> result = activityLogRepository.findAll(
                spec,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "occurredAt", "id"))
        );
        List<ActivityLogDTO> items = result.getContent().stream().map(this::toDto).toList();
        return new ActivityLogDTO.Page(
                items,
                result.getTotalElements(),
                safePage,
                safeSize,
                result.getTotalPages()
        );
    }

    public ActivityLogDTO.Catalog catalog() {
        List<ActivityLogDTO.ModuleOption> modules = Arrays.stream(ActivityModule.values())
                .map(module -> new ActivityLogDTO.ModuleOption(module.name(), module.label()))
                .toList();
        List<ActivityLogDTO.ActionOption> actions = ActivityActions.catalog().stream()
                .map(def -> new ActivityLogDTO.ActionOption(def.action(), def.module().name(), def.label()))
                .toList();
        return new ActivityLogDTO.Catalog(modules, actions);
    }

    private void fillActor(ActivityLog row, ActivityEvent event) {
        CustomUserDetails user = currentUser();
        PersonnelDetails principal = user != null ? loadPrincipal(user.getStaffId()) : null;

        ActivityActorType actorType = event.getActorType();
        if (actorType == null) {
            actorType = user != null ? ActivityActorType.USER : ActivityActorType.SYSTEM;
        }
        row.setActorType(actorType);

        Long staffId = event.getActorStaffId() != null ? event.getActorStaffId() : (user != null ? user.getStaffId() : null);
        row.setActorStaffId(staffId);

        String username = firstNonBlank(event.getActorUsername(), user != null ? user.getUsername() : null,
                principal != null ? principal.getUsername() : null);
        row.setActorUsername(trim(username, 128));

        String display = firstNonBlank(event.getActorDisplayName(), ActivityNames.displayName(principal));
        if (display == null && staffId != null && (principal == null || !staffId.equals(principal.getId()))) {
            display = ActivityNames.displayName(personnelDetailsRepository.findById(staffId).orElse(null));
        }
        row.setActorDisplayName(trim(display, 255));
    }

    private void fillScope(ActivityLog row, ActivityEvent event) {
        Long tenantId = event.getTenantId();
        Long storeId = event.getStoreId();
        if (tenantId == null || tenantId <= 0) {
            tenantId = SecurityUtils.getTenantId(null);
        }
        if (storeId == null || storeId <= 0) {
            storeId = SecurityUtils.getStoreId(null);
        }
        row.setTenantId(tenantId);
        row.setStoreId(storeId != null && storeId > 0 ? storeId : null);
    }

    private void fillRequest(ActivityLog row) {
        ActivityRequestContext.Snapshot snapshot = ActivityRequestContext.get();
        if (snapshot == null) {
            return;
        }
        row.setIpAddress(trim(snapshot.ipAddress(), 45));
        row.setUserAgent(trim(snapshot.userAgent(), 255));
        row.setCorrelationId(trim(snapshot.correlationId(), 64));
        row.setRequestMethod(trim(snapshot.method(), 12));
        row.setRequestPath(trim(snapshot.path(), 255));
    }

    private ActivityLogDTO toDto(ActivityLog row) {
        String actionLabel = ActivityActions.catalog().stream()
                .filter(def -> def.action().equals(row.getAction()))
                .map(ActivityActions.ActionDef::label)
                .findFirst()
                .orElse(row.getAction());
        Map<String, Object> context = ActivityRedactor.redact(row.getContextJson());
        return new ActivityLogDTO(
                row.getId(),
                row.getOccurredAt(),
                row.getActorType() != null ? row.getActorType().name() : null,
                row.getActorStaffId(),
                row.getActorDisplayName(),
                row.getActorUsername(),
                row.getModule() != null ? row.getModule().name() : null,
                row.getModule() != null ? row.getModule().label() : null,
                row.getAction(),
                actionLabel,
                row.getResourceType(),
                row.getResourceId(),
                row.getResourceLabel(),
                row.getSummary(),
                row.getOutcome() != null ? row.getOutcome().name() : null,
                row.getCorrelationId(),
                row.getIpAddress(),
                context
        );
    }

    private CustomUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details;
        }
        return null;
    }

    private PersonnelDetails loadPrincipal(Long staffId) {
        if (staffId == null) {
            return null;
        }
        return personnelDetailsRepository.findById(staffId).orElse(null);
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
