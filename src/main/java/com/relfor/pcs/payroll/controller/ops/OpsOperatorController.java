package com.relfor.pcs.payroll.controller.ops;

import com.relfor.pcs.payroll.service.ops.OpsOperatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/ops/operators")
@PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).SUPERADMIN)")
public class OpsOperatorController {

    private final OpsOperatorService opsOperatorService;

    public OpsOperatorController(OpsOperatorService opsOperatorService) {
        this.opsOperatorService = opsOperatorService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        return ResponseEntity.ok(Map.of("success", true, "data", opsOperatorService.list()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("success", true, "data", opsOperatorService.create(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("success", true, "data", opsOperatorService.update(id, body)));
    }
}
