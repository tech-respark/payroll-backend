package com.relfor.pcs.payroll.controller;

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

        Optional<StoreDetails> optStore = storeDetailsRepository.fetchStoreAndTenantDetails("RESPARK", tenantId,
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
    public ResponseEntity<Map<String, Object>> saveStoreSettings(
            @RequestHeader("Tenantid") Long tenantId,
            @RequestHeader("Storeid") Long storeId,
            @RequestBody Map<String, String> payload) {
        tenantId = SecurityUtils.getTenantId(tenantId);
        storeId = SecurityUtils.getStoreId(storeId);

        Optional<StoreDetails> optStore = storeDetailsRepository.fetchStoreAndTenantDetails("RESPARK", tenantId,
				storeId);
        if (optStore.isPresent()) {
            StoreDetails store = optStore.get();
            store.setStoreOpenTime(payload.get("storeOpenTime"));
            store.setStoreCloseTime(payload.get("storeCloseTime"));
            storeDetailsRepository.save(store);
            return ResponseEntity.ok(createResponse("Store settings saved", "SUCCESS", store));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createResponse("Store not found", "ERROR", null));
        }
    }
}