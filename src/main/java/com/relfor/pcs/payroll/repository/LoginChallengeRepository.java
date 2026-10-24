package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.LoginChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginChallengeRepository extends JpaRepository<LoginChallenge, Long> {
    Optional<LoginChallenge> findByToken(String token);
}
