package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.entity.PersonnelDetails;
import com.relfor.pcs.payroll.entity.SStaffShifts;
import com.relfor.pcs.payroll.repository.PersonnelDetailsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StaffIdentityService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PersonnelDetailsRepository personnelDetailsRepository;

    public List<Long> rosterKeysForStaffId(Long staffId) {
        LinkedHashSet<Long> keys = new LinkedHashSet<>();
        if (staffId == null) {
            return new ArrayList<>();
        }
        keys.add(staffId);
        personnelDetailsRepository.findById(staffId).ifPresent(addIdentityKeys(keys));
        personnelDetailsRepository.findByPersonnelCode(staffId).ifPresent(addIdentityKeys(keys));
        return new ArrayList<>(keys);
    }

    public List<SStaffShifts> canonicalizeShiftStaffIds(List<SStaffShifts> shifts) {
        if (shifts == null || shifts.isEmpty()) {
            return shifts;
        }
        Set<Long> storedIds = shifts.stream()
                .map(SStaffShifts::getStaffId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (storedIds.isEmpty()) {
            return shifts;
        }
        Map<Long, Long> codeToCanonicalId = new HashMap<>();
        for (PersonnelDetails personnel : personnelDetailsRepository.findByPersonnelCodeIn(storedIds)) {
            if (personnel.getPersonnelCode() != null) {
                codeToCanonicalId.put(personnel.getPersonnelCode(), personnel.getId());
            }
        }
        for (SStaffShifts shift : shifts) {
            Long mapped = codeToCanonicalId.get(shift.getStaffId());
            if (mapped != null && !mapped.equals(shift.getStaffId())) {
                Hibernate.initialize(shift.getStaffBreakTime());
                entityManager.detach(shift);
                shift.setStaffId(mapped);
            }
        }
        return shifts;
    }

    private java.util.function.Consumer<PersonnelDetails> addIdentityKeys(Set<Long> keys) {
        return personnel -> {
            if (personnel.getId() != null) {
                keys.add(personnel.getId());
            }
            if (personnel.getPersonnelCode() != null && personnel.getPersonnelCode() > 0) {
                keys.add(personnel.getPersonnelCode());
            }
        };
    }
}
