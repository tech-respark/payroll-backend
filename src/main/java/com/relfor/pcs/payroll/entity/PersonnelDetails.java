package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "personnel_details")
public class PersonnelDetails extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private Long id;
	private Long tenantCompanyMappingId;
	@Column(length = 50)
	private String firstName;
	@Column(length = 50)
	private String lastName;
	@Column(length = 15)
	private String gender;
	private String designation;
	private String personnelMobileNumber;
	private String applicationName;
	private Long applicationTenantId;
	private Boolean active;
	@OneToOne(mappedBy = "personnelDetails", cascade = CascadeType.ALL)
	private PersonnelBankAccountDetails personnelBankAccountDetails;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "personnel_details_id", referencedColumnName = "id")
	private List<PersonnelWorkExperienceDetails> personnelWorkExperienceDetails;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "personnel_details_id", referencedColumnName = "id")
	private List<PersonnelEmergencyContactDetails> personnelEmergencyContactDetails;
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "personnel_details_id", referencedColumnName = "id")
	private List<PersonnelDocumentDetails> personnelDocumentDetails;
	private String employeeCode;
	private Long reportingTo;
	private Long storeId;
	private String uanNumber;
	private Float workingHours;
	private String username;
	private String password;
	private String email;
	private String address;
	private String speciality;
	private String phone;
	private String passcode;
	private Float experience;
	private String description;
	private String profileImage;
	private String otp;
	@ColumnDefault("true")
	private Boolean enableAppointments = true;
	@ColumnDefault("true")
	private Boolean allStaffAppointmentDashboard = true;
	private String weeklyOff;
	@ColumnDefault("false")
	private Boolean owner = false;
	@ColumnDefault("false")
	private Boolean areaManager = false;
	private Boolean doctor;
	private Boolean consultant;
	private String birthDate;
	private String joiningDate;
	@ColumnDefault("false")
	private Boolean receiveOnlineNotifications;
	@ColumnDefault("false")
	private Boolean receiveNotificationsOfAll;
	@Column(name = "salaryAmount", columnDefinition = "Decimal(19,2) default '0'")
	private BigDecimal salaryAmount = BigDecimal.ZERO;
	private Integer displayRank;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSpeciality() {
		return speciality;
	}

	public void setSpeciality(String speciality) {
		this.speciality = speciality;
	}

	public Boolean getEnableAppointments() {
		return enableAppointments;
	}

	public void setEnableAppointments(Boolean enableAppointments) {
		this.enableAppointments = enableAppointments;
	}

	public Boolean getAllStaffAppointmentDashboard() {
		return allStaffAppointmentDashboard;
	}

	public void setAllStaffAppointmentDashboard(Boolean allStaffAppointmentDashboard) {
		this.allStaffAppointmentDashboard = allStaffAppointmentDashboard;
	}

	public Boolean getOwner() {
		return owner;
	}

	public void setOwner(Boolean owner) {
		this.owner = owner;
	}

	public Boolean getAreaManager() {
		return areaManager;
	}

	public void setAreaManager(Boolean areaManager) {
		this.areaManager = areaManager;
	}

	public Boolean getDoctor() {
		return doctor;
	}

	public void setDoctor(Boolean doctor) {
		this.doctor = doctor;
	}

	public Boolean getConsultant() {
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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public Long getTenantCompanyMappingId() {
		return tenantCompanyMappingId;
	}

	public void setTenantCompanyMappingId(Long tenantCompanyMappingId) {
		this.tenantCompanyMappingId = tenantCompanyMappingId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPersonnelMobileNumber() {
		return personnelMobileNumber;
	}

	public void setPersonnelMobileNumber(String personnelMobileNumber) {
		this.personnelMobileNumber = personnelMobileNumber;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Long getApplicationTenantId() {
		return applicationTenantId;
	}

	public void setApplicationTenantId(Long applicationTenantId) {
		this.applicationTenantId = applicationTenantId;
	}

	public PersonnelBankAccountDetails getPersonnelBankAccountDetails() {
		return personnelBankAccountDetails;
	}

	public void setPersonnelBankAccountDetails(PersonnelBankAccountDetails personnelBankAccountDetails) {
		this.personnelBankAccountDetails = personnelBankAccountDetails;
	}

	public List<PersonnelWorkExperienceDetails> getPersonnelWorkExperienceDetails() {
		return personnelWorkExperienceDetails;
	}

	public void setPersonnelWorkExperienceDetails(List<PersonnelWorkExperienceDetails> personnelWorkExperienceDetails) {
		this.personnelWorkExperienceDetails = personnelWorkExperienceDetails;
	}

	public List<PersonnelEmergencyContactDetails> getPersonnelEmergencyContactDetails() {
		return personnelEmergencyContactDetails;
	}

	public void setPersonnelEmergencyContactDetails(List<PersonnelEmergencyContactDetails> personnelEmergencyContactDetails) {
		this.personnelEmergencyContactDetails = personnelEmergencyContactDetails;
	}

	public String getEmployeeCode() {
		return employeeCode;
	}

	public void setEmployeeCode(String employeeCode) {
		this.employeeCode = employeeCode;
	}

	public String getWeeklyOff() {
		return weeklyOff;
	}

	public void setWeeklyOff(String weeklyOff) {
		this.weeklyOff = weeklyOff;
	}

	public Long getReportingTo() {
		return reportingTo;
	}

	public void setReportingTo(Long reportingTo) {
		this.reportingTo = reportingTo;
	}

	public Float getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(Float workingHours) {
		this.workingHours = workingHours;
	}

	public String getUanNumber() {
		return uanNumber;
	}

	public void setUanNumber(String uanNumber) {
		this.uanNumber = uanNumber;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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

	public List<PersonnelDocumentDetails> getPersonnelDocumentDetails() {
		return personnelDocumentDetails;
	}

	public void setPersonnelDocumentDetails(List<PersonnelDocumentDetails> personnelDocumentDetails) {
		this.personnelDocumentDetails = personnelDocumentDetails;
	}

	public Integer getDisplayRank() {
		return displayRank;
	}

	public void setDisplayRank(Integer displayRank) {
		this.displayRank = displayRank;
	}
}
