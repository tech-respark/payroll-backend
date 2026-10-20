package com.relfor.pcs.payroll.controller;

import com.relfor.pcs.payroll.dto.HospitalityDtos;
import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.util.ResponseHandler;
import com.relfor.pcs.payroll.util.SecurityUtils;
import com.relfor.pcs.payroll.service.HospitalityService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll-management/v1/hospitality")
public class HospitalityController {

    private final HospitalityService hospitalityService;

    public HospitalityController(HospitalityService hospitalityService) {
        this.hospitalityService = hospitalityService;
    }

    @GetMapping("/advances")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> advances(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                  @RequestParam(required = false) Long staffId) {
        return ok(hospitalityService.listAdvances(scopeTenant(tenantId), scopeStore(storeId), staffId));
    }

    @PostMapping("/advances")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> createAdvance(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                       @RequestBody HospitalityDtos.AdvanceRequest request) {
        return created(hospitalityService.createAdvance(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @PostMapping("/advances/{id}/close")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> closeAdvance(@PathVariable Long id, @RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(hospitalityService.closeAdvance(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @GetMapping("/commission-plans")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> commissionPlans(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(hospitalityService.listCommissionPlans(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PostMapping("/commission-plans")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> saveCommissionPlan(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                            @RequestBody HospitalityDtos.CommissionPlanRequest request) {
        return created(hospitalityService.saveCommissionPlan(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @DeleteMapping("/commission-plans/{id}")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> deleteCommissionPlan(@PathVariable Long id, @RequestParam Long tenantId, @RequestParam Long storeId) {
        hospitalityService.deleteCommissionPlan(scopeTenant(tenantId), scopeStore(storeId), id);
        return ok("Commission plan deleted");
    }

    @GetMapping("/sales")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> sales(@RequestParam Long tenantId, @RequestParam Long storeId,
                                               @RequestParam(required = false) String fromDate,
                                               @RequestParam(required = false) String toDate,
                                               @RequestParam(required = false) Long staffId) {
        return ok(hospitalityService.listSales(scopeTenant(tenantId), scopeStore(storeId),
                fromDate != null ? java.time.LocalDate.parse(fromDate) : null,
                toDate != null ? java.time.LocalDate.parse(toDate) : null,
                staffId));
    }

    @PostMapping("/sales")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> createSale(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                    @RequestBody HospitalityDtos.SalesRequest request) {
        return created(hospitalityService.createSale(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @PostMapping("/sales/import")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> importSales(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                     @RequestBody HospitalityDtos.SalesImportRequest request) {
        return ok(hospitalityService.importSales(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @GetMapping("/tip-pools")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> tipPools(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(hospitalityService.listTipPools(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PostMapping("/tip-pools")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> createTipPool(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                       @RequestBody HospitalityDtos.TipPoolRequest request) {
        return created(hospitalityService.createTipPool(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @PostMapping("/tip-pools/{id}/distribute")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> distributeTipPool(@PathVariable Long id, @RequestParam Long tenantId, @RequestParam Long storeId,
                                                           @RequestBody(required = false) HospitalityDtos.TipPoolRequest request) {
        return ok(hospitalityService.distributeTipPool(scopeTenant(tenantId), scopeStore(storeId), id, request));
    }

    @GetMapping("/statutory")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> statutory(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(hospitalityService.getStatutory(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PutMapping("/statutory")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> saveStatutory(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                       @RequestBody HospitalityDtos.StatutoryRequest request) {
        return ok(hospitalityService.saveStatutory(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @GetMapping("/daily-sales")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_REPORTS, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> dailySales(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                    @RequestParam(required = false) String fromDate,
                                                    @RequestParam(required = false) String toDate) {
        return ok(hospitalityService.listDailySales(scopeTenant(tenantId), scopeStore(storeId),
                fromDate != null ? java.time.LocalDate.parse(fromDate) : null,
                toDate != null ? java.time.LocalDate.parse(toDate) : null));
    }

    @PostMapping("/daily-sales")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> upsertDailySales(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                          @RequestBody HospitalityDtos.DailySalesRequest request) {
        return ok(hospitalityService.upsertDailySales(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @GetMapping("/labor")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_REPORTS, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> labor(@RequestParam Long tenantId, @RequestParam Long storeId,
                                               @RequestParam(required = false) String fromDate,
                                               @RequestParam(required = false) String toDate) {
        return ok(hospitalityService.laborReport(scopeTenant(tenantId), scopeStore(storeId),
                fromDate != null ? java.time.LocalDate.parse(fromDate) : null,
                toDate != null ? java.time.LocalDate.parse(toDate) : null));
    }

    @GetMapping("/muster")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_REPORTS, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_ATTENDANCE, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> muster(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                @RequestParam String month, @RequestParam Integer year) {
        return ok(hospitalityService.musterRoll(scopeTenant(tenantId), scopeStore(storeId), month, year));
    }

    @GetMapping("/neft")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<byte[]> neft(@RequestParam Long tenantId, @RequestParam Long storeId,
                                       @RequestParam String month, @RequestParam Integer year) {
        String csv = hospitalityService.neftCsv(scopeTenant(tenantId), scopeStore(storeId), month, year);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=neft-" + month + "-" + year + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @PostMapping("/apply-payroll")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> applyPayroll(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                      @RequestBody HospitalityDtos.PeriodRequest request) {
        return ok(hospitalityService.applyPayroll(scopeTenant(tenantId), scopeStore(storeId), request.getMonth(), request.getYear()));
    }

    @GetMapping("/fnf")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_STAFF, T(com.relfor.pcs.payroll.security.Permissions).VIEW_HOSPITALITY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> fnf(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(hospitalityService.listFnf(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PostMapping("/fnf")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> createFnf(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                   @RequestBody HospitalityDtos.FnfRequest request) {
        return created(hospitalityService.createFnf(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @PostMapping("/fnf/{id}/settle")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> settleFnf(@PathVariable Long id, @RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(hospitalityService.settleFnf(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @GetMapping("/swaps")
    public ResponseEntity<ResponseModel> swaps(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(hospitalityService.listSwaps(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PostMapping("/swaps")
    public ResponseEntity<ResponseModel> createSwap(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                    @RequestBody HospitalityDtos.SwapRequest request) {
        return created(hospitalityService.createSwap(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @PostMapping("/swaps/{id}/decide")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SHIFTS, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_HOSPITALITY)")
    public ResponseEntity<ResponseModel> decideSwap(@PathVariable Long id, @RequestParam Long tenantId, @RequestParam Long storeId,
                                                    @RequestBody HospitalityDtos.SwapDecisionRequest request) {
        return ok(hospitalityService.decideSwap(scopeTenant(tenantId), scopeStore(storeId), id, request.isApproved()));
    }

    @GetMapping("/availability")
    public ResponseEntity<ResponseModel> availability(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                      @RequestParam(required = false) Long staffId) {
        return ok(hospitalityService.listAvailability(scopeTenant(tenantId), scopeStore(storeId), staffId));
    }

    @PostMapping("/availability")
    public ResponseEntity<ResponseModel> saveAvailability(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                          @RequestBody HospitalityDtos.AvailabilityRequest request) {
        return ok(hospitalityService.saveAvailability(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @PostMapping("/clock")
    public ResponseEntity<ResponseModel> clock(@RequestParam Long tenantId, @RequestParam Long storeId,
                                               @RequestBody HospitalityDtos.ClockRequest request) {
        return ok(hospitalityService.clock(scopeTenant(tenantId), scopeStore(storeId), request));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseModel> me(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(hospitalityService.mySummary(scopeTenant(tenantId), scopeStore(storeId)));
    }

    private Long scopeTenant(Long tenantId) {
        return SecurityUtils.getTenantId(tenantId);
    }

    private Long scopeStore(Long storeId) {
        return SecurityUtils.getStoreId(storeId);
    }

    private ResponseEntity<ResponseModel> ok(Object data) {
        ResponseModel response = new ResponseModel();
        response.setCode(HttpStatus.OK);
        response.setMessage("SUCCESS");
        response.setData(data);
        return ResponseHandler.generateResponseModel(response);
    }

    private ResponseEntity<ResponseModel> created(Object data) {
        ResponseModel response = new ResponseModel();
        response.setCode(HttpStatus.CREATED);
        response.setMessage("SUCCESS");
        response.setData(data);
        return ResponseHandler.generateResponseModel(response);
    }
}
