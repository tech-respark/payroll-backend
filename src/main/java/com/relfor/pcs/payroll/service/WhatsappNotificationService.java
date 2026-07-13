package com.relfor.pcs.payroll.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relfor.pcs.payroll.dto.MessagingPayload;
import com.relfor.pcs.payroll.dto.MessagingVariable;
import com.relfor.pcs.payroll.dto.NotificationType;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.slf4j.MDC;
import com.relfor.pcs.payroll.dto.CommonConstants;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class WhatsappNotificationService {

    private final Logger logger = LoggerFactory.getLogger(WhatsappNotificationService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${env:dev}")
    private String env;

    public HttpHeaders getDefaultHeaders(Long fallbackTenantId, Long fallbackStoreId) {
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.set(CommonConstants.INTERNAL_TRACE_ID, MDC.get(CommonConstants.INTERNAL_TRACE_ID) != null ? MDC.get(CommonConstants.INTERNAL_TRACE_ID) : java.util.UUID.randomUUID().toString());
            headers.set(CommonConstants.USER_ID, MDC.get(CommonConstants.USER_ID) != null ? MDC.get(CommonConstants.USER_ID) : "system");
            headers.set(CommonConstants.USERNAME, MDC.get(CommonConstants.USERNAME) != null ? MDC.get(CommonConstants.USERNAME) : "system");
            
            String tenantIdStr = MDC.get(CommonConstants.TENANT_ID);
            headers.set(CommonConstants.TENANT_ID, tenantIdStr != null ? tenantIdStr : (fallbackTenantId != null ? String.valueOf(fallbackTenantId) : ""));
            
            String storeIdStr = MDC.get(CommonConstants.STORE_ID);
            headers.set(CommonConstants.STORE_ID, storeIdStr != null ? storeIdStr : (fallbackStoreId != null ? String.valueOf(fallbackStoreId) : ""));
            
            headers.set(CommonConstants.AUTHORIZATION, MDC.get(CommonConstants.AUTHORIZATION));
            headers.set("clientIP", MDC.get("clientIP"));
            headers.set(CommonConstants.LATITUDE, MDC.get(CommonConstants.LATITUDE));
            headers.set(CommonConstants.LONGITUDE, MDC.get(CommonConstants.LONGITUDE));
            headers.set(CommonConstants.USER_AGENT, MDC.get(CommonConstants.USER_AGENT));
            headers.set(CommonConstants.ALLOWED_STORES, MDC.get(CommonConstants.ALLOWED_STORES));
        } catch (Exception ex) {
            logger.error("Error retrieving MDC context for headers", ex);
        } finally {
            if (!headers.containsKey(CommonConstants.USER_AGENT)
                    || headers.getFirst(CommonConstants.USER_AGENT) == null
                    || Objects.requireNonNull(headers.getFirst(CommonConstants.USER_AGENT)).isEmpty()) {

                headers.set(CommonConstants.USER_AGENT, "Default-User-Agent");
            }
            headers.set(CommonConstants.CHECK, "OK");
        }
        return headers;
    }

    public void produceMsgWithHeaders(String exchange, String routingKey, String msg, Long tenantId, Long storeId) {
        String fullExchange = env + "." + exchange;
        String fullRoutingKey = env + "." + routingKey;

        HttpHeaders headers = getDefaultHeaders(tenantId, storeId);

        rabbitTemplate.convertAndSend(fullExchange, fullRoutingKey, msg, message -> {
            if (headers != null) {
                headers.forEach((key, values) -> {
                    if (values != null && !values.isEmpty() && values.get(0) != null) {
                        message.getMessageProperties().setHeader(key, values.get(0));
                    }
                });
            }
            return message;
        });

        logger.info("Sent msg from Producer to Exchange: {} with Key: {}. Payload: {}", fullExchange, fullRoutingKey, msg);
    }

    public void sendOtpViaWhatsapp(PersonnelDetails staff, String otp) {
        try {
            MessagingVariable messagingVariable = new MessagingVariable();
            messagingVariable.setTenantId(staff.getApplicationTenantId());
            messagingVariable.setStoreId(staff.getStoreId());
            messagingVariable.setOtp(otp);
            messagingVariable.setEventName("OTP");

            messagingVariable.setRecipientName(staff.getFirstName());
            messagingVariable.setRecipientCountryCode(""); // Assuming country code is embedded in mobile number or handled by provider
            messagingVariable.setRecipientNumber(staff.getPersonnelMobileNumber());

            MessagingPayload payload = new MessagingPayload();
            payload.setNotificationTypes(List.of(NotificationType.WHATSAPP));
            
            Map<String, Object> variables = objectMapper.convertValue(messagingVariable, new TypeReference<Map<String, Object>>() {});
            payload.setVariables(variables);
            
            payload.setEntity("RESPARK");
            payload.setStoreId(messagingVariable.getStoreId());
            payload.setTenantId(messagingVariable.getTenantId());
            payload.setType("template");
            payload.setEventName(messagingVariable.getEventName());
            
            String number = (StringUtils.isNotBlank(messagingVariable.getRecipientCountryCode()) ? messagingVariable.getRecipientCountryCode() : "") + messagingVariable.getRecipientNumber();
            String recipientName = Objects.toString(messagingVariable.getRecipientName(), "").trim();
            String recipientNumber = Objects.toString(number, "").trim();
            String recipientEmail = Objects.toString(staff.getEmail(), "").trim();

            List<Map<String, String>> recipientMap = List.of(
                    Map.of(
                            "name", recipientName,
                            "number", recipientNumber,
                            "email", recipientEmail
                    )
            );
            payload.setRecipient(recipientMap);

            String json = objectMapper.writeValueAsString(payload);
            
            produceMsgWithHeaders("notification.exchange.dl", "dead.letter", json, staff.getApplicationTenantId(), staff.getStoreId());
            
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp OTP notification via RabbitMQ", e);
        }
    }
}
