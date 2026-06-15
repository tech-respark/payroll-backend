package com.relfor.pcs.payroll.repository;

import com.relfor.pcs.payroll.entity.PersonnelSalaryComponents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PersonnelSalaryComponentsRepository extends JpaRepository<PersonnelSalaryComponents, Long> {
    @Query(value = "SELECT * FROM personnel_salary_components pd \n" +
            "WHERE personnel_code = :personnelCode ;", nativeQuery = true)
    List<PersonnelSalaryComponents> findByPersonnelCode(Long personnelCode);
}