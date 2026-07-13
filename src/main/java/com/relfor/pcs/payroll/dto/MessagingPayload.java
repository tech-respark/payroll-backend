package com.relfor.pcs.payroll.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MessagingPayload {
    private List<NotificationType> notificationTypes;
    private Map<String, Object> variables;
    private String eventName;
    private String type;
    private Long tenantId;
    private Long storeId;
    private String entity;
    private List<Map<String, String>> recipient;
}
