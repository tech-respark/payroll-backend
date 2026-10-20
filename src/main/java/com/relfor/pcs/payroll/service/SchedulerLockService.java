package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.entity.SchedulerLock;
import com.relfor.pcs.payroll.repository.SchedulerLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class SchedulerLockService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerLockService.class);

    private final SchedulerLockRepository schedulerLockRepository;
    private final String instanceId;

    public SchedulerLockService(SchedulerLockRepository schedulerLockRepository) {
        this.schedulerLockRepository = schedulerLockRepository;
        this.instanceId = resolveInstanceId();
    }

    @Transactional
    public boolean tryAcquire(String lockName, Duration ttl) {
        Instant now = Instant.now();
        Instant until = now.plus(ttl);
        SchedulerLock lock = schedulerLockRepository.findByLockNameForUpdate(lockName).orElse(null);
        if (lock != null && lock.getLockedUntil() != null && lock.getLockedUntil().isAfter(now)) {
            logger.info("Scheduler lock {} is held until {}", lockName, lock.getLockedUntil());
            return false;
        }
        if (lock == null) {
            lock = new SchedulerLock();
            lock.setLockName(lockName);
        }
        lock.setLockedUntil(until);
        lock.setLockedBy(instanceId);
        schedulerLockRepository.save(lock);
        return true;
    }

    private String resolveInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + ProcessHandle.current().pid();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
