package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.StaffPasswordResetOTP;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.repository.StaffPasswordResetOTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PersonnelDetailsRepository personnelDetailsRepository;

    @Autowired
    private StaffPasswordResetOTPRepository otpRepository;

    @Autowired
    private PayrollSmsService smsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseModel generateOtp(String usernameOrMobile) {
        ResponseModel response = new ResponseModel();
        try {
            Optional<PersonnelDetails> staffOpt = personnelDetailsRepository.findByUsername(usernameOrMobile);
            
            // If not found by username, try email, or mobile (if exists, but we only have findByEmail or username in repo, let's just stick to username/email for now or write findByPersonnelMobileNumber if needed)
            if (!staffOpt.isPresent()) {
                staffOpt = personnelDetailsRepository.findByEmail(usernameOrMobile);
            }
            
            if (!staffOpt.isPresent()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("User not found with provided username or email.");
                return response;
            }

            PersonnelDetails staff = staffOpt.get();

            // Invalidate existing OTPs
            List<StaffPasswordResetOTP> existingOtps = otpRepository.findValidOtpsByStaffId(staff.getId(), Instant.now());
            for (StaffPasswordResetOTP existing : existingOtps) {
                existing.setExpired(true);
                otpRepository.save(existing);
            }

            // Generate 6-digit OTP
            String otpStr = String.format("%06d", new Random().nextInt(999999));
            int expiryMinutes = 5;

            StaffPasswordResetOTP otpRecord = new StaffPasswordResetOTP();
            otpRecord.setStaffId(staff.getId());
            otpRecord.setMobileNo(staff.getPersonnelMobileNumber());
            otpRecord.setEmail(staff.getEmail());
            otpRecord.setOtp(otpStr);
            otpRecord.setGenerationTime(Instant.now());
            otpRecord.setExpiryTime(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES));
            otpRepository.save(otpRecord);

            // Send SMS
            smsService.sendRegistrationOTP(staff, otpStr, expiryMinutes);

            response.setCode(HttpStatus.OK);
            response.setMessage("OTP sent successfully. Please check your registered mobile number.");
        } catch (Exception e) {
            logger.error("Error generating OTP", e);
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setMessage("Failed to generate OTP.");
        }
        return response;
    }

    public ResponseModel verifyOtp(String usernameOrMobile, String otp) {
        ResponseModel response = new ResponseModel();
        try {
            Optional<PersonnelDetails> staffOpt = personnelDetailsRepository.findByUsername(usernameOrMobile);
            if (!staffOpt.isPresent()) {
                staffOpt = personnelDetailsRepository.findByEmail(usernameOrMobile);
            }

            if (!staffOpt.isPresent()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("User not found.");
                return response;
            }

            PersonnelDetails staff = staffOpt.get();
            List<StaffPasswordResetOTP> validOtps = otpRepository.findValidOtpsByStaffId(staff.getId(), Instant.now());

            if (validOtps.isEmpty()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("OTP is invalid or has expired.");
                return response;
            }

            StaffPasswordResetOTP currentOtp = validOtps.get(0);

            if (currentOtp.getFailedAttempts() >= 3) {
                currentOtp.setExpired(true);
                otpRepository.save(currentOtp);
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Too many failed attempts. OTP is now expired.");
                return response;
            }

            if (!currentOtp.getOtp().equals(otp)) {
                currentOtp.setFailedAttempts(currentOtp.getFailedAttempts() + 1);
                otpRepository.save(currentOtp);
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Incorrect OTP.");
                return response;
            }

            // Correct OTP
            currentOtp.setVerified(true);
            String resetToken = UUID.randomUUID().toString();
            currentOtp.setResetToken(resetToken);
            otpRepository.save(currentOtp);

            response.setCode(HttpStatus.OK);
            response.setMessage("OTP verified successfully.");
            response.setData(resetToken); // Return token for next step
        } catch (Exception e) {
            logger.error("Error verifying OTP", e);
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setMessage("Failed to verify OTP.");
        }
        return response;
    }

    public ResponseModel resetPassword(String resetToken, String newPassword) {
        ResponseModel response = new ResponseModel();
        try {
            Optional<StaffPasswordResetOTP> otpOpt = otpRepository.findByResetTokenAndExpiredFalse(resetToken);
            
            if (!otpOpt.isPresent()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Invalid or expired reset token.");
                return response;
            }

            StaffPasswordResetOTP otpRecord = otpOpt.get();
            
            if (!otpRecord.isVerified()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("OTP must be verified before resetting password.");
                return response;
            }

            Optional<PersonnelDetails> staffOpt = personnelDetailsRepository.findById(otpRecord.getStaffId());
            if (!staffOpt.isPresent()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Staff record not found.");
                return response;
            }

            PersonnelDetails staff = staffOpt.get();
            staff.setPassword(passwordEncoder.encode(newPassword));
            personnelDetailsRepository.save(staff);

            // Invalidate the token
            otpRecord.setExpired(true);
            otpRepository.save(otpRecord);

            response.setCode(HttpStatus.OK);
            response.setMessage("Password has been reset successfully.");
        } catch (Exception e) {
            logger.error("Error resetting password", e);
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setMessage("Failed to reset password.");
        }
        return response;
    }
}
