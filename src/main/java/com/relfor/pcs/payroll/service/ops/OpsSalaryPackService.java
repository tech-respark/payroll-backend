package com.relfor.pcs.payroll.service.ops;

import com.relfor.pcs.payroll.entity.SalaryComponentDefinitions;
import com.relfor.pcs.payroll.entity.SalaryComponentRules;
import com.relfor.pcs.payroll.repository.SalaryComponentDefinitionsRepository;
import org.mvel2.MVEL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class OpsSalaryPackService {

    private static final Pattern ALIAS = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

    private final SalaryComponentDefinitionsRepository definitionsRepository;

    public OpsSalaryPackService(SalaryComponentDefinitionsRepository definitionsRepository) {
        this.definitionsRepository = definitionsRepository;
    }

    public List<Map<String, Object>> list(Long tenantId, Long storeId) {
        return definitionsRepository.findAllByTenantIdAndStoreId(tenantId, storeId == null ? 0L : storeId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public Map<String, Object> upsertDefinition(Long tenantId, Map<String, Object> body) {
        Long id = body.get("id") instanceof Number number ? number.longValue() : null;
        SalaryComponentDefinitions definition = id == null
                ? new SalaryComponentDefinitions()
                : definitionsRepository.findById(id).orElseThrow(() -> OpsException.notFound("Definition not found."));
        if (id != null && !tenantId.equals(definition.getTenantId())) {
            throw OpsException.forbidden("Definition belongs to another tenant.");
        }
        String alias = required(body, "componentNameAlias");
        if (!ALIAS.matcher(alias).matches()) {
            throw OpsException.badRequest("Alias must be a valid identifier, for example basic or hra.");
        }
        definition.setTenantId(tenantId);
        definition.setStoreId(asLong(body.get("storeId"), 0L));
        definition.setComponentName(required(body, "componentName"));
        definition.setComponentNameAlias(alias);
        definition.setComponentType(required(body, "componentType").toUpperCase());
        definition.setComponentCategory(required(body, "componentCategory").toUpperCase());
        definition.setPriorityIndex(asInt(body.get("priorityIndex"), 10));
        definition.setComputable(asBool(body.get("computable"), "FORMULA".equals(definition.getComponentType())));
        definition.setIncludeInTotal(asBool(body.get("includeInTotal"), true));
        definition.setIsCalculatedMonthly(asBool(body.get("isCalculatedMonthly"), false));
        validateRules((List<?>) body.get("rules"));
        List<SalaryComponentRules> rules = new ArrayList<>();
        if (body.get("rules") instanceof List<?> incoming) {
            for (Object raw : incoming) {
                if (!(raw instanceof Map<?, ?> map)) {
                    continue;
                }
                SalaryComponentRules rule = new SalaryComponentRules();
                Object condition = map.get("conditionExpression");
                Object formula = map.get("formula");
                rule.setConditionExpression(condition == null || String.valueOf(condition).isBlank()
                        ? null : String.valueOf(condition).trim());
                rule.setFormula(formula == null ? null : String.valueOf(formula).trim());
                rules.add(rule);
            }
        }
        definition.setSalaryComponentRulesList(rules);
        return toView(definitionsRepository.save(definition));
    }

    @Transactional
    public void deleteDefinition(Long tenantId, Long definitionId) {
        SalaryComponentDefinitions definition = definitionsRepository.findById(definitionId)
                .orElseThrow(() -> OpsException.notFound("Definition not found."));
        if (!tenantId.equals(definition.getTenantId())) {
            throw OpsException.forbidden("Definition belongs to another tenant.");
        }
        definitionsRepository.delete(definition);
    }

    @Transactional
    public List<Map<String, Object>> applySystemPack(Long tenantId) {
        if (!definitionsRepository.findAllByTenantIdAndStoreId(tenantId, 0L).isEmpty()) {
            return list(tenantId, 0L);
        }
        List<SalaryComponentDefinitions> pack = new ArrayList<>();
        pack.add(fixed(tenantId, "Basic", "basic", "EARNING", 10, true, true, false));
        pack.add(fixed(tenantId, "Special allowance", "special_allowance", "EARNING", 20, true, true, false));
        pack.add(formula(tenantId, "HRA", "hra", "EARNING", 30, true, true, false,
                "basic > 0", "basic * 0.4"));
        pack.add(formula(tenantId, "Absent penalty", "absent_penalty", "DEDUCTION", 80, false, true, false,
                "totalPenaltyAbsentDays > 0", "(basic / 365) * totalPenaltyAbsentDays"));
        pack.add(monthly(tenantId, "Incentive", "incentive", "EARNING", 90));
        return definitionsRepository.saveAll(pack).stream().map(this::toView).toList();
    }

    @Transactional
    public List<Map<String, Object>> cloneFrom(Long sourceTenantId, Long targetTenantId) {
        List<SalaryComponentDefinitions> source = definitionsRepository.findAllByTenantIdAndStoreId(sourceTenantId, 0L);
        if (source.isEmpty()) {
            source = definitionsRepository.findAllByTenantIdAndStoreId(sourceTenantId, 1L);
        }
        if (source.isEmpty()) {
            throw OpsException.notFound("Source tenant has no salary definitions.");
        }
        java.util.Set<String> existing = definitionsRepository.findAllByTenantIdAndStoreId(targetTenantId, 0L)
                .stream()
                .map(SalaryComponentDefinitions::getComponentNameAlias)
                .filter(alias -> alias != null && !alias.isBlank())
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());
        List<SalaryComponentDefinitions> copies = new ArrayList<>();
        for (SalaryComponentDefinitions item : source.stream()
                .sorted(Comparator.comparing(d -> d.getPriorityIndex() == null ? 0 : d.getPriorityIndex()))
                .toList()) {
            String alias = item.getComponentNameAlias() == null ? "" : item.getComponentNameAlias().toLowerCase();
            if (existing.contains(alias)) {
                continue;
            }
            SalaryComponentDefinitions copy = new SalaryComponentDefinitions();
            copy.setTenantId(targetTenantId);
            copy.setStoreId(0L);
            copy.setComponentName(item.getComponentName());
            copy.setComponentNameAlias(item.getComponentNameAlias());
            copy.setComponentType(item.getComponentType());
            copy.setComponentCategory(item.getComponentCategory());
            copy.setPriorityIndex(item.getPriorityIndex());
            copy.setComputable(item.getComputable());
            copy.setIncludeInTotal(item.getIncludeInTotal());
            copy.setIsCalculatedMonthly(item.getIsCalculatedMonthly());
            List<SalaryComponentRules> rules = new ArrayList<>();
            if (item.getSalaryComponentRulesList() != null) {
                for (SalaryComponentRules rule : item.getSalaryComponentRulesList()) {
                    SalaryComponentRules copied = new SalaryComponentRules();
                    copied.setConditionExpression(rule.getConditionExpression());
                    copied.setFormula(rule.getFormula());
                    rules.add(copied);
                }
            }
            copy.setSalaryComponentRulesList(rules);
            copies.add(copy);
        }
        return definitionsRepository.saveAll(copies).stream().map(this::toView).toList();
    }

    public Map<String, Object> preview(String formula, Map<String, Object> inputs) {
        Map<String, BigDecimal> context = new HashMap<>();
        if (inputs != null) {
            inputs.forEach((key, value) -> context.put(key, asDecimal(value)));
        }
        try {
            Object result = MVEL.eval(formula, context);
            return Map.of("ok", true, "result", result);
        } catch (Exception ex) {
            throw OpsException.badRequest("Formula could not be evaluated: " + ex.getMessage());
        }
    }

    private void validateRules(List<?> rules) {
        if (rules == null) {
            return;
        }
        Map<String, BigDecimal> sample = new HashMap<>();
        sample.put("basic", BigDecimal.valueOf(120000));
        sample.put("special_allowance", BigDecimal.valueOf(24000));
        sample.put("totalPenaltyAbsentDays", BigDecimal.ONE);
        sample.put("workingHours", BigDecimal.valueOf(9.5));
        for (Object raw : rules) {
            if (!(raw instanceof Map<?, ?> map)) {
                continue;
            }
            Object condition = map.get("conditionExpression");
            Object formula = map.get("formula");
            if (condition != null && !String.valueOf(condition).isBlank()) {
                try {
                    Serializable compiled = MVEL.compileExpression(String.valueOf(condition));
                    MVEL.executeExpression(compiled, sample);
                } catch (Exception ex) {
                    throw OpsException.badRequest("Invalid condition: " + ex.getMessage());
                }
            }
            if (formula != null && !String.valueOf(formula).isBlank()) {
                try {
                    MVEL.compileExpression(String.valueOf(formula));
                } catch (Exception ex) {
                    throw OpsException.badRequest("Invalid formula: " + ex.getMessage());
                }
            }
        }
    }

    private SalaryComponentDefinitions fixed(
            Long tenantId, String name, String alias, String category, int priority,
            boolean computable, boolean includeInTotal, boolean monthly) {
        SalaryComponentDefinitions definition = base(tenantId, name, alias, "FIXED", category, priority,
                computable, includeInTotal, monthly);
        definition.setSalaryComponentRulesList(new ArrayList<>());
        return definition;
    }

    private SalaryComponentDefinitions monthly(Long tenantId, String name, String alias, String category, int priority) {
        return fixed(tenantId, name, alias, category, priority, false, true, true);
    }

    private SalaryComponentDefinitions formula(
            Long tenantId, String name, String alias, String category, int priority,
            boolean computable, boolean includeInTotal, boolean monthly,
            String condition, String formula) {
        SalaryComponentDefinitions definition = base(tenantId, name, alias, "FORMULA", category, priority,
                computable, includeInTotal, monthly);
        SalaryComponentRules rule = new SalaryComponentRules();
        rule.setConditionExpression(condition);
        rule.setFormula(formula);
        definition.setSalaryComponentRulesList(List.of(rule));
        return definition;
    }

    private SalaryComponentDefinitions base(
            Long tenantId, String name, String alias, String type, String category, int priority,
            boolean computable, boolean includeInTotal, boolean monthly) {
        SalaryComponentDefinitions definition = new SalaryComponentDefinitions();
        definition.setTenantId(tenantId);
        definition.setStoreId(0L);
        definition.setComponentName(name);
        definition.setComponentNameAlias(alias);
        definition.setComponentType(type);
        definition.setComponentCategory(category);
        definition.setPriorityIndex(priority);
        definition.setComputable(computable);
        definition.setIncludeInTotal(includeInTotal);
        definition.setIsCalculatedMonthly(monthly);
        return definition;
    }

    private Map<String, Object> toView(SalaryComponentDefinitions definition) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", definition.getId());
        view.put("tenantId", definition.getTenantId());
        view.put("storeId", definition.getStoreId());
        view.put("componentName", definition.getComponentName());
        view.put("componentNameAlias", definition.getComponentNameAlias());
        view.put("componentType", definition.getComponentType());
        view.put("componentCategory", definition.getComponentCategory());
        view.put("priorityIndex", definition.getPriorityIndex());
        view.put("computable", definition.getComputable());
        view.put("includeInTotal", definition.getIncludeInTotal());
        view.put("isCalculatedMonthly", definition.getIsCalculatedMonthly());
        List<Map<String, Object>> rules = new ArrayList<>();
        if (definition.getSalaryComponentRulesList() != null) {
            for (SalaryComponentRules rule : definition.getSalaryComponentRulesList()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rule.getId());
                row.put("conditionExpression", rule.getConditionExpression());
                row.put("formula", rule.getFormula());
                rules.add(row);
            }
        }
        view.put("rules", rules);
        return view;
    }

    private String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw OpsException.badRequest(key + " is required.");
        }
        return String.valueOf(value).trim();
    }

    private Long asLong(Object value, Long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer asInt(Object value, Integer fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return fallback;
    }

    private Boolean asBool(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    private BigDecimal asDecimal(Object value) {
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
