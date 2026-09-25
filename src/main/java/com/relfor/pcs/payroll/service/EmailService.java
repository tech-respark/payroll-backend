package com.relfor.pcs.payroll.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            if (to == null || to.trim().isEmpty()) {
                logger.warn("Skipping email send. Recipient address is null or empty.");
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}", to, e);
        }
    }

    private String getLeaveTemplate(String statusText, String name, String code, String appNumber, String appDate, String leaveType, String startDate, String endDate, String reason) {
        return "Dear " + name + " (" + code + ") ,\n\n" +
               "We pleased to inform you that your Leave application has been " + statusText + ".\n" +
               "The Leave details are as follows : -\n\n" +
               "Application Number : " + appNumber + "\n" +
               "Application Date : " + appDate + "\n" +
               "Type : " + leaveType + "\n" +
               "Date : " + startDate + " To " + endDate + "\n" +
               "Reason : " + reason + "\n\n" +
               "Please login to Paysquare Web Portal mylms.paysquare.com to confirm leave details.\n\n" +
               "Best Regards,\n" +
               "Relfor Labs Pvt Ltd Payroll,\n" +
               "Paysquare Consultancy Limited | mypayroll.paysquare.com\n\n" +
               "This is a system generated e-mail. We request you not to reply to this e-mail.\n" +
               "This email may contain confidential information and/or copyright material. This email is intended for the use of the addressee only. Any unauthorized use may be unlawful. If you receive this email by mistake, please advise the sender immediately by using the reply facility in your email software.\n" +
               "This email has been scanned for all known viruses";
    }

    private String getRegularizationTemplate(String statusText, String name, String code, String appNumber, String appDate, String reason, String date, String inTime, String outTime, String remark) {
        return "Dear " + name + " (" + code + "),\n" +
               "This is to confirm that your Attendance Regularization Application has been " + statusText + " on our Web Portal. The application details are as follows :-\n" +
               "Application Number : " + appNumber + "\n" +
               "Application Date : " + appDate + "\n" +
               "Type : " + reason + "\n" +
               "Date : " + date + " - " + date + "\n" +
               "Time : " + inTime + " - " + outTime + "\n" +
               "Remark : " + remark + "\n" +
               "This application will go through approval process as laid down by your company.\n" +
               "In case you want to cancel the application at a later stage, you may do so by clicking the cancellation tab on the Paysquare Web Portal.\n" +
               "Best Regards,\n" +
               "Relfor Labs Pvt Ltd Payroll,\n" +
               "Paysquare Consultancy Limited | mypayroll.paysquare.com\n" +
               "This is a system generated e-mail. We request you not to reply to this e-mail.\n" +
               "This email may contain confidential information and/or copyright material. This email is intended for the use of the addressee only. Any unauthorized use may be unlawful. If you receive this email by mistake, please advise the sender immediately by using the reply facility in your email software.\n" +
               "This email has been scanned for all known viruses.";
    }

    @Async
    public void sendLeaveAppliedEmail(String to, String name, String code, String appNumber, String appDate, String leaveType, String startDate, String endDate, String reason) {
        String body = getLeaveTemplate("registered", name, code, appNumber, appDate, leaveType, startDate, endDate, reason);
        sendEmail(to, "Leave Application Submitted", body);
    }

    @Async
    public void sendLeaveApprovedEmail(String to, String name, String code, String appNumber, String appDate, String leaveType, String startDate, String endDate, String reason) {
        String body = getLeaveTemplate("approved", name, code, appNumber, appDate, leaveType, startDate, endDate, reason);
        sendEmail(to, "Leave Approved", body);
    }

    @Async
    public void sendLeaveRejectedEmail(String to, String name, String code, String appNumber, String appDate, String leaveType, String startDate, String endDate, String reason) {
        String body = getLeaveTemplate("rejected", name, code, appNumber, appDate, leaveType, startDate, endDate, reason);
        sendEmail(to, "Leave Rejected", body);
    }

    @Async
    public void sendRegularizationAppliedEmail(String to, String name, String code, String appNumber, String appDate, String reason, String date, String inTime, String outTime, String remark) {
        String body = getRegularizationTemplate("registered", name, code, appNumber, appDate, reason, date, inTime, outTime, remark);
        sendEmail(to, "Regularization Request Submitted", body);
    }

    @Async
    public void sendRegularizationApprovedEmail(String to, String name, String code, String appNumber, String appDate, String reason, String date, String inTime, String outTime, String remark) {
        String body = getRegularizationTemplate("approved", name, code, appNumber, appDate, reason, date, inTime, outTime, remark);
        sendEmail(to, "Regularization Approved", body);
    }

    @Async
    public void sendRegularizationRejectedEmail(String to, String name, String code, String appNumber, String appDate, String reason, String date, String inTime, String outTime, String remark) {
        String body = getRegularizationTemplate("rejected", name, code, appNumber, appDate, reason, date, inTime, outTime, remark);
        sendEmail(to, "Regularization Rejected", body);
    }
}
