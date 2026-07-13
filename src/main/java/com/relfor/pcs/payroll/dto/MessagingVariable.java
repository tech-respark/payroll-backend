package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessagingVariable {
    private Long tenantId;
    private Long storeId;
    private String eventName;
    private String otp;
    private String recipientName;
    private String recipientCountryCode;
    private String recipientNumber;
}
