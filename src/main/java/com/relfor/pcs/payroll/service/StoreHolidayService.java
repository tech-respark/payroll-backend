package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.StoreHolidayRequest;
import com.relfor.pcs.payroll.dto.StoreHolidayResponse;
import com.relfor.pcs.payroll.entity.StoreHoliday;
import com.relfor.pcs.payroll.repository.StoreHolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreHolidayService {

    @Autowired
    private StoreHolidayRepository storeHolidayRepository;

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
        return mapToResponse(saved);
    }

    public List<StoreHolidayResponse> getHolidaysByStore(Long tenantId, Long storeId) {
        return storeHolidayRepository.findByTenantIdAndStoreId(tenantId, storeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteHoliday(Long id) {
        if (!storeHolidayRepository.existsById(id)) {
            throw new IllegalArgumentException("Holiday not found with ID: " + id);
        }
        storeHolidayRepository.deleteById(id);
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
