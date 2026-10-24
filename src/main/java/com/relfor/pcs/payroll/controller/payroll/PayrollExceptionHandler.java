package com.relfor.pcs.payroll.controller.payroll;

import com.relfor.pcs.payroll.dto.ResponseModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.relfor.pcs.payroll.controller.payroll")
public class PayrollExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ResponseModel> badRequest(RuntimeException ex) {
        ResponseModel model = new ResponseModel();
        model.setCode(HttpStatus.BAD_REQUEST);
        model.setMessage(ex.getMessage());
        model.setError(ex.getMessage());
        return ResponseEntity.badRequest().body(model);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseModel> denied(AccessDeniedException ex) {
        ResponseModel model = new ResponseModel();
        model.setCode(HttpStatus.FORBIDDEN);
        model.setMessage(ex.getMessage());
        model.setError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(model);
    }
}
