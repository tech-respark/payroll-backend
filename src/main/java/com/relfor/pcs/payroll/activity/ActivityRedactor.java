package com.relfor.pcs.payroll.activity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Drops secrets and high-sensitivity payroll fields from context JSON.
 * The activity log stores who did what, not salary, bank, or credential values.
 */
public final class ActivityRedactor {

    private static final Set<String> BLOCKED = Set.of(
            "password", "pwd", "newpassword", "oldpassword", "otp", "token", "resettoken",
            "secret", "authorization", "jwt", "apikey", "api_key", "creditcard", "cvv",
            "salary", "salaryamount", "amount", "principal", "outstanding", "installment",
            "bank", "account", "accountnumber", "ifsccode", "pan", "pannumber", "aadhaar",
            "uan", "esinumber", "latitude", "longitude", "geo"
    );

    private static final int MAX_KEYS = 24;
    private static final int MAX_STRING = 240;

    private ActivityRedactor() {
    }

    public static Map<String, Object> redact(Map<String, ?> raw) {
        Map<String, Object> clean = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) {
            return clean;
        }
        int count = 0;
        for (Map.Entry<String, ?> entry : raw.entrySet()) {
            if (count >= MAX_KEYS) {
                break;
            }
            String key = entry.getKey();
            if (key == null || isBlocked(key) || entry.getValue() == null) {
                continue;
            }
            Object value = sanitize(entry.getValue());
            if (value != null) {
                clean.put(key, value);
                count++;
            }
        }
        return clean;
    }

    private static boolean isBlocked(String key) {
        String normalized = key.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        for (String blocked : BLOCKED) {
            if (normalized.contains(blocked)) {
                return true;
            }
        }
        return false;
    }

    private static Object sanitize(Object value) {
        if (value instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return trimmed.length() > MAX_STRING ? trimmed.substring(0, MAX_STRING) : trimmed;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Enum<?> enumerated) {
            return enumerated.name();
        }
        if (value instanceof Collection<?> collection) {
            return collection.size();
        }
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        String asText = String.valueOf(value);
        return asText.length() > MAX_STRING ? asText.substring(0, MAX_STRING) : asText;
    }
}
