package com.relfor.pcs.payroll.controller.ops;

import com.relfor.pcs.payroll.service.ops.OpsProductService;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/ops/products")
@PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN, T(com.relfor.pcs.payroll.security.Permissions).SALES)")
public class OpsProductController {

    private final OpsProductService productService;

    public OpsProductController(OpsProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        if (includeInactive) {
            return ok(productService.listCatalog());
        }
        return ok(productService.listForPicker());
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN)")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ok(productService.create(body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN)")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ok(productService.update(id, body));
    }

    private ResponseEntity<Map<String, Object>> ok(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data, "actor", String.valueOf(SecurityUtils.currentUsername())));
    }
}
