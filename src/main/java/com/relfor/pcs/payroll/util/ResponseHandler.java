package com.relfor.pcs.payroll.util;
import com.relfor.pcs.payroll.dto.CommonConstants;
import com.relfor.pcs.payroll.util.SensitiveErrorFilter;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.util.SensitiveErrorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    public static ResponseEntity<Object> generateResponse(String message, String error, HttpStatus httpStatus,
                                                          Object responseObj) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("code", httpStatus.value());
        map.put("timestamp", new Date());
        if (httpStatus.value() == 200 || httpStatus.is2xxSuccessful()) {
            map.put("data", responseObj);
        } else {
            if (SensitiveErrorFilter.isSensitive(error)) {
                logger.error("Error - {}", error);
                error = "Something went wrong. Please try again later.";
            }
            map.put("error", error);
        }
        return new ResponseEntity<>(map, httpStatus);
    }

    public static ResponseEntity<ResponseModel> generateResponseModel(ResponseModel responseModel) {
        responseModel.setTimestamp(new Date());
        if (SensitiveErrorFilter.isSensitive(responseModel.getError())) {
            logger.error("Error - {}", responseModel.getError());
            responseModel.setError("Something went wrong. Please try again later.");
        }
        return new ResponseEntity<>(responseModel, responseModel.getCode());
    }
}