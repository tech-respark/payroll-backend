package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import com.relfor.pcs.payroll.entity.StoreDetails;
import com.relfor.pcs.payroll.repository.StoreDetailsRepository;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/payroll-management/v1/storeSettings")
public class StoreSettingsController {

    @Autowired
    private StoreDetailsRepository storeDetailsRepository;

    @Autowired
    private ActivityRecorder activityRecorder;

    private Map<String, Object> createResponse(String message, String code, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("code", code);
        map.put("data", data);
        return map;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStoreSettings(
            @RequestParam Long tenantId,
            @RequestParam Long storeId) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);

        Optional<StoreDetails> optStore = storeDetailsRepository.fetchStoreAndTenantDetails( tenantId,
				storeId);
        if (optStore.isPresent()) {
            StoreDetails store = optStore.get();
            Map<String, String> data = new HashMap<>();
            data.put("storeOpenTime", store.getStoreOpenTime() != null ? store.getStoreOpenTime() : "08:00");
            data.put("storeCloseTime", store.getStoreCloseTime() != null ? store.getStoreCloseTime() : "22:00");
            return ResponseEntity.ok(createResponse("OK", "SUCCESS", data));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createResponse("Store not found", "ERROR", null));
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_STORE_SETTINGS, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SHIFTS)")
    public ResponseEntity<Map<String, Object>> saveStoreSettings(
            @RequestHeader("Tenantid") Long tenantId,
            @RequestHeader("Storeid") Long storeId,
            @RequestBody Map<String, String> payload) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);

        Optional<StoreDetails> optStore = storeDetailsRepository.fetchStoreAndTenantDetails(tenantId,
				storeId);
        if (optStore.isPresent()) {
            StoreDetails store = optStore.get();
            store.setStoreOpenTime(payload.get("storeOpenTime"));
            store.setStoreCloseTime(payload.get("storeCloseTime"));
            storeDetailsRepository.save(store);
            activityRecorder.record(ActivityEvent.builder()
                    .action(ActivityActions.SETTINGS_STORE_SAVED)
                    .tenantId(tenantId)
                    .storeId(storeId)
                    .resourceType(ActivityResourceTypes.STORE)
                    .resourceId(storeId)
                    .resourceLabel(store.getStoreName())
                    .summary("Updated store hours")
                    .context("open", payload.get("storeOpenTime"))
                    .context("close", payload.get("storeCloseTime"))
                    .build());
            return ResponseEntity.ok(createResponse("Store settings saved", "SUCCESS", store));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createResponse("Store not found", "ERROR", null));
        }
    }
}