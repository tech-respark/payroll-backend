package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PayrollSmsService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Send OTP SMS to the staff member directly via HTTP (Bypassing external queues).
     */
    public void sendRegistrationOTP(PersonnelDetails staff, String otp, int expiryTimeMinutes) {
        try {
            String mobileNo = staff.getPersonnelMobileNumber();
            String name = staff.getFirstName() + " " + (staff.getLastName() != null ? staff.getLastName() : "");
            
            logger.info("==========================================================");
            logger.info("PREPARING SMS DELIVERY - To: {} (Mobile: {})", name, mobileNo);
            logger.info("==========================================================");

            // TODO: Implement custom SMS logic here
            logger.info("SMS logic removed. User will implement custom SMS sending code.");

        } catch (Exception e) {
            logger.error("Error preparing OTP SMS: " + e.getMessage(), e);
        }
    }
}
