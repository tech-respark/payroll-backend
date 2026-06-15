package com.relfor.pcs.payroll.repository;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.relfor.pcs.payroll.entity.StaffBookedSlots;

@Repository
public interface StaffBookedRepository extends JpaRepository<StaffBookedSlots, Long> {

	@Query(value = "SELECT  st.booked_slot from staff_booked_slots st  WHERE st.appointment_date = ?1 and st.staff_id = ?2 and st.is_canceled = 0", nativeQuery = true)
	List<String> findByAppointmentDateAndStaffId(String date, long staffId);

	List<StaffBookedSlots> findByAppointmentId(String appointmentId);

	long deleteByAppointmentId(String appointmentId);

	@Modifying
	@Transactional
	@Query(value = "Update staff_booked_slots st set st.is_canceled = 1, st.modified_on = NOW() WHERE st.appointment_id = ?1", nativeQuery = true)
	void cancelStaffSlot(String appoitmentId);
}