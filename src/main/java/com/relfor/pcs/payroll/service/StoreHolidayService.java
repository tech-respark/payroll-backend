package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.activity.ActivityActions;
import com.relfor.pcs.payroll.activity.ActivityEvent;
import com.relfor.pcs.payroll.activity.ActivityNames;
import com.relfor.pcs.payroll.activity.ActivityRecorder;
import com.relfor.pcs.payroll.activity.ActivityResourceTypes;
import com.relfor.pcs.payroll.dto.StoreHolidayRequest;
import com.relfor.pcs.payroll.dto.StoreHolidayResponse;
import com.relfor.pcs.payroll.entity.StoreHoliday;
import com.relfor.pcs.payroll.repository.StoreHolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreHolidayService {

    @Autowired
    private StoreHolidayRepository storeHolidayRepository;

    @Autowired
    private ActivityRecorder activityRecorder;

    @Transactional
    public StoreHolidayResponse createHoliday(StoreHolidayRequest request) {
        if (storeHolidayRepository.existsByTenantIdAndStoreIdAndHolidayDate(
                request.getTenantId(), request.getStoreId(), request.getHolidayDate())) {
            throw new IllegalArgumentException("A holiday already exists for this date.");
        }

        StoreHoliday holiday = new StoreHoliday();
        holiday.setTenantId(request.getTenantId());
        holiday.setStoreId(request.getStoreId());
        holiday.setHolidayDate(request.getHolidayDate());
        holiday.setHolidayName(request.getHolidayName());
        holiday.setIsOptional(request.getIsOptional() != null ? request.getIsOptional() : false);

        StoreHoliday saved = storeHolidayRepository.save(holiday);
        activityRecorder.record(ActivityEvent.builder()
                .action(ActivityActions.LEAVE_HOLIDAY_CREATED)
                .tenantId(saved.getTenantId())
                .storeId(saved.getStoreId())
                .resourceType(ActivityResourceTypes.HOLIDAY)
                .resourceId(saved.getId())
                .resourceLabel(saved.getHolidayName())
                .summary("Added holiday " + saved.getHolidayName() + " on " + ActivityNames.formatDate(saved.getHolidayDate()))
                .context("date", saved.getHolidayDate())
                .context("optional", saved.getIsOptional())
                .build());
        return mapToResponse(saved);
    }

    public List<StoreHolidayResponse> getHolidaysByStore(Long tenantId, Long storeId) {
        return storeHolidayRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteHoliday(Long id) {
        StoreHoliday existing = storeHolidayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holiday not found with ID: " + id));
        storeHolidayRepository.delete(existing);
        activityRecorder.record(ActivityEvent.builder()
                .action(ActivityActions.LEAVE_HOLIDAY_DELETED)
                .tenantId(existing.getTenantId())
                .storeId(existing.getStoreId())
                .resourceType(ActivityResourceTypes.HOLIDAY)
                .resourceId(existing.getId())
                .resourceLabel(existing.getHolidayName())
                .summary("Removed holiday " + existing.getHolidayName())
                .context("date", existing.getHolidayDate())
                .build());
    }

    private StoreHolidayResponse mapToResponse(StoreHoliday entity) {
        StoreHolidayResponse response = new StoreHolidayResponse();
        response.setId(entity.getId());
        response.setTenantId(entity.getTenantId());
        response.setStoreId(entity.getStoreId());
        response.setHolidayDate(entity.getHolidayDate());
        response.setHolidayName(entity.getHolidayName());
        response.setIsOptional(entity.getIsOptional());
        return response;
    }
}
