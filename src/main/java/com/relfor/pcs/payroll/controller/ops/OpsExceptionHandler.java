package com.relfor.pcs.payroll.controller.ops;

import com.relfor.pcs.payroll.service.ops.OpsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.relfor.pcs.payroll.controller.ops")
public class OpsExceptionHandler {

    @ExceptionHandler(OpsException.class)
    public ResponseEntity<Map<String, Object>> handle(OpsException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of(
                "success", false,
                "error", ex.getStatus().name(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadEnum(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "BAD_REQUEST",
                "message", ex.getMessage()
        ));
    }
}
