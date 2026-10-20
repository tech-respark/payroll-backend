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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final String GENERIC_OTP_MESSAGE = "If an account exists for that identifier, an OTP has been sent.";
    private static final String GENERIC_OTP_FAILURE = "OTP is invalid or has expired.";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private PersonnelDetailsRepository personnelDetailsRepository;

    @Autowired
    private StaffPasswordResetOTPRepository otpRepository;

    @Autowired
    private WhatsappNotificationService whatsappNotificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseModel generateOtp(String usernameOrMobile) {
        ResponseModel response = new ResponseModel();
        response.setCode(HttpStatus.OK);
        response.setMessage(GENERIC_OTP_MESSAGE);
        try {
            Optional<PersonnelDetails> staffOpt = findStaffForPasswordReset(usernameOrMobile);
            if (staffOpt.isEmpty()) {
                return response;
            }

            PersonnelDetails staff = staffOpt.get();
            if (staff.getActive() == null || !staff.getActive()) {
                return response;
            }

            List<StaffPasswordResetOTP> existingOtps = otpRepository.findValidOtpsByStaffId(staff.getId(), Instant.now());
            for (StaffPasswordResetOTP existing : existingOtps) {
                existing.setExpired(true);
                otpRepository.save(existing);
            }

            String otpStr = String.format("%06d", secureRandom.nextInt(1_000_000));
            int expiryMinutes = 5;

            StaffPasswordResetOTP otpRecord = new StaffPasswordResetOTP();
            otpRecord.setStaffId(staff.getId());
            otpRecord.setMobileNo(staff.getPersonnelMobileNumber());
            otpRecord.setEmail(staff.getEmail());
            otpRecord.setOtp(passwordEncoder.encode(otpStr));
            otpRecord.setGenerationTime(Instant.now());
            otpRecord.setExpiryTime(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES));
            otpRepository.save(otpRecord);

            whatsappNotificationService.sendOtpViaWhatsapp(staff, otpStr);
        } catch (Exception e) {
            logger.error("Error generating OTP", e);
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setMessage("Unable to process the request.");
        }
        return response;
    }

    public ResponseModel verifyOtp(String usernameOrMobile, String otp) {
        ResponseModel response = new ResponseModel();
        try {
            Optional<PersonnelDetails> staffOpt = findStaffForPasswordReset(usernameOrMobile);
            if (staffOpt.isEmpty() || otp == null || otp.isBlank()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage(GENERIC_OTP_FAILURE);
                return response;
            }

            PersonnelDetails staff = staffOpt.get();
            List<StaffPasswordResetOTP> validOtps = otpRepository.findValidOtpsByStaffId(staff.getId(), Instant.now());

            if (validOtps.isEmpty()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage(GENERIC_OTP_FAILURE);
                return response;
            }

            StaffPasswordResetOTP currentOtp = validOtps.get(0);

            if (currentOtp.getFailedAttempts() >= 3) {
                currentOtp.setExpired(true);
                otpRepository.save(currentOtp);
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage(GENERIC_OTP_FAILURE);
                return response;
            }

            if (!passwordEncoder.matches(otp, currentOtp.getOtp())) {
                currentOtp.setFailedAttempts(currentOtp.getFailedAttempts() + 1);
                otpRepository.save(currentOtp);
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage(GENERIC_OTP_FAILURE);
                return response;
            }

            currentOtp.setVerified(true);
            String resetToken = UUID.randomUUID().toString();
            currentOtp.setResetToken(sha256(resetToken));
            otpRepository.save(currentOtp);

            response.setCode(HttpStatus.OK);
            response.setMessage("OTP verified successfully.");
            response.setData(resetToken);
        } catch (Exception e) {
            logger.error("Error verifying OTP", e);
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setMessage("Unable to process the request.");
        }
        return response;
    }

    public ResponseModel resetPassword(String resetToken, String newPassword) {
        ResponseModel response = new ResponseModel();
        try {
            if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
                return response;
            }

            if (resetToken == null || resetToken.isBlank()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Invalid or expired reset token.");
                return response;
            }

            Optional<StaffPasswordResetOTP> otpOpt = otpRepository.findByResetTokenAndExpiredFalse(sha256(resetToken));

            if (otpOpt.isEmpty()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Invalid or expired reset token.");
                return response;
            }

            StaffPasswordResetOTP otpRecord = otpOpt.get();

            if (!otpRecord.isVerified()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Invalid or expired reset token.");
                return response;
            }

            Optional<PersonnelDetails> staffOpt = personnelDetailsRepository.findById(otpRecord.getStaffId());
            if (staffOpt.isEmpty()) {
                response.setCode(HttpStatus.BAD_REQUEST);
                response.setMessage("Invalid or expired reset token.");
                return response;
            }

            PersonnelDetails staff = staffOpt.get();
            staff.setPassword(passwordEncoder.encode(newPassword));
            personnelDetailsRepository.save(staff);

            otpRecord.setExpired(true);
            otpRepository.save(otpRecord);

            response.setCode(HttpStatus.OK);
            response.setMessage("Password has been reset successfully.");
        } catch (Exception e) {
            logger.error("Error resetting password", e);
            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setMessage("Unable to process the request.");
        }
        return response;
    }

    private Optional<PersonnelDetails> findStaffForPasswordReset(String usernameOrMobile) {
        if (usernameOrMobile == null || usernameOrMobile.isBlank()) {
            return Optional.empty();
        }
        String value = usernameOrMobile.trim();
        Optional<PersonnelDetails> staffOpt = personnelDetailsRepository.findByUsername(value);
        if (staffOpt.isEmpty()) {
            staffOpt = personnelDetailsRepository.findByEmail(value);
        }
        if (staffOpt.isEmpty()) {
            staffOpt = personnelDetailsRepository.findByPersonnelMobileNumber(value);
        }
        return staffOpt;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required", e);
        }
    }
}
