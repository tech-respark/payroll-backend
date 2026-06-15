package com.relfor.pcs.payroll.service;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.dto.STenantStore;
import com.relfor.pcs.payroll.dto.SalaryComponentDTO;
import com.relfor.pcs.payroll.dto.SalaryComponentResponseDTO;
import com.relfor.pcs.payroll.dto.SalaryComponentsDTO;
import com.relfor.pcs.payroll.entity.*;
import com.relfor.pcs.payroll.repository.*;
import com.relfor.pcs.payroll.util.ApiHelper;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryCalculation {

    @Autowired
    private SalaryComponentDefinitionsRepository definitionsRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String SUCCESS = "SUCCESS";

    @Autowired
    private PersonnelSalaryComponentsRepository personnelSalaryComponentsRepository;

    @Autowired
    private MonthWiseAttendanceSummaryRepository monthWiseAttendanceSummaryRepository;
    @Autowired
    PersonnelDetailsRepository personnelDetailsRepository;
    @Autowired
    ApiHelper apiHelper;
    @Autowired
    StoreDetailsRepository storeDetailsRepository;
    @Autowired
    private  PersonnelPayslipHistoryRepository personnelPayslipHistoryRepository;

    public ResponseModel processSalaryComponents(SalaryComponentResponseDTO personnelComponents) {
        ResponseModel responseModel = new ResponseModel();
        try {
            if (!ObjectUtils.isEmpty(personnelComponents)){
                Long personnelCode = personnelComponents.getPersonnelCode();
                Long tenantId = personnelComponents.getTenantId();
                Long storeId = personnelComponents.getStoreId();
                logger.info("Processing salary components for PersonnelCode={} ", personnelCode);
                List<PersonnelSalaryComponents> existingPersonnelComponents =
                        personnelSalaryComponentsRepository.findByPersonnelCode(personnelCode);
                if(!ObjectUtils.isEmpty(existingPersonnelComponents)){
                    personnelSalaryComponentsRepository.deleteAll(existingPersonnelComponents);
                }

                List<SalaryComponentDefinitions> salaryComponentDefinitions = definitionsRepository.findAllByTenantIdAndStoreId(tenantId, storeId);
                if (ObjectUtils.isEmpty(salaryComponentDefinitions)) {
                    salaryComponentDefinitions = definitionsRepository.findAllByTenantIdAndStoreId(tenantId, 0L);
                }
                logger.info("Found {} salary component definitions for TenantId={} | StoreId={}",
                        salaryComponentDefinitions.size(), tenantId, storeId);

                List<SalaryComponentDefinitions> computableComponents = salaryComponentDefinitions.stream()
                                .filter(SalaryComponentDefinitions::getComputable).collect(Collectors.toList());
                List<PersonnelSalaryComponents> personnelSalaryComponents = getPersonnelSalaryComponentsList(personnelComponents, salaryComponentDefinitions);
                List<PersonnelSalaryComponents> fixedComponents = personnelSalaryComponents.stream()
                        .filter(component -> component.getSalaryComponentDefinitions().getComponentType().equalsIgnoreCase("FIXED")).collect(Collectors.toList());

                List<PersonnelSalaryComponents> responseData = new ArrayList<>(fixedComponents);

                Map<String, BigDecimal> context = new HashMap<>();
                fixedComponents.forEach(comp -> {
                    context.put(comp.getSalaryComponentDefinitions().getComponentNameAlias(), comp.getAnnualValue());
                });

                List<SalaryComponentDefinitions> formulaDefinitions =
                        computableComponents.stream().filter(definition -> definition.getComponentType().equalsIgnoreCase("FORMULA")).collect(Collectors.toList());
                List<SalaryComponentDefinitions> fixedDefinitions =
                        computableComponents.stream().filter(definition -> definition.getComponentType().equalsIgnoreCase("FIXED")).collect(Collectors.toList());

                for(SalaryComponentDefinitions fixComponent : fixedDefinitions) {
                    context.putIfAbsent(fixComponent.getComponentNameAlias() , BigDecimal.ZERO);
                }

                for (SalaryComponentDefinitions definition : formulaDefinitions) {
                    for (SalaryComponentRules rule : definition.getSalaryComponentRulesList()) {
                        if (rule.getConditionExpression() == null ||
                                evaluateCondition(rule.getConditionExpression(), context)) {
                            BigDecimal result = evaluateFormula(rule.getFormula(), context);

                            PersonnelSalaryComponents component = new PersonnelSalaryComponents();
                            component.setPersonnelCode(personnelCode);
                            component.setTenantId(definition.getTenantId());
                            component.setStoreId(storeId);
                            component.setComponentName(definition.getComponentName());
                            component.setAnnualValue(result);
                            component.setSalaryComponentDefinitions(definition);

                            context.put(component.getSalaryComponentDefinitions().getComponentNameAlias(), component.getAnnualValue());

                            responseData.add(component);
                        }
                    }
                }
                responseData = responseData.stream().map(this::calculateValues).collect(Collectors.toList());
                personnelSalaryComponentsRepository.saveAll(responseData);
                logger.info("Successfully saved {} salary components for PersonnelCode={}",
                        responseData.size(), personnelCode);

                SalaryComponentResponseDTO salaryResponse = getSalaryResponse(responseData, true);
                responseModel.setData(salaryResponse);
            }
            responseModel.setCode(HttpStatus.OK);
            responseModel.setMessage(SUCCESS);
        } catch (Exception ex) {
            logger.error("Exception in getPersonnelAttendanceSummary: {}", ex.getMessage());
            throw ex;
        }
        return responseModel;
    }

    private List<PersonnelSalaryComponents> getPersonnelSalaryComponentsList(SalaryComponentResponseDTO personnelComponents, List<SalaryComponentDefinitions> salaryComponentDefinitions) {
        List<PersonnelSalaryComponents> personnelSalaryComponents = new ArrayList<>();
        Long tenantId = personnelComponents.getTenantId();
        Long storeId = personnelComponents.getStoreId();
        Long personnelCode = personnelComponents.getPersonnelCode();

        List<SalaryComponentDTO> allComponentsList = new ArrayList<>();
        if (personnelComponents.getEarningsList() != null) {
            allComponentsList.addAll(personnelComponents.getEarningsList());
        }
        if (personnelComponents.getDeductionsList() != null) {
            allComponentsList.addAll(personnelComponents.getDeductionsList());
        }

        for(SalaryComponentDTO dto : allComponentsList) {
            Optional<SalaryComponentDefinitions> salaryDefinition = salaryComponentDefinitions.stream().filter(definition -> definition.getId() == dto.getSalaryComponentDefinitionsId()).findFirst();

            PersonnelSalaryComponents salaryComponent = new PersonnelSalaryComponents();
            salaryComponent.setTenantId(tenantId);
            salaryComponent.setStoreId(storeId);
            salaryComponent.setPersonnelCode(personnelCode);
            salaryComponent.setAnnualValue(dto.getAnnualValue());
            salaryComponent.setSalaryComponentDefinitions(salaryDefinition.get());
            salaryComponent.setComponentName(salaryDefinition.get().getComponentName());

            personnelSalaryComponents.add(salaryComponent);
        }
        logger.info("Generated {} personnel salary components for PersonnelCode={}",
                personnelSalaryComponents.size(), personnelCode);

        return personnelSalaryComponents;
    }

    private  PersonnelSalaryComponents calculateValues(PersonnelSalaryComponents component) {
        Double DaysInMonths = 30D;
        Double HoursInDay = 9.5;
        Float workingHours = personnelDetailsRepository.getPersonnelWorkingHours(component.getPersonnelCode());
        if (workingHours != null && workingHours > 0f) {
            HoursInDay = Double.valueOf(workingHours);
        }

        BigDecimal result = component.getAnnualValue();
        BigDecimal monthlyValue = result.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
        BigDecimal dailyValue = monthlyValue.divide(BigDecimal.valueOf(DaysInMonths), 4, RoundingMode.HALF_UP);
        BigDecimal hourlyValue = dailyValue.divide(BigDecimal.valueOf(HoursInDay), 4, RoundingMode.HALF_UP);

        component.setMonthlyValue(monthlyValue);
        component.setDailyValue(dailyValue);
        component.setHourlyValue(hourlyValue);

        return component;
    }

    private BigDecimal evaluateFormula(String formula, Map<String, BigDecimal> context) {
        try {
            Object result = MVEL.eval(formula, context);
            if (result instanceof Number) {
                return new BigDecimal(result.toString());
            }
            throw new RuntimeException("Formula must evaluate to a number");
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating formula: " + formula, e);
        }
    }

    private boolean evaluateCondition(String condition, Map<String, BigDecimal> context) {
        try {
            return MVEL.evalToBoolean(condition, context);
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating condition: " + condition, e);
        }
    }

    public ResponseModel processSalary(Long personnelCode, String  month, Long year, Long tenantId, Long storeId) {
        ResponseModel responseModel = new ResponseModel();
        try {
            MonthWiseAttendanceSummary staffMonthWiseSummary = monthWiseAttendanceSummaryRepository.getSummaryByPersonnelCodeAndMonth(personnelCode, month, year);
            List<SalaryComponentDefinitions> salaryComponentDefinitionsList = definitionsRepository.findByTenantIdAndStoreId(tenantId, storeId);
            if (ObjectUtils.isEmpty(salaryComponentDefinitionsList)) {
                salaryComponentDefinitionsList = definitionsRepository.findByTenantIdAndStoreId(tenantId, 0L);
            }
            PersonnelPayslipHistory responseData =  this.calculateSalaryComponents(staffMonthWiseSummary, salaryComponentDefinitionsList, personnelCode, null);
            responseModel.setCode(HttpStatus.OK);
            if(!ObjectUtils.isEmpty(responseData)) {
                responseModel.setData(responseData);
                responseModel.setMessage(SUCCESS);
            } else {
            responseModel.setMessage("No Data Found");
            }
        } catch (Exception ex) {
            logger.error("Exception in processSalary: {}", ex.getMessage());
            throw ex;
        }
        return responseModel;
    }

    public void processSalaryForMonthlySummary(List<MonthWiseAttendanceSummary> monthWiseAttendanceSummaryList,
                                               List<StoreDetails> storeDetailsListForMonthlySummary) {
        try {
            monthWiseAttendanceSummaryList.sort(Comparator.comparing(MonthWiseAttendanceSummary::getStoreId));
            Long currentStoreId = null;
            List<SalaryComponentDefinitions> salaryComponentDefinitionsList = null;
            for (MonthWiseAttendanceSummary monthWiseAttendanceSummary: monthWiseAttendanceSummaryList) {
                if (currentStoreId == null
                        || !Objects.equals(monthWiseAttendanceSummary.getStoreId(), currentStoreId)) {
                    currentStoreId = monthWiseAttendanceSummary.getStoreId();
                    salaryComponentDefinitionsList = this.findSalaryCompenentDefinitionsList(monthWiseAttendanceSummary, currentStoreId);
                }
                if (!ObjectUtils.isEmpty(salaryComponentDefinitionsList)) {
                    this.calculateSalaryComponents(monthWiseAttendanceSummary, salaryComponentDefinitionsList, monthWiseAttendanceSummary.getPersonnelCode(), storeDetailsListForMonthlySummary);
                }
            }
        } catch (Exception ex) {
            logger.error("Exception in processSalaryForMonthlySummary: {}", ex.getMessage());
        }
    }

    public List<SalaryComponentDefinitions> findSalaryCompenentDefinitionsList(MonthWiseAttendanceSummary monthWiseAttendanceSummary, Long currentStoreId) {
        List<SalaryComponentDefinitions> salaryComponentDefinitionsList;
        salaryComponentDefinitionsList
                = definitionsRepository.findByTenantIdAndStoreId(monthWiseAttendanceSummary.getTenantId(), currentStoreId);

        if (ObjectUtils.isEmpty(salaryComponentDefinitionsList)) {
            salaryComponentDefinitionsList
                    = definitionsRepository.findByTenantIdAndStoreId(monthWiseAttendanceSummary.getTenantId(), 0L);
        }
        return salaryComponentDefinitionsList;
    }

    public PersonnelPayslipHistory calculateSalaryComponents(
            MonthWiseAttendanceSummary attendanceSummary,
            List<SalaryComponentDefinitions> componentDefinitions,
            Long personnelCode,
            List<StoreDetails> storeDetailsListForMonthlySummary) {

        SalaryComponentsDTO salaryComponents = null;

        if(!ObjectUtils.isEmpty(attendanceSummary)){
            Map<String, BigDecimal> context = this.convertToContext(attendanceSummary);
            Float workingHours = personnelDetailsRepository.getPersonnelWorkingHours(personnelCode);
            BigDecimal hours = workingHours != null ? BigDecimal.valueOf(workingHours) : BigDecimal.valueOf(9.5);
            context.put("workingHours", hours);

            List<PersonnelSalaryComponents> existingComponents =
                    personnelSalaryComponentsRepository.findByPersonnelCode(personnelCode);
            if (!existingComponents.isEmpty()) {
                existingComponents.forEach(component -> {
                    context.put(
                            component.getSalaryComponentDefinitions().getComponentNameAlias(),
                            component.getAnnualValue()
                    );
                });

                salaryComponents =  setSalaryComponents(personnelCode, attendanceSummary, componentDefinitions, existingComponents, context, storeDetailsListForMonthlySummary);
                return saveOrUpdateSalaryComponents(salaryComponents, componentDefinitions);
            }
        }
        return null;
    }

    private Map<String, BigDecimal> convertToContext(MonthWiseAttendanceSummary attendanceSummary) {
        Map<String, BigDecimal> context = new HashMap<>();
        context.put("extraDaysWorked", BigDecimal.valueOf(attendanceSummary.getExtraDaysWorked()));
        context.put("totalAbsentDays", BigDecimal.valueOf(attendanceSummary.getTotalAbsentDays()));
        context.put("totalPenaltyAbsentDays", BigDecimal.valueOf(attendanceSummary.getTotalPenaltyAbsentDays()));
        context.put("totalLateArrivalMins", BigDecimal.valueOf(attendanceSummary.getTotalLateArrivalMins()));
        context.put("totalEarlyExitMins", BigDecimal.valueOf(attendanceSummary.getTotalEarlyExitMins()));
        context.put("totalOvertimeMins", BigDecimal.valueOf(attendanceSummary.getTotalOvertimeMins()));
        context.put("totalEarlyExit", BigDecimal.valueOf(attendanceSummary.getTotalEarlyExits()));
        context.put("totalLateArrivals", BigDecimal.valueOf(attendanceSummary.getTotalLateArrivals()));

        return context;

    }

    private void processNonComputableComponents(
            List<SalaryComponentDefinitions> componentDefinitions,
            Map<String, BigDecimal> context,
            SalaryComponentsDTO salaryDto) {

        List<SalaryComponentDefinitions> nonComputableComponents =
                componentDefinitions.stream()
                        .filter(definition -> !definition.getComputable() && !definition.getIsCalculatedMonthly())
                        .collect(Collectors.toList());

        for (SalaryComponentDefinitions definition : nonComputableComponents) {
            for (SalaryComponentRules rule : definition.getSalaryComponentRulesList()) {
                if (rule.getConditionExpression() == null ||
                        evaluateCondition(rule.getConditionExpression(), context)) {
                    BigDecimal amount = evaluateFormula(rule.getFormula(), context)
                            .setScale(4, RoundingMode.HALF_UP);
                    context.put(definition.getComponentName(), amount);
                    SalaryComponentDTO salaryComponentDTO = new SalaryComponentDTO();
                    salaryComponentDTO.setSalaryComponentDefinitionsId(definition.getId());
                    salaryComponentDTO.setComponentName(definition.getComponentName());
                    salaryComponentDTO.setComponentType(definition.getComponentType());
                    salaryComponentDTO.setCalculatedMonthly(definition.getIsCalculatedMonthly());
                    salaryComponentDTO.setMonthlyValue(amount);
                    if ("EARNING".equalsIgnoreCase(definition.getComponentCategory())) {
                        salaryDto.getEarningsList().add(salaryComponentDTO);
                        salaryDto.getEarnings().put(definition.getComponentName(), amount);
                        if(definition.getIncludeInTotal()) {
                            salaryDto.setTotalEarning(salaryDto.getTotalEarning().add(amount));
                        }
                    } else {
                        salaryDto.getDeductionsList().add(salaryComponentDTO);
                        salaryDto.getDeductions().put(definition.getComponentName(), amount);
                        if(definition.getIncludeInTotal()) {
                            salaryDto.setTotalDeduction(salaryDto.getTotalDeduction().add(amount));
                        }
                    }
                }
            }
        }
    }

    private SalaryComponentsDTO setSalaryComponents(
            Long personnelCode,
            MonthWiseAttendanceSummary attendanceSummary,
            List<SalaryComponentDefinitions> componentDefinitions,
            List<PersonnelSalaryComponents> existingComponents,
            Map<String, BigDecimal> context,
            List<StoreDetails> storeDetailsListForMonthlySummary) {

        SalaryComponentsDTO dto = null;
        Optional<PersonnelDetails> personnelDetailsOptional = personnelDetailsRepository.findByPersonnelCode(personnelCode);
        if (personnelDetailsOptional.isPresent()) {
            PersonnelDetails personnelDetails = personnelDetailsOptional.get();
            dto = new SalaryComponentsDTO();
            Optional<StoreDetails> storeDetailsOptional;
            if (!ObjectUtils.isEmpty(storeDetailsListForMonthlySummary)) {
                storeDetailsOptional = storeDetailsListForMonthlySummary.stream().filter(
                        storeDetails -> (StringUtils.equalsIgnoreCase(storeDetails.getTenantCompanyMapping().getApplicationName(), attendanceSummary.getApplicationName()))
                                && (Objects.equals(storeDetails.getTenantCompanyMapping().getTenantId(), attendanceSummary.getTenantId()))
                                && (Objects.equals(storeDetails.getStoreId(), attendanceSummary.getStoreId()))).findFirst();
            } else {
                storeDetailsOptional = storeDetailsRepository.fetchStoreAndTenantDetails(attendanceSummary.getApplicationName(),
                    attendanceSummary.getTenantId(), attendanceSummary.getStoreId());
            }
            this.setPersonnelDetails(personnelDetails, dto, storeDetailsOptional.orElse(null));
            String timeZone = null;
            Integer offset = null;
            Integer startDay = null;
            if (storeDetailsOptional.isPresent()) {
                timeZone = storeDetailsOptional.get().getTimeZone();
                offset = storeDetailsOptional.get().getTenantCompanyMapping().getSalaryCalculationOffsetDays();
                startDay = storeDetailsOptional.get().getTenantCompanyMapping().getSalaryCycleStartDay();
            }
            ZoneId zoneId = !StringUtils.isEmpty(timeZone) ? ZoneId.of(timeZone) : ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = Instant.now().atZone(zoneId);
            LocalDate salaryDate;
            if (offset != null && startDay != null) {
                int effectiveSalaryDay = (startDay == 1) ? offset : (startDay - 1 + offset);
                salaryDate = LocalDate.of(zonedDateTime.getYear(), zonedDateTime.getMonth(), effectiveSalaryDay);
            } else {
                salaryDate = zonedDateTime.toLocalDate();
            }
            dto.setSalaryDate(salaryDate);
            dto.setTotalDays(attendanceSummary.getTotalDaysInCycle());
            dto.setTotalWorkingDays(attendanceSummary.getTotalWorkingDays());
            dto.setAbsentDays(attendanceSummary.getTotalAbsentDays());
            dto.setPenaltyAbsentDays(attendanceSummary.getTotalPenaltyAbsentDays());
            dto.setTotalHolidays(attendanceSummary.getTotalHolidays());
            dto.setTotalweeklyOff(attendanceSummary.getTotalWeeklyOffs());
            dto.setTotalPaidLeaves(attendanceSummary.getTotalPaidLeaves());
            dto.setTotalPaidDays(attendanceSummary.getTotalPaidDays());
            dto.setSalaryMonth(attendanceSummary.getSalaryMonth());
            dto.setSalaryYear(attendanceSummary.getSalaryYear());

            dto.setEarnings(new LinkedHashMap<>());
            dto.setDeductions(new LinkedHashMap<>());

            dto.setEarningsList(new ArrayList<>());
            dto.setDeductionsList(new ArrayList<>());

            dto.setTotalEarning(BigDecimal.ZERO);
            dto.setTotalDeduction(BigDecimal.ZERO);

            String salaryPeriod = attendanceSummary.getSalaryCycleFromDate() + " to " + attendanceSummary.getSalaryCycleToDate();
            dto.setSalaryPeriod(salaryPeriod);

            processNonComputableComponents(
                    componentDefinitions,
                    context,
                    dto
            );

            processExistingComponents(
                    existingComponents,
                    dto
            );

            dto.setSalaryAmount(
                    dto.getTotalEarning().subtract(dto.getTotalDeduction())
            );

        }
        return dto;
    }

    private void setPersonnelDetails(PersonnelDetails personnelDetails, SalaryComponentsDTO dto,StoreDetails storeDetails) {
        dto.setTenantId(personnelDetails.getApplicationTenantId());
        if(!ObjectUtils.isEmpty(personnelDetails.getStoreId())){
            dto.setStoreId(personnelDetails.getStoreId());
        } else if (!ObjectUtils.isEmpty(storeDetails)) {
            dto.setStoreId(storeDetails.getStoreId());
        }
        if (!ObjectUtils.isEmpty(dto.getStoreId())) {
            STenantStore store = apiHelper.getTenantStore(personnelDetails.getApplicationTenantId(), dto.getStoreId());
            dto.setStoreName(store.getName());
        }
        dto.setPersonnelId(personnelDetails.getPersonnelCode());
        dto.setDesignation(personnelDetails.getDesignation());
        dto.setEmployeeCode(personnelDetails.getEmployeeCode());
        dto.setUanNumber(personnelDetails.getUanNumber());
        String fullName =
                (personnelDetails.getFirstName() != null ? personnelDetails.getFirstName() + " ": "") +
                        (personnelDetails.getLastName() != null ? personnelDetails.getLastName() : "");
        dto.setPersonnelName(fullName);
        PersonnelBankAccountDetails bankDetails = personnelDetails.getPersonnelBankAccountDetails();

        if(!ObjectUtils.isEmpty(bankDetails)){
            dto.setBankName(bankDetails.getBankName());
            dto.setAccountNumber(bankDetails.getAccountNumber());
            dto.setIfscCode(bankDetails.getIfscCode());
        }
        if(!ObjectUtils.isEmpty(personnelDetails.getPersonnelDocumentDetails())) {
            for(PersonnelDocumentDetails document : personnelDetails.getPersonnelDocumentDetails()) {
                if(document.getDocumentName().equalsIgnoreCase("PAN")) {
                    dto.setPanNo(document.getDocumentNumber());
                }
            }
        }

    }

    private void processExistingComponents(
            List<PersonnelSalaryComponents> existingComponents,
            SalaryComponentsDTO dto) {

        float workingDays = dto.getTotalPaidDays();

        for (PersonnelSalaryComponents component : existingComponents) {
            BigDecimal amount = component.getDailyValue()
                    .multiply(BigDecimal.valueOf(workingDays))
                    .setScale(4, RoundingMode.HALF_UP);

            SalaryComponentDTO salaryComponentDTO = new SalaryComponentDTO();
            salaryComponentDTO.setSalaryComponentDefinitionsId(component.getSalaryComponentDefinitions().getId());
            salaryComponentDTO.setComponentName(component.getSalaryComponentDefinitions().getComponentName());
            salaryComponentDTO.setComponentType(component.getSalaryComponentDefinitions().getComponentType());
            salaryComponentDTO.setCalculatedMonthly(component.getSalaryComponentDefinitions().getIsCalculatedMonthly());
            salaryComponentDTO.setMonthlyValue(amount);
            if ("EARNING".equalsIgnoreCase(component.getSalaryComponentDefinitions().getComponentCategory())) {
                dto.getEarningsList().add(salaryComponentDTO);
                dto.getEarnings().put(component.getComponentName(), amount);
                if(component.getSalaryComponentDefinitions().getIncludeInTotal()){
                    dto.setTotalEarning(dto.getTotalEarning().add(amount));
                }
            } else {
                dto.getDeductionsList().add(salaryComponentDTO);
                dto.getDeductions().put(component.getComponentName(), amount);
                if(component.getSalaryComponentDefinitions().getIncludeInTotal()){
                    dto.setTotalDeduction(dto.getTotalDeduction().add(amount));
                }
            }
        }
    }

    public PersonnelPayslipHistory saveOrUpdateSalaryComponents(SalaryComponentsDTO dto, List<SalaryComponentDefinitions> componentDefinitions) {
        logger.info("Saving or updating payslip for PersonnelId={}, Month={}, Year={}",
                dto.getPersonnelId(), dto.getSalaryMonth(), dto.getSalaryYear());

        PersonnelPayslipHistory entity = convertToSalaryComponentEntity(dto);
        Optional<PersonnelPayslipHistory> personnelPayslipHistoryOptional =
                personnelPayslipHistoryRepository.findByPersonnelIdAndSalaryMonthAndSalaryYear(entity.getPersonnelId(), entity.getSalaryMonth(), entity.getSalaryYear());
        if (personnelPayslipHistoryOptional.isPresent()) {
            List<SalaryComponentDTO> earningsCalculatedMonthlyList = !ObjectUtils.isEmpty(personnelPayslipHistoryOptional.get().getEarnings()) ?
                    personnelPayslipHistoryOptional.get().getEarnings().stream().filter(SalaryComponentDTO::getCalculatedMonthly).collect(Collectors.toList()) : null;
            List<SalaryComponentDTO> deductionsCalculatedMonthlyList = !ObjectUtils.isEmpty(personnelPayslipHistoryOptional.get().getDeductions()) ?
                    personnelPayslipHistoryOptional.get().getDeductions().stream().filter(SalaryComponentDTO::getCalculatedMonthly).collect(Collectors.toList()) : null;
            entity.setId(personnelPayslipHistoryOptional.get().getId());
            if (!ObjectUtils.isEmpty(earningsCalculatedMonthlyList)) {
                entity.getEarnings().addAll(earningsCalculatedMonthlyList);
            }
            if (!ObjectUtils.isEmpty(deductionsCalculatedMonthlyList)) {
                entity.getDeductions().addAll(deductionsCalculatedMonthlyList);
            }
        }

        // Use includeInTotal filters
        Set<Long> includedEarningIds = componentDefinitions.stream()
                .filter(def -> def.getIncludeInTotal() && "EARNING".equalsIgnoreCase(def.getComponentCategory()))
                .map(SalaryComponentDefinitions::getId)
                .collect(Collectors.toSet());

        Set<Long> includedDeductionIds = componentDefinitions.stream()
                .filter(def -> def.getIncludeInTotal() && "DEDUCTION".equalsIgnoreCase(def.getComponentCategory()))
                .map(SalaryComponentDefinitions::getId)
                .collect(Collectors.toSet());

        BigDecimal totalEarning = calculateTotal(entity.getEarnings(), includedEarningIds);
        BigDecimal totalDeduction = calculateTotal(entity.getDeductions(), includedDeductionIds);
        BigDecimal netSalary = totalEarning.subtract(totalDeduction);

        entity.setTotalEarning(totalEarning);
        entity.setTotalDeduction(totalDeduction);
        entity.setSalaryAmount(netSalary);

        PersonnelPayslipHistory saved = personnelPayslipHistoryRepository.save(entity);
        logger.info("Payslip saved successfully for PersonnelId={} | NetSalary={} | Month-Year={}-{}",
                dto.getPersonnelId(), netSalary, dto.getSalaryMonth(), dto.getSalaryYear());

        return saved;
    }

    private PersonnelPayslipHistory convertToSalaryComponentEntity(SalaryComponentsDTO dto) {
        PersonnelPayslipHistory entity = new PersonnelPayslipHistory();
        entity.setTenantId(dto.getTenantId());
        entity.setStoreId(dto.getStoreId());
        entity.setPersonnelId(dto.getPersonnelId());
        entity.setPersonnelName(dto.getPersonnelName());
        entity.setSalaryDate(dto.getSalaryDate());
        entity.setEarnings(dto.getEarningsList());
        entity.setDeductions(dto.getDeductionsList());
        entity.setTotalEarning(dto.getTotalEarning());
        entity.setTotalDeduction(dto.getTotalDeduction());
        entity.setSalaryAmount(dto.getSalaryAmount());
        entity.setTotalDays(dto.getTotalDays());
        entity.setTotalWorkingDays(dto.getTotalWorkingDays());
        entity.setAbsentDays(dto.getAbsentDays());
        entity.setPenaltyAbsentDays(dto.getPenaltyAbsentDays());
        entity.setTotalHolidays(dto.getTotalHolidays());
        entity.setTotalweeklyOff(dto.getTotalweeklyOff());
        entity.setBankName(dto.getBankName());
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setIfscCode(dto.getIfscCode());
        entity.setPanNo(dto.getPanNo());
        entity.setSalaryPeriod(dto.getSalaryPeriod());
        entity.setDesignation(dto.getDesignation());
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setStoreName(dto.getStoreName());
        entity.setSalaryMonth(dto.getSalaryMonth());
        entity.setSalaryYear(dto.getSalaryYear());
        entity.setTotalPaidLeaves(dto.getTotalPaidLeaves());
        entity.setTotalPaidDays(dto.getTotalPaidDays());
        entity.setUanNumber(dto.getUanNumber());
        return entity;
    }

    private List<SalaryComponentDTO> mergeByComponentId(List<SalaryComponentDTO> components) {
        Map<Long, SalaryComponentDTO> merged = new LinkedHashMap<>();
        for (SalaryComponentDTO dto : components) {
            merged.put(dto.getSalaryComponentDefinitionsId(), dto); // Latest wins
        }
        return new ArrayList<>(merged.values());
    }

    private BigDecimal calculateTotal(List<SalaryComponentDTO> components, Set<Long> includedDefinitionIds) {
        if (components == null) return BigDecimal.ZERO;

        return components.stream()
                .filter(c -> includedDefinitionIds.contains(c.getSalaryComponentDefinitionsId()))
                .map(SalaryComponentDTO::getMonthlyValue)
                .filter(Objects::nonNull)// Convert Double to BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    public ResponseModel setMonthlyComponents(SalaryComponentResponseDTO salaryComponent) {
        ResponseModel responseModel = new ResponseModel();
        PersonnelPayslipHistory payslipHistory = new PersonnelPayslipHistory();
        payslipHistory.setPersonnelId(salaryComponent.getPersonnelCode());
        payslipHistory.setTenantId(salaryComponent.getTenantId());
        payslipHistory.setStoreId(salaryComponent.getStoreId());
        payslipHistory.setSalaryMonth(salaryComponent.getSalaryMonth());
        payslipHistory.setSalaryYear(salaryComponent.getSalaryYear());
        logger.info("Salary Component Definitions fetch for TenantId: {}, StoreId: {}",
                salaryComponent.getTenantId(), salaryComponent.getStoreId());
        List<SalaryComponentDefinitions> salaryComponentDefinitions = definitionsRepository.findAllByTenantIdAndStoreId(salaryComponent.getTenantId(), salaryComponent.getStoreId());
        if (ObjectUtils.isEmpty(salaryComponentDefinitions)) {
            salaryComponentDefinitions = definitionsRepository.findAllByTenantIdAndStoreId(salaryComponent.getTenantId(), 0L);
        }

        Optional<PersonnelPayslipHistory> personnelPayslipHistoryOptional =
                personnelPayslipHistoryRepository.findByPersonnelIdAndSalaryMonthAndSalaryYear(salaryComponent.getPersonnelCode(), salaryComponent.getSalaryMonth(), salaryComponent.getSalaryYear());


        if (!ObjectUtils.isEmpty(salaryComponent.getEarningsList())) {
            for (SalaryComponentDTO salaryComponentDTO: salaryComponent.getEarningsList()) {
                Optional<SalaryComponentDefinitions> salaryDefinition = salaryComponentDefinitions.stream()
                        .filter(definition -> Objects.equals(definition.getId(), salaryComponentDTO.getSalaryComponentDefinitionsId())).findFirst();
                salaryComponentDTO.setCalculatedMonthly(salaryDefinition.map(SalaryComponentDefinitions::getIsCalculatedMonthly).orElse(false));
                salaryComponentDTO.setComponentType(salaryDefinition.map(SalaryComponentDefinitions::getComponentType).orElse(null));
            }
        }

        if (!ObjectUtils.isEmpty(salaryComponent.getDeductionsList())) {
            for (SalaryComponentDTO salaryComponentDTO: salaryComponent.getDeductionsList()) {
                Optional<SalaryComponentDefinitions> salaryDefinition = salaryComponentDefinitions.stream()
                        .filter(definition -> Objects.equals(definition.getId(), salaryComponentDTO.getSalaryComponentDefinitionsId())).findFirst();
                salaryComponentDTO.setCalculatedMonthly(salaryDefinition.map(SalaryComponentDefinitions::getIsCalculatedMonthly).orElse(false));
                salaryComponentDTO.setComponentType(salaryDefinition.map(SalaryComponentDefinitions::getComponentType).orElse(null));
            }
        }

        List<SalaryComponentDTO> earningsList = null;
        List<SalaryComponentDTO> deductionsList = null;
        if (personnelPayslipHistoryOptional.isPresent()) {
            earningsList = personnelPayslipHistoryOptional.get().getEarnings();
            deductionsList = personnelPayslipHistoryOptional.get().getDeductions();
            payslipHistory.setId(personnelPayslipHistoryOptional.get().getId());
            if (!ObjectUtils.isEmpty(salaryComponent.getEarningsList())) {
                for (SalaryComponentDTO salaryComponentDTO: salaryComponent.getEarningsList()) {
                    Optional<SalaryComponentDTO> salaryComponentDTOOptional = earningsList.stream().filter(history -> Objects.equals(history.getSalaryComponentDefinitionsId(), salaryComponentDTO.getSalaryComponentDefinitionsId())).findFirst();
                    salaryComponentDTOOptional.ifPresent(earningsList::remove);
                    earningsList.add(salaryComponentDTO);
                }
            }
            if (!ObjectUtils.isEmpty(salaryComponent.getDeductionsList())) {
                for (SalaryComponentDTO salaryComponentDTO: salaryComponent.getDeductionsList()) {
                    Optional<SalaryComponentDTO> salaryComponentDTOOptional = deductionsList.stream().filter(history -> Objects.equals(history.getSalaryComponentDefinitionsId(), salaryComponentDTO.getSalaryComponentDefinitionsId())).findFirst();
                    salaryComponentDTOOptional.ifPresent(deductionsList::remove);
                    deductionsList.add(salaryComponentDTO);
                }
            }
            logger.info("Updating existing payslip history for PersonnelCode: {}, Month: {}, Year: {}",
                    salaryComponent.getPersonnelCode(), salaryComponent.getSalaryMonth(), salaryComponent.getSalaryYear());
        } else {
            SalaryComponentResponseDTO salaryComponentResponseDTO = this.constructSalaryComponentResponseDTO(salaryComponent.getPersonnelCode(), false);
            if (salaryComponentResponseDTO != null) {
                earningsList = salaryComponentResponseDTO.getEarningsList();
                deductionsList = salaryComponentResponseDTO.getDeductionsList();
            } else {
                earningsList = new ArrayList<>();
                deductionsList = new ArrayList<>();
            }
            if (!ObjectUtils.isEmpty(salaryComponent.getEarningsList())) {
                earningsList.addAll(salaryComponent.getEarningsList());
            }
            if (!ObjectUtils.isEmpty(salaryComponent.getDeductionsList())) {
                deductionsList.addAll(salaryComponent.getDeductionsList());
            }
            logger.info("Creating new payslip history for PersonnelCode: {}, Month: {}, Year: {}",
                    salaryComponent.getPersonnelCode(), salaryComponent.getSalaryMonth(), salaryComponent.getSalaryYear());
        }
        payslipHistory.setEarnings(earningsList);
        payslipHistory.setDeductions(deductionsList);
        Set<Long> includedEarningIds = salaryComponentDefinitions.stream()
                .filter(def -> def.getIncludeInTotal() && "EARNING".equalsIgnoreCase(def.getComponentCategory()))
                .map(SalaryComponentDefinitions::getId)
                .collect(Collectors.toSet());

        Set<Long> includedDeductionIds = salaryComponentDefinitions.stream()
                .filter(def -> def.getIncludeInTotal() && "DEDUCTION".equalsIgnoreCase(def.getComponentCategory()))
                .map(SalaryComponentDefinitions::getId)
                .collect(Collectors.toSet());

        BigDecimal totalEarning = calculateTotal(earningsList, includedEarningIds);
        BigDecimal totalDeduction = calculateTotal(deductionsList, includedDeductionIds);

        payslipHistory.setTotalEarning(totalEarning);
        payslipHistory.setTotalDeduction(totalDeduction);
        payslipHistory.setSalaryAmount(totalEarning.subtract(totalDeduction));
        personnelPayslipHistoryRepository.save(payslipHistory);
        logger.info("Payslip history saved for PersonnelCode: {}, Month: {}, Year: {}",
                salaryComponent.getPersonnelCode(), salaryComponent.getSalaryMonth(), salaryComponent.getSalaryYear());

        SalaryComponentResponseDTO salaryResponse = new SalaryComponentResponseDTO();
        salaryResponse.setTenantId(payslipHistory.getTenantId());
        salaryResponse.setStoreId(payslipHistory.getStoreId());
        salaryResponse.setDeductionsList(payslipHistory.getDeductions());
        salaryResponse.setEarningsList(payslipHistory.getEarnings());
        salaryResponse.setSalaryMonth(payslipHistory.getSalaryMonth());
        salaryResponse.setSalaryYear(payslipHistory.getSalaryYear());
        salaryResponse.setTotalEarning(payslipHistory.getTotalEarning());
        salaryResponse.setTotalDeduction(payslipHistory.getTotalDeduction());
        salaryResponse.setSalaryAmount(payslipHistory.getSalaryAmount());

        responseModel.setData(salaryResponse);
        responseModel.setCode(HttpStatus.OK);
        responseModel.setMessage(SUCCESS);

        return responseModel;
    }

    public ResponseModel getPayslipData(Long personnelCode, String month, Integer year) {
        ResponseModel responseModel = new ResponseModel();
        Optional<PersonnelPayslipHistory> personnelPayslipHistory = personnelPayslipHistoryRepository.findByPersonnelIdAndSalaryMonthAndSalaryYear(personnelCode, month, year);

        responseModel.setCode(HttpStatus.OK);
        if(personnelPayslipHistory.isPresent()){
            SalaryComponentsDTO salarySlip = convertToSalaryComponentDTO(personnelPayslipHistory.get());
            responseModel.setData(salarySlip);
            responseModel.setMessage(SUCCESS);
        } else {
            responseModel.setMessage("No Data Found");
        }

        return responseModel;
    }

    public ResponseModel getSalaryComponentDefinitions(Long tenantId, Long storeId) {
        ResponseModel responseModel = new ResponseModel();
        List<SalaryComponentDefinitions> salaryComponents = definitionsRepository.findByTenantIdAndStoreId(tenantId, storeId);
        if (ObjectUtils.isEmpty(salaryComponents)) {
            salaryComponents = definitionsRepository.findByTenantIdAndStoreId(tenantId, 0L);
        }

        responseModel.setCode(HttpStatus.OK);
        if(!ObjectUtils.isEmpty(salaryComponents)) {
            responseModel.setData(salaryComponents);
            responseModel.setMessage(SUCCESS);
        } else {
            responseModel.setMessage("No Data Found");
        }
        return responseModel;
    }

    public ResponseModel getPersonnelSalaryComponents(Long personnelCode) {
        ResponseModel responseModel = new ResponseModel();

        responseModel.setMessage(SUCCESS);
        responseModel.setCode(HttpStatus.OK);
        SalaryComponentResponseDTO responseDTO = this.constructSalaryComponentResponseDTO(personnelCode, true);
        responseModel.setData(responseDTO);
        return responseModel;
    }

    private SalaryComponentResponseDTO constructSalaryComponentResponseDTO(Long personnelCode, Boolean isAnnual) {
        SalaryComponentResponseDTO responseDTO = null;
        List<PersonnelSalaryComponents> existingComponents =
                personnelSalaryComponentsRepository.findByPersonnelCode(personnelCode);
        if(!ObjectUtils.isEmpty(existingComponents)){
            responseDTO = getSalaryResponse(existingComponents, isAnnual);
        }
        return responseDTO;
    }

    private SalaryComponentResponseDTO getSalaryResponse(List<PersonnelSalaryComponents> salaryComponents, Boolean isAnnual) {
        SalaryComponentResponseDTO responseDTO = new SalaryComponentResponseDTO();
        List<SalaryComponentDTO> earnings = new ArrayList<>();
        List<SalaryComponentDTO> deductions = new ArrayList<>();
        BigDecimal totalEarning = BigDecimal.ZERO;
        BigDecimal totalDeduction = BigDecimal.ZERO;

        PersonnelSalaryComponents base = salaryComponents.get(0); // assume tenantId, storeId, personnelCode are same for all
        responseDTO.setTenantId(base.getTenantId());
        responseDTO.setStoreId(base.getStoreId());
        responseDTO.setPersonnelCode(base.getPersonnelCode());

        for (PersonnelSalaryComponents comp : salaryComponents) {
            SalaryComponentDefinitions def = comp.getSalaryComponentDefinitions();
            if (def == null) continue;

            SalaryComponentDTO dto = new SalaryComponentDTO();
            dto.setSalaryComponentDefinitionsId(def.getId());
            if(isAnnual){
                dto.setAnnualValue(comp.getAnnualValue());
            } else {
                dto.setMonthlyValue(comp.getMonthlyValue());
                dto.setCalculatedMonthly(comp.getSalaryComponentDefinitions().getIsCalculatedMonthly());
            }
            dto.setComponentName(def.getComponentName());
            dto.setComponentType(def.getComponentType());

            if ("EARNING".equalsIgnoreCase(def.getComponentCategory())) {
                if(def.getIncludeInTotal()) {
                    if(isAnnual){
                        totalEarning = totalEarning.add(comp.getAnnualValue());
                    } else {
                        totalEarning = totalEarning.add(comp.getMonthlyValue());
                    }
                }
                earnings.add(dto);
            } else if ("DEDUCTION".equalsIgnoreCase(def.getComponentCategory())) {
                if(def.getIncludeInTotal()) {
                    if(isAnnual){
                        totalDeduction = totalDeduction.add(comp.getAnnualValue());
                    } else {
                        totalDeduction = totalDeduction.add(comp.getMonthlyValue());
                    }
                }
                deductions.add(dto);
            }
        }

        responseDTO.setEarningsList(earnings);
        responseDTO.setDeductionsList(deductions);
        responseDTO.setTotalDeduction(totalDeduction);
        responseDTO.setTotalEarning(totalEarning);
        responseDTO.setSalaryAmount(totalEarning.subtract(totalDeduction));
        return responseDTO;
    }

    public ResponseModel getPersonnelSalaryComponentsForMonth(Long personnelCode, String month, Integer year) {
        ResponseModel responseModel = new ResponseModel();
        Optional<PersonnelPayslipHistory> paySlipDataOptional = personnelPayslipHistoryRepository.findByPersonnelIdAndSalaryMonthAndSalaryYear(personnelCode, month, year);
        SalaryComponentResponseDTO salaryComponentResponseDTO = new SalaryComponentResponseDTO();
        if(paySlipDataOptional.isPresent()) {
            PersonnelPayslipHistory payslipHistory = paySlipDataOptional.get();
            salaryComponentResponseDTO.setTenantId(payslipHistory.getTenantId());
            salaryComponentResponseDTO.setStoreId(payslipHistory.getStoreId());
            salaryComponentResponseDTO.setDeductionsList(payslipHistory.getDeductions());
            salaryComponentResponseDTO.setEarningsList(payslipHistory.getEarnings());
            salaryComponentResponseDTO.setTotalEarning(payslipHistory.getTotalEarning());
            salaryComponentResponseDTO.setTotalDeduction(payslipHistory.getTotalDeduction());
            salaryComponentResponseDTO.setSalaryAmount(payslipHistory.getSalaryAmount());
        } else {
            List<PersonnelSalaryComponents> existingComponents =
                    personnelSalaryComponentsRepository.findByPersonnelCode(personnelCode);
            if(!ObjectUtils.isEmpty(existingComponents)){
                salaryComponentResponseDTO = getSalaryResponse(existingComponents, false);
            }
        }
        salaryComponentResponseDTO.setSalaryMonth(month);
        salaryComponentResponseDTO.setSalaryYear(year);
        responseModel.setMessage(SUCCESS);
        responseModel.setCode(HttpStatus.OK);
        responseModel.setData(salaryComponentResponseDTO);
        return responseModel;
    }

    public SalaryComponentsDTO convertToSalaryComponentDTO(PersonnelPayslipHistory entity) {
        SalaryComponentsDTO dto = new SalaryComponentsDTO();
        dto.setTenantId(entity.getTenantId());
        dto.setStoreId(entity.getStoreId());
        dto.setPersonnelId(entity.getPersonnelId());
        dto.setPersonnelName(entity.getPersonnelName());
        dto.setSalaryDate(entity.getSalaryDate());
        dto.setTotalEarning(entity.getTotalEarning());
        dto.setTotalDeduction(entity.getTotalDeduction());
        dto.setSalaryAmount(entity.getSalaryAmount());
        dto.setTotalDays(entity.getTotalDays());
        dto.setTotalWorkingDays(entity.getTotalWorkingDays());
        dto.setAbsentDays(entity.getAbsentDays());
        dto.setPenaltyAbsentDays(entity.getPenaltyAbsentDays());
        dto.setTotalHolidays(entity.getTotalHolidays());
        dto.setTotalweeklyOff(entity.getTotalweeklyOff());
        dto.setBankName(entity.getBankName());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setIfscCode(entity.getIfscCode());
        dto.setPanNo(entity.getPanNo());
        dto.setSalaryPeriod(entity.getSalaryPeriod());
        dto.setDesignation(entity.getDesignation());
        dto.setEmployeeCode(entity.getEmployeeCode());
        dto.setStoreName(entity.getStoreName());
        dto.setSalaryMonth(entity.getSalaryMonth());
        dto.setSalaryYear(entity.getSalaryYear());
        dto.setTotalPaidLeaves(entity.getTotalPaidLeaves());
        dto.setTotalPaidDays(entity.getTotalPaidDays());
        dto.setEarnings(convertListToMap(entity.getEarnings()));
        dto.setDeductions(convertListToMap(entity.getDeductions()));
        dto.setUanNumber(entity.getUanNumber());
        return dto;
    }

    private Map<String, BigDecimal> convertListToMap(List<SalaryComponentDTO> components) {
        Map<String, BigDecimal> result = new HashMap<>();
        if(!ObjectUtils.isEmpty(components)) {
            for(SalaryComponentDTO dto : components) {
                result.put(dto.getComponentName(), dto.getMonthlyValue());
            }
        }
        return result;
    }
}
