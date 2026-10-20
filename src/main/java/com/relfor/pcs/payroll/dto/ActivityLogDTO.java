package com.relfor.pcs.payroll.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ActivityLogDTO(
        Long id,
        Instant occurredAt,
        String actorType,
        Long actorStaffId,
        String actorDisplayName,
        String actorUsername,
        String module,
        String moduleLabel,
        String action,
        String actionLabel,
        String resourceType,
        String resourceId,
        String resourceLabel,
        String summary,
        String outcome,
        String correlationId,
        String ipAddress,
        Map<String, Object> context
) {
    public record Page(List<ActivityLogDTO> items, long total, int page, int size, int totalPages) {
    }

    public record ModuleOption(String id, String label) {
    }

    public record ActionOption(String id, String module, String label) {
    }

    public record Catalog(List<ModuleOption> modules, List<ActionOption> actions) {
    }
}
