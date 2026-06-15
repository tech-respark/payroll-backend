package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class StaffResponseDTO {
	private Long id;
	private String firstName;
	private String lastName;
	private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String pwd;
	private String email;
	private String mobile;
	private String phone;
	private String gender;
	private String address;
	private Long tenantId;
	private Integer active;
	private String speciality;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String passcode;
	private Float experience;
	private String description;
	private String profileImage;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String otp;
	private Integer enableAppointments;
	private Boolean allStaffAppointmentDashboard;
	private String[] weeklyOff;
	private Boolean owner;
	private Boolean areaManager;
	private Boolean doctor;
	private Boolean consultant;
	private String birthDate;
	private String joiningDate;
	private String designation;
	private BigDecimal salaryAmount = BigDecimal.ZERO;
	private Boolean receiveOnlineNotifications;
	private Boolean receiveNotificationsOfAll;
	private Boolean isBiometricIntegration;
	private Boolean isSyncedWithPersonnelManagement;
	private Boolean isSyncedWithBiometricDevice;
	private String applicationName;
	private Integer displayRank = 0; // Used for sorting in the list of staff members
    private Boolean hasPassword;
	private Boolean isPasswordUpdated;
	private PersonnelJoiningDetailsDTO personnelJoiningDetails;
	private List<PersonnelWorkExperienceDetailsDTO> personnelWorkExperienceDetailsList;
	private PersonnelBankAccountDetailsDTO personnelBankAccountDetails;
	private List<PersonnelDocumentDetailsDTO> personnelDocumentDetailsList;
	private List<PersonnelEmergencyContactsDTO> personnelEmergencyContactsList;

	public List<PersonnelEmergencyContactsDTO> getPersonnelEmergencyContactsList() {
		return personnelEmergencyContactsList;
	}

	public void setPersonnelEmergencyContactsList(List<PersonnelEmergencyContactsDTO> personnelEmergencyContactsList) {
		this.personnelEmergencyContactsList = personnelEmergencyContactsList;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

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

	public float getExperience() {
		return experience;
	}

	public void setExperience(float experience) {
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

	public Boolean isAllStaffAppointmentDashboard() {
		return allStaffAppointmentDashboard;
	}

	public void setAllStaffAppointmentDashboard(Boolean allStaffAppointmentDashboard) {
		this.allStaffAppointmentDashboard = allStaffAppointmentDashboard;
	}

	public String[] getWeeklyOff() {
		return weeklyOff;
	}

	public void setWeeklyOff(String[] weeklyOff) {
		this.weeklyOff = weeklyOff;
	}

	public Boolean isOwner() {
		return owner;
	}

	public void setOwner(Boolean owner) {
		this.owner = owner;
	}

	public Boolean isAreaManager() {
		return areaManager;
	}

	public void setAreaManager(Boolean areaManager) {
		this.areaManager = areaManager;
	}

	public Boolean isDoctor() {
		return doctor;
	}

	public void setDoctor(Boolean doctor) {
		this.doctor = doctor;
	}

	public Boolean isConsultant() {
		return consultant;
	}

	public void setConsultant(Boolean consultant) {
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

	public BigDecimal getSalaryAmount() {
		return salaryAmount;
	}

	public void setSalaryAmount(BigDecimal salaryAmount) {
		this.salaryAmount = salaryAmount;
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

	public Boolean getIsBiometricIntegration() {
		return isBiometricIntegration;
	}

	public void setIsBiometricIntegration(Boolean biometricIntegration) {
		isBiometricIntegration = biometricIntegration;
	}

	public Boolean getIsSyncedWithPersonnelManagement() {
		return isSyncedWithPersonnelManagement;
	}

	public void setIsSyncedWithPersonnelManagement(Boolean syncedWithPersonnelManagement) {
		isSyncedWithPersonnelManagement = syncedWithPersonnelManagement;
	}

	public Boolean getIsSyncedWithBiometricDevice() {
		return isSyncedWithBiometricDevice;
	}

	public void setIsSyncedWithBiometricDevice(Boolean syncedWithBiometricDevice) {
		isSyncedWithBiometricDevice = syncedWithBiometricDevice;
	}

	public PersonnelJoiningDetailsDTO getPersonnelJoiningDetails() {
		return personnelJoiningDetails;
	}

	public void setPersonnelJoiningDetails(PersonnelJoiningDetailsDTO personnelJoiningDetails) {
		this.personnelJoiningDetails = personnelJoiningDetails;
	}

	public List<PersonnelWorkExperienceDetailsDTO> getPersonnelWorkExperienceDetailsList() {
		return personnelWorkExperienceDetailsList;
	}

	public void setPersonnelWorkExperienceDetailsList(List<PersonnelWorkExperienceDetailsDTO> personnelWorkExperienceDetailsList) {
		this.personnelWorkExperienceDetailsList = personnelWorkExperienceDetailsList;
	}

	public PersonnelBankAccountDetailsDTO getPersonnelBankAccountDetails() {
		return personnelBankAccountDetails;
	}

	public void setPersonnelBankAccountDetails(PersonnelBankAccountDetailsDTO personnelBankAccountDetails) {
		this.personnelBankAccountDetails = personnelBankAccountDetails;
	}

	public List<PersonnelDocumentDetailsDTO> getPersonnelDocumentDetailsList() {
		return personnelDocumentDetailsList;
	}

	public void setPersonnelDocumentDetailsList(List<PersonnelDocumentDetailsDTO> personnelDocumentDetailsList) {
		this.personnelDocumentDetailsList = personnelDocumentDetailsList;
	}

	public int getDisplayRank() {
		return displayRank;
	}

	public void setDisplayRank(int displayRank) {
		this.displayRank = displayRank;
	}

    public Boolean getHasPassword() {
        return hasPassword;
    }

    public void setHasPassword(Boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

	public Boolean getPasswordUpdated() {
		return isPasswordUpdated;
	}

	public void setPasswordUpdated(Boolean passwordUpdated) {
		isPasswordUpdated = passwordUpdated;
	}
}
