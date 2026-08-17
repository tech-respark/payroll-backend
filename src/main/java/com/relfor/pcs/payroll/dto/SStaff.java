package com.relfor.pcs.payroll.dto;

import java.math.BigDecimal;

public class SStaff {

	private long id;
	private String firstName;
	private String lastName;
	private String username;
	private String pwd;
	private String email;
	private String mobile;
	private String phone;
	private String gender;
	private String address;
	private long tenantId;
	private int active;
	private String speciality;
	private String passcode;
	private Float experience;
	private String description;
	private String profileImage;
	private String otp;
	private int enableAppointments;
	private boolean allStaffAppointmentDashboard;
	private String[] weeklyOff;
	private boolean owner;
	private boolean areaManager;
	private boolean doctor;
	private boolean consultant;
	private String birthDate;
	private String joiningDate;
	private String designation;
	private BigDecimal salaryAmount = BigDecimal.ZERO;
	private Boolean receiveOnlineNotifications;
	private Boolean receiveNotificationsOfAll;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getTenantId() {
		return tenantId;
	}

	public void setTenantId(long tenantId) {
		this.tenantId = tenantId;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public String getSpeciality() {
		return speciality;
	}

	public void setSpeciality(String speciality) {
		this.speciality = speciality;
	}

	public String getPasscode() {
		return passcode;
	}

	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}

	public Float getExperience() {
		return experience;
	}

	public void setExperience(Float experience) {
		this.experience = experience;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public int getEnableAppointments() {
		return enableAppointments;
	}

	public void setEnableAppointments(int enableAppointments) {
		this.enableAppointments = enableAppointments;
	}

	public boolean isAllStaffAppointmentDashboard() {
		return allStaffAppointmentDashboard;
	}

	public void setAllStaffAppointmentDashboard(boolean allStaffAppointmentDashboard) {
		this.allStaffAppointmentDashboard = allStaffAppointmentDashboard;
	}

	public String[] getWeeklyOff() {
		return weeklyOff;
	}

	public void setWeeklyOff(String[] weeklyOff) {
		this.weeklyOff = weeklyOff;
	}

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public boolean isAreaManager() {
		return areaManager;
	}

	public void setAreaManager(boolean areaManager) {
		this.areaManager = areaManager;
	}

	public boolean isDoctor() {
		return doctor;
	}

	public void setDoctor(boolean doctor) {
		this.doctor = doctor;
	}

	public boolean isConsultant() {
		return consultant;
	}

	public void setConsultant(boolean consultant) {
		this.consultant = consultant;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getJoiningDate() {
		return joiningDate;
	}

	public void setJoiningDate(String joiningDate) {
		this.joiningDate = joiningDate;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public Boolean getReceiveOnlineNotifications() {
		return receiveOnlineNotifications;
	}

	public void setReceiveOnlineNotifications(Boolean receiveOnlineNotifications) {
		this.receiveOnlineNotifications = receiveOnlineNotifications;
	}

	public Boolean getReceiveNotificationsOfAll() {
		return receiveNotificationsOfAll;
	}

	public void setReceiveNotificationsOfAll(Boolean receiveNotificationsOfAll) {
		this.receiveNotificationsOfAll = receiveNotificationsOfAll;
	}

	public BigDecimal getSalaryAmount() {
		return salaryAmount;
	}

	public void setSalaryAmount(BigDecimal salaryAmount) {
		this.salaryAmount = salaryAmount;
	}
}
