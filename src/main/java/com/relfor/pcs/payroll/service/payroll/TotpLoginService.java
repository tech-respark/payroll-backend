package com.relfor.pcs.payroll.service.payroll;

import com.relfor.pcs.payroll.entity.LoginChallenge;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.repository.LoginChallengeRepository;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TotpLoginService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final LoginChallengeRepository challengeRepository;

    public TotpLoginService(PersonnelDetailsRepository personnelDetailsRepository,
                            LoginChallengeRepository challengeRepository) {
        this.personnelDetailsRepository = personnelDetailsRepository;
        this.challengeRepository = challengeRepository;
    }

    public boolean isEnabled(PersonnelDetails staff) {
        return staff != null && Boolean.TRUE.equals(staff.getTotpEnabled())
                && staff.getTotpSecret() != null && !staff.getTotpSecret().isBlank();
    }

    @Transactional
    public String createChallenge(Long staffId) {
        LoginChallenge challenge = new LoginChallenge();
        byte[] raw = new byte[24];
        RANDOM.nextBytes(raw);
        challenge.setToken(HexFormat.of().formatHex(raw));
        challenge.setStaffId(staffId);
        challenge.setExpiresAt(Instant.now().plusSeconds(300));
        challenge.setUsed(false);
        return challengeRepository.save(challenge).getToken();
    }

    @Transactional
    public PersonnelDetails consumeChallenge(String token, String code) {
        LoginChallenge challenge = challengeRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code."));
        if (Boolean.TRUE.equals(challenge.getUsed()) || challenge.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired verification code.");
        }
        PersonnelDetails staff = personnelDetailsRepository.findById(challenge.getStaffId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code."));
        if (!TotpUtil.verify(staff.getTotpSecret(), code)) {
            throw new IllegalArgumentException("Invalid or expired verification code.");
        }
        challenge.setUsed(true);
        challengeRepository.save(challenge);
        return staff;
    }

    public Map<String, Object> status() {
        PersonnelDetails staff = requireSelf();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("enabled", Boolean.TRUE.equals(staff.getTotpEnabled()));
        return view;
    }

    public Map<String, Object> start() {
        PersonnelDetails staff = requireSelf();
        String secret = TotpUtil.newSecret();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("secret", secret);
        view.put("otpauthUrl", TotpUtil.otpauthUrl("Relfor Payroll",
                staff.getUsername() != null ? staff.getUsername() : String.valueOf(staff.getId()), secret));
        return view;
    }

    @Transactional
    public Map<String, Object> confirm(String code, String secret) {
        PersonnelDetails staff = requireSelf();
        String pending = secret != null && !secret.isBlank() ? secret : staff.getTotpSecret();
        if (pending == null || pending.isBlank() || !TotpUtil.verify(pending, code)) {
            throw new IllegalArgumentException("That code did not match. Try again.");
        }
        staff.setTotpSecret(pending);
        staff.setTotpEnabled(true);
        personnelDetailsRepository.save(staff);
        return Map.of("enabled", true);
    }

    @Transactional
    public Map<String, Object> disable(String code) {
        PersonnelDetails staff = requireSelf();
        if (Boolean.TRUE.equals(staff.getTotpEnabled()) && !TotpUtil.verify(staff.getTotpSecret(), code)) {
            throw new IllegalArgumentException("That code did not match. Try again.");
        }
        staff.setTotpEnabled(false);
        staff.setTotpSecret(null);
        personnelDetailsRepository.save(staff);
        return Map.of("enabled", false);
    }

    private PersonnelDetails requireSelf() {
        Long staffId = SecurityUtils.getStaffId();
        if (staffId == null) {
            throw new IllegalArgumentException("Sign in as a staff user to manage two-factor authentication.");
        }
        return personnelDetailsRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff record not found."));
    }
}
