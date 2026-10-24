package com.relfor.pcs.payroll.service.ops;

import org.springframework.http.HttpStatus;

public class OpsException extends RuntimeException {
    private final HttpStatus status;

    public OpsException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static OpsException badRequest(String message) {
        return new OpsException(HttpStatus.BAD_REQUEST, message);
    }

    public static OpsException notFound(String message) {
        return new OpsException(HttpStatus.NOT_FOUND, message);
    }

    public static OpsException conflict(String message) {
        return new OpsException(HttpStatus.CONFLICT, message);
    }

    public static OpsException forbidden(String message) {
        return new OpsException(HttpStatus.FORBIDDEN, message);
    }
}
