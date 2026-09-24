package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.entity.AttendanceRegularizationRequest;
import com.relfor.pcs.payroll.repository.AttendanceRegularizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AttendanceRegularizationService {

    private final AttendanceRegularizationRequestRepository requestRepository;

    @Transactional(readOnly = true)
    public java.util.List<AttendanceRegularizationRequest> getPendingRegularizations(Long tenantId, Long storeId, Long managerId, boolean isHrAdmin) {
        if (isHrAdmin) {
            return requestRepository.findByTenantAndStoreAndStatus(tenantId, storeId, AttendanceRegularizationRequest.RegularizationStatus.PENDING);
        } else {
            return requestRepository.findByTenantAndStoreAndReportingToAndStatus(tenantId, storeId, managerId, AttendanceRegularizationRequest.RegularizationStatus.PENDING);
        }
    }

    @Transactional
    public AttendanceRegularizationRequest requestRegularization(Long staffId, LocalDate date, LocalTime inTime, LocalTime outTime, String reason) {
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot regularize a future date.");
        }
        
        AttendanceRegularizationRequest request = AttendanceRegularizationRequest.builder()
                .staffId(staffId)
                .dateToRegularize(date)
                .requestedInTime(inTime)
                .requestedOutTime(outTime)
                .reason(reason)
                .status(AttendanceRegularizationRequest.RegularizationStatus.PENDING)
                .build();
                
        return requestRepository.save(request);
    }

    @Transactional
    public void approveRegularization(Long requestId, Long approverId) {
        AttendanceRegularizationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
                
        request.setStatus(AttendanceRegularizationRequest.RegularizationStatus.APPROVED);
        request.setApproverId(approverId);
        requestRepository.save(request);
        
        // Push the missing punch to the DayWiseAttendanceSummary / InOutHistory
    }

    @Transactional
    public void rejectRegularization(Long requestId, Long approverId) {
        AttendanceRegularizationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
                
        request.setStatus(AttendanceRegularizationRequest.RegularizationStatus.REJECTED);
        request.setApproverId(approverId);
        requestRepository.save(request);
    }
}
