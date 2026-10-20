package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityNames;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import com.relfor.pcs.payroll.entity.AttendanceRegularizationRequest;
import com.relfor.pcs.payroll.repository.AttendanceRegularizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import com.relfor.pcs.payroll.entity.PersonnelDetails;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceRegularizationService {

    private final AttendanceRegularizationRequestRepository requestRepository;
    private final PersonnelDetailsRepository personnelDetailsRepository;
    private final EmailService emailService;
    private final ActivityRecorder activityRecorder;

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
                
        request = requestRepository.save(request);
        activityRecorder.record(ActivityEvent.builder()
                .action(ActivityActions.ATTENDANCE_REGULARIZATION_REQUESTED)
                .resourceType(ActivityResourceTypes.REGULARIZATION)
                .resourceId(request.getId())
                .resourceLabel(ActivityNames.displayName(personnelDetailsRepository.findById(staffId).orElse(null)))
                .summary("Requested punch correction for " + ActivityNames.formatDate(date))
                .context("date", date)
                .build());

        try {
            Optional<PersonnelDetails> personnelOpt = personnelDetailsRepository.findById(staffId);
            if (personnelOpt.isPresent()) {
                PersonnelDetails personnel = personnelOpt.get();
                String applierEmail = personnel.getEmail();
                String managerEmail = null;
                if (personnel.getReportingTo() != null) {
                    Optional<PersonnelDetails> manager = personnelDetailsRepository.findById(personnel.getReportingTo());
                    if (manager.isPresent()) {
                        managerEmail = manager.get().getEmail();
                    }
                }
                String name = personnel.getFirstName() + (personnel.getLastName() != null ? " " + personnel.getLastName() : "");
                if (applierEmail != null) {
                    emailService.sendRegularizationAppliedEmail(applierEmail, name, personnel.getEmployeeCode(), String.valueOf(request.getId()), LocalDate.now().toString(), request.getReason() != null ? request.getReason() : "", request.getDateToRegularize().toString(), request.getRequestedInTime() != null ? request.getRequestedInTime().toString() : "", request.getRequestedOutTime() != null ? request.getRequestedOutTime().toString() : "", "");
                }
                if (managerEmail != null) {
                    emailService.sendEmail(managerEmail, "New Regularization Request", "A new attendance regularization request has been submitted by " + name + ".");
                }
            }
        } catch (Exception e) {
            // Ignored to avoid breaking transaction
        }

        return request;
    }

    @Transactional
    public void approveRegularization(Long requestId, Long approverId) {
        AttendanceRegularizationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
                
        request.setStatus(AttendanceRegularizationRequest.RegularizationStatus.APPROVED);
        request.setApproverId(approverId);
        requestRepository.save(request);
        activityRecorder.record(regularizationEvent(request, ActivityActions.ATTENDANCE_REGULARIZATION_APPROVED, "Approved punch correction"));

        try {
            Optional<PersonnelDetails> personnelOpt = personnelDetailsRepository.findById(request.getStaffId());
            if (personnelOpt.isPresent() && personnelOpt.get().getEmail() != null) {
                PersonnelDetails personnel = personnelOpt.get();
                String name = personnel.getFirstName() + (personnel.getLastName() != null ? " " + personnel.getLastName() : "");
                emailService.sendRegularizationApprovedEmail(personnel.getEmail(), name, personnel.getEmployeeCode(), String.valueOf(request.getId()), LocalDate.now().toString(), request.getReason() != null ? request.getReason() : "", request.getDateToRegularize().toString(), request.getRequestedInTime() != null ? request.getRequestedInTime().toString() : "", request.getRequestedOutTime() != null ? request.getRequestedOutTime().toString() : "", "");
            }
        } catch (Exception e) {
            // Ignored to avoid breaking transaction
        }
    }

    @Transactional
    public void rejectRegularization(Long requestId, Long approverId) {
        AttendanceRegularizationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
                
        request.setStatus(AttendanceRegularizationRequest.RegularizationStatus.REJECTED);
        request.setApproverId(approverId);
        requestRepository.save(request);
        activityRecorder.record(regularizationEvent(request, ActivityActions.ATTENDANCE_REGULARIZATION_REJECTED, "Rejected punch correction"));

        try {
            Optional<PersonnelDetails> personnelOpt = personnelDetailsRepository.findById(request.getStaffId());
            if (personnelOpt.isPresent() && personnelOpt.get().getEmail() != null) {
                PersonnelDetails personnel = personnelOpt.get();
                String name = personnel.getFirstName() + (personnel.getLastName() != null ? " " + personnel.getLastName() : "");
                emailService.sendRegularizationRejectedEmail(personnel.getEmail(), name, personnel.getEmployeeCode(), String.valueOf(request.getId()), LocalDate.now().toString(), request.getReason() != null ? request.getReason() : "", request.getDateToRegularize().toString(), request.getRequestedInTime() != null ? request.getRequestedInTime().toString() : "", request.getRequestedOutTime() != null ? request.getRequestedOutTime().toString() : "", "");
            }
        } catch (Exception e) {
            // Ignored to avoid breaking transaction
        }
    }

    private ActivityEvent regularizationEvent(AttendanceRegularizationRequest request, String action, String verb) {
        String name = ActivityNames.displayName(personnelDetailsRepository.findById(request.getStaffId()).orElse(null));
        return ActivityEvent.builder()
                .action(action)
                .resourceType(ActivityResourceTypes.REGULARIZATION)
                .resourceId(request.getId())
                .resourceLabel(name)
                .summary(verb + " for " + (name != null ? name : "staff") + " on " + ActivityNames.formatDate(request.getDateToRegularize()))
                .context("date", request.getDateToRegularize())
                .build();
    }
}
