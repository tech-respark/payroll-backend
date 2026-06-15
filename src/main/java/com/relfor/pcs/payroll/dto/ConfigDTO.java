package com.relfor.pcs.payroll.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigDTO {
	private Long tenantId;
	private Long storeId;
	private String tenant;
	private String store;
	private String alphaCodeForOrder;
	private String currencySymbol;
	private Integer decimalPlaces;
	private boolean isInventory;
	private boolean isConsumableInventory;
	private boolean isLoyalty;
	private boolean isTipToStaff;
	private boolean isWhatsapp;
	private boolean isWhatsappCombineTemplates;
	private boolean isMembership;
	private boolean isZeroInvoice;
	private boolean isEmailService;
	private boolean isReferral;
	private float minOrderValue;
	private String startTime;
	private String closureTime;
	private String weeklyOff;
	private boolean orderingOn;
	private boolean deliveryOn;
	private boolean preOrdering;
	private boolean recieveOnlinePayment;
	private boolean cod;
	private String genderConfig;
	private boolean showServicesPdp;
	private boolean readOnlyMenu;
	private String deliveryDisclaimer;
	private String pickupDisclaimer;
	private boolean pickupOn;
	private boolean showProductCategoryGridView;
	private boolean showStoreLocator;
	private Object storeConfig;
	private boolean showProductInGridView;
	private boolean codForDeliveryOrders;
	private String alphaCodeForMembership;
	private String alphaCodeForPackage;
	private String membershipCode;
	private String alphaCodeForGiftCard;
	private String zeroBillInvoiceCode;
	private boolean isAppointment;
	private boolean isAppointmentActive;
	private boolean sendReminders;
	private Integer reminderBeforeDays;
	private Integer reminderBeforeHrs;
	private Integer reminderBeforeMinToStaff;
	private boolean isServiceReminder;
	private Integer serviceReminderBeforeDays;
	private boolean isDobReminder;
	private boolean isAnniversaryReminder;
	private boolean isService;
	private boolean stylistProductivity;
	private Long clientRetentionDays;
	private boolean isPushNotification;
	private boolean biometric;
	private boolean isBalanceRepaymentActive;
	private boolean enableRoundOff;
	private boolean isProductExpiry;
	private boolean payroll;
	private List<Map<String, Object>> dayWiseTiming;
	private Integer defaultSlotTime;
	private String dateFormat;
	private boolean gdpr;

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}

	public Long getStoreId() {
		return storeId;
	}

	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public String getAlphaCodeForOrder() {
		return alphaCodeForOrder;
	}

	public void setAlphaCodeForOrder(String alphaCodeForOrder) {
		this.alphaCodeForOrder = alphaCodeForOrder;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public boolean isInventory() {
		return isInventory;
	}

	public void setInventory(boolean inventory) {
		isInventory = inventory;
	}

	public boolean isConsumableInventory() {
		return isConsumableInventory;
	}

	public void setConsumableInventory(boolean consumableInventory) {
		isConsumableInventory = consumableInventory;
	}

	public boolean isLoyalty() {
		return isLoyalty;
	}

	public void setLoyalty(boolean loyalty) {
		isLoyalty = loyalty;
	}

	public boolean isTipToStaff() {
		return isTipToStaff;
	}

	public void setTipToStaff(boolean tipToStaff) {
		isTipToStaff = tipToStaff;
	}

	public boolean isWhatsapp() {
		return isWhatsapp;
	}

	public void setWhatsapp(boolean whatsapp) {
		isWhatsapp = whatsapp;
	}

	public boolean isMembership() {
		return isMembership;
	}

	public void setMembership(boolean membership) {
		isMembership = membership;
	}

	public boolean isZeroInvoice() {
		return isZeroInvoice;
	}

	public void setZeroInvoice(boolean zeroInvoice) {
		isZeroInvoice = zeroInvoice;
	}

	public boolean isEmailService() {
		return isEmailService;
	}

	public void setEmailService(boolean emailService) {
		isEmailService = emailService;
	}

	public boolean isReferral() {
		return isReferral;
	}

	public void setReferral(boolean referral) {
		isReferral = referral;
	}

	public float getMinOrderValue() {
		return minOrderValue;
	}

	public void setMinOrderValue(float minOrderValue) {
		this.minOrderValue = minOrderValue;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getClosureTime() {
		return closureTime;
	}

	public void setClosureTime(String closureTime) {
		this.closureTime = closureTime;
	}

	public String getWeeklyOff() {
		return weeklyOff;
	}

	public void setWeeklyOff(String weeklyOff) {
		this.weeklyOff = weeklyOff;
	}

	public boolean isOrderingOn() {
		return orderingOn;
	}

	public void setOrderingOn(boolean orderingOn) {
		this.orderingOn = orderingOn;
	}

	public boolean isDeliveryOn() {
		return deliveryOn;
	}

	public void setDeliveryOn(boolean deliveryOn) {
		this.deliveryOn = deliveryOn;
	}

	public boolean isPreOrdering() {
		return preOrdering;
	}

	public void setPreOrdering(boolean preOrdering) {
		this.preOrdering = preOrdering;
	}

	public boolean isRecieveOnlinePayment() {
		return recieveOnlinePayment;
	}

	public void setRecieveOnlinePayment(boolean recieveOnlinePayment) {
		this.recieveOnlinePayment = recieveOnlinePayment;
	}

	public boolean isCod() {
		return cod;
	}

	public void setCod(boolean cod) {
		this.cod = cod;
	}

	public String getGenderConfig() {
		return genderConfig;
	}

	public void setGenderConfig(String genderConfig) {
		this.genderConfig = genderConfig;
	}

	public boolean isShowServicesPdp() {
		return showServicesPdp;
	}

	public void setShowServicesPdp(boolean showServicesPdp) {
		this.showServicesPdp = showServicesPdp;
	}

	public boolean isReadOnlyMenu() {
		return readOnlyMenu;
	}

	public void setReadOnlyMenu(boolean readOnlyMenu) {
		this.readOnlyMenu = readOnlyMenu;
	}

	public String getDeliveryDisclaimer() {
		return deliveryDisclaimer;
	}

	public void setDeliveryDisclaimer(String deliveryDisclaimer) {
		this.deliveryDisclaimer = deliveryDisclaimer;
	}

	public String getPickupDisclaimer() {
		return pickupDisclaimer;
	}

	public void setPickupDisclaimer(String pickupDisclaimer) {
		this.pickupDisclaimer = pickupDisclaimer;
	}

	public boolean isPickupOn() {
		return pickupOn;
	}

	public void setPickupOn(boolean pickupOn) {
		this.pickupOn = pickupOn;
	}

	public boolean isShowProductCategoryGridView() {
		return showProductCategoryGridView;
	}

	public void setShowProductCategoryGridView(boolean showProductCategoryGridView) {
		this.showProductCategoryGridView = showProductCategoryGridView;
	}

	public boolean isShowStoreLocator() {
		return showStoreLocator;
	}

	public void setShowStoreLocator(boolean showStoreLocator) {
		this.showStoreLocator = showStoreLocator;
	}

	public Object getStoreConfig() {
		return storeConfig;
	}

	public void setStoreConfig(Object storeConfig) {
		this.storeConfig = storeConfig;
	}

	public boolean isShowProductInGridView() {
		return showProductInGridView;
	}

	public void setShowProductInGridView(boolean showProductInGridView) {
		this.showProductInGridView = showProductInGridView;
	}

	public boolean isCodForDeliveryOrders() {
		return codForDeliveryOrders;
	}

	public void setCodForDeliveryOrders(boolean codForDeliveryOrders) {
		this.codForDeliveryOrders = codForDeliveryOrders;
	}

	public String getAlphaCodeForMembership() {
		return alphaCodeForMembership;
	}

	public void setAlphaCodeForMembership(String alphaCodeForMembership) {
		this.alphaCodeForMembership = alphaCodeForMembership;
	}

	public String getAlphaCodeForPackage() {
		return alphaCodeForPackage;
	}

	public void setAlphaCodeForPackage(String alphaCodeForPackage) {
		this.alphaCodeForPackage = alphaCodeForPackage;
	}

	public String getMembershipCode() {
		return membershipCode;
	}

	public void setMembershipCode(String membershipCode) {
		this.membershipCode = membershipCode;
	}

	public String getAlphaCodeForGiftCard() {
		return alphaCodeForGiftCard;
	}

	public void setAlphaCodeForGiftCard(String alphaCodeForGiftCard) {
		this.alphaCodeForGiftCard = alphaCodeForGiftCard;
	}

	public String getZeroBillInvoiceCode() {
		return zeroBillInvoiceCode;
	}

	public void setZeroBillInvoiceCode(String zeroBillInvoiceCode) {
		this.zeroBillInvoiceCode = zeroBillInvoiceCode;
	}

	public boolean isAppointment() {
		return isAppointment;
	}

	public void setAppointment(boolean appointment) {
		isAppointment = appointment;
	}

	public boolean isAppointmentActive() {
		return isAppointmentActive;
	}

	public void setAppointmentActive(boolean appointmentActive) {
		isAppointmentActive = appointmentActive;
	}

	public boolean isSendReminders() {
		return sendReminders;
	}

	public void setSendReminders(boolean sendReminders) {
		this.sendReminders = sendReminders;
	}

	public Integer getReminderBeforeDays() {
		return reminderBeforeDays;
	}

	public void setReminderBeforeDays(Integer reminderBeforeDays) {
		this.reminderBeforeDays = reminderBeforeDays;
	}

	public Integer getReminderBeforeHrs() {
		return reminderBeforeHrs;
	}

	public void setReminderBeforeHrs(Integer reminderBeforeHrs) {
		this.reminderBeforeHrs = reminderBeforeHrs;
	}

	public Integer getReminderBeforeMinToStaff() {
		return reminderBeforeMinToStaff;
	}

	public void setReminderBeforeMinToStaff(Integer reminderBeforeMinToStaff) {
		this.reminderBeforeMinToStaff = reminderBeforeMinToStaff;
	}

	public boolean isServiceReminder() {
		return isServiceReminder;
	}

	public void setServiceReminder(boolean serviceReminder) {
		isServiceReminder = serviceReminder;
	}

	public Integer getServiceReminderBeforeDays() {
		return serviceReminderBeforeDays;
	}

	public void setServiceReminderBeforeDays(Integer serviceReminderBeforeDays) {
		this.serviceReminderBeforeDays = serviceReminderBeforeDays;
	}

	public boolean isDobReminder() {
		return isDobReminder;
	}

	public void setDobReminder(boolean dobReminder) {
		isDobReminder = dobReminder;
	}

	public boolean isAnniversaryReminder() {
		return isAnniversaryReminder;
	}

	public void setAnniversaryReminder(boolean anniversaryReminder) {
		this.isAnniversaryReminder = anniversaryReminder;
	}

	public boolean isService() {
		return isService;
	}

	public void setService(boolean service) {
		isService = service;
	}

	public boolean isStylistProductivity() {
		return stylistProductivity;
	}

	public void setStylistProductivity(boolean stylistProductivity) {
		this.stylistProductivity = stylistProductivity;
	}

	public Long getClientRetentionDays() {
		return clientRetentionDays;
	}

	public void setClientRetentionDays(Long clientRetentionDays) {
		this.clientRetentionDays = clientRetentionDays;
	}

	public boolean getIsPushNotification() {
		return isPushNotification;
	}

	public void setIsPushNotification(boolean pushNotification) {
		isPushNotification = pushNotification;
	}

	public boolean getIsBalanceRepaymentActive() {
		return isBalanceRepaymentActive;
	}

	public void setIsBalanceRepaymentActive(boolean isBalanceRepaymentActive) {
		this.isBalanceRepaymentActive = isBalanceRepaymentActive;
	}

	public boolean getEnableRoundOff() {
		return enableRoundOff;
	}

	public void setEnableRoundOff(boolean enableRoundOff) {
		this.enableRoundOff = enableRoundOff;
	}

	public boolean getBiometric() {
		return biometric;
	}

	public void setBiometric(boolean biometric) {
		this.biometric = biometric;
	}

	public boolean getPayroll() {
		return payroll;
	}

	public void setPayroll(boolean payroll) {
		this.payroll = payroll;
	}

	public List<Map<String, Object>> getDayWiseTiming() {
		return dayWiseTiming;
	}

	public void setDayWiseTiming(List<Map<String, Object>> dayWiseTiming) {
		this.dayWiseTiming = dayWiseTiming;
	}

	public boolean isProductExpiry() {
		return isProductExpiry;
	}

	public void setProductExpiry(boolean productExpiry) {
		isProductExpiry = productExpiry;
	}

	public Integer getDefaultSlotTime() {
		return defaultSlotTime;
	}

	public void setDefaultSlotTime(Integer defaultSlotTime) {
		this.defaultSlotTime = defaultSlotTime;
	}

	public boolean isWhatsappCombineTemplates() {
		return isWhatsappCombineTemplates;
	}

	public void setWhatsappCombineTemplates(boolean whatsappCombineTemplates) {
		isWhatsappCombineTemplates = whatsappCombineTemplates;
	}

	public boolean getGdpr() {
		return gdpr;
	}

	public void setGdpr(boolean gdpr) {
		this.gdpr = gdpr;
	}
}
