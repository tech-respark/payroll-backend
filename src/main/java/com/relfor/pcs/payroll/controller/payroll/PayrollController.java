package com.relfor.pcs.payroll.controller.payroll;

import com.relfor.pcs.payroll.dto.ResponseModel;
import com.relfor.pcs.payroll.entity.ItDeclaration;
import com.relfor.pcs.payroll.entity.ReimbursementClaim;
import com.relfor.pcs.payroll.entity.SalaryRevision;
import com.relfor.pcs.payroll.entity.StaffLoan;
import com.relfor.pcs.payroll.entity.StatutoryConfig;
import com.relfor.pcs.payroll.security.Permissions;
import com.relfor.pcs.payroll.service.payroll.PayRunService;
import com.relfor.pcs.payroll.service.payroll.PayrollOpsService;
import com.relfor.pcs.payroll.service.payroll.TotpLoginService;
import com.relfor.pcs.payroll.util.ResponseHandler;
import com.relfor.pcs.payroll.util.SecurityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/payroll-management/v1/payroll")
public class PayrollController {

    private final PayRunService payRunService;
    private final PayrollOpsService payrollOpsService;
    private final TotpLoginService totpLoginService;

    public PayrollController(PayRunService payRunService,
                             PayrollOpsService payrollOpsService,
                             TotpLoginService totpLoginService) {
        this.payRunService = payRunService;
        this.payrollOpsService = payrollOpsService;
        this.totpLoginService = totpLoginService;
    }

    @GetMapping("/runs")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> runs(@RequestParam Long tenantId, @RequestParam Long storeId,
                                              @RequestParam(required = false) String month,
                                              @RequestParam(required = false) Integer year,
                                              @RequestParam(required = false) String cycleType) {
        if (month != null && year != null) {
            return ok(payRunService.find(scopeTenant(tenantId), scopeStore(storeId), month, year, cycleType));
        }
        return ok(payRunService.list(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PostMapping("/runs")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> openRun(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                 @RequestBody Map<String, Object> body) {
        return ok(payRunService.getOrCreate(scopeTenant(tenantId), scopeStore(storeId),
                text(body.get("month")), intVal(body.get("year")), text(body.get("cycleType"))));
    }

    @GetMapping("/runs/{id}")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> run(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id) {
        return ok(payRunService.get(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @PostMapping("/runs/{id}/calculate")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> calculate(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id) {
        return ok(payRunService.calculate(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @PostMapping("/runs/{id}/skip")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> skip(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id,
                                              @RequestBody Map<String, Object> body) {
        return ok(payRunService.skip(scopeTenant(tenantId), scopeStore(storeId), id,
                longVal(body.get("staffId")), text(body.get("reason")), Boolean.TRUE.equals(body.get("remove"))));
    }

    @PostMapping("/runs/{id}/submit")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> submit(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id) {
        return ok(payRunService.submit(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @PostMapping("/runs/{id}/approve")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> approve(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id) {
        return ok(payRunService.approve(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @PostMapping("/runs/{id}/publish")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> publish(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id) {
        return ok(payRunService.publish(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @PostMapping("/runs/{id}/lock")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> lock(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id) {
        return ok(payRunService.lock(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @GetMapping("/statutory")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> statutory(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(payrollOpsService.getStatutory(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PutMapping("/statutory")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> saveStatutory(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                       @RequestBody StatutoryConfig body) {
        return ok(payrollOpsService.saveStatutory(scopeTenant(tenantId), scopeStore(storeId), body));
    }

    @GetMapping("/claims")
    public ResponseEntity<ResponseModel> claims(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                @RequestParam(required = false) Long staffId) {
        boolean manager = SecurityUtils.hasAnyAuthority(Permissions.MANAGE_SALARY);
        Long scopedStaff = manager ? staffId : SecurityUtils.getStaffId();
        if (!manager) {
            SecurityUtils.requireSelfOrAnyAuthority(scopedStaff, Permissions.MANAGE_SALARY);
        }
        return ok(payrollOpsService.listClaims(scopeTenant(tenantId), scopeStore(storeId), scopedStaff, manager));
    }

    @PostMapping("/claims")
    public ResponseEntity<ResponseModel> createClaim(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                     @RequestBody ReimbursementClaim body) {
        if (!SecurityUtils.hasAuthority(Permissions.MANAGE_SALARY)) {
            body.setStaffId(SecurityUtils.getStaffId());
        }
        return created(payrollOpsService.createClaim(scopeTenant(tenantId), scopeStore(storeId), body));
    }

    @PostMapping("/claims/{id}/decide")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> decideClaim(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                     @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ok(payrollOpsService.decideClaim(scopeTenant(tenantId), scopeStore(storeId), id,
                Boolean.TRUE.equals(body.get("approved")), text(body.get("remarks"))));
    }

    @GetMapping("/declarations")
    public ResponseEntity<ResponseModel> declarations(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                      @RequestParam(required = false) Long staffId) {
        boolean manager = SecurityUtils.hasAnyAuthority(Permissions.MANAGE_SALARY);
        Long scopedStaff = manager ? staffId : SecurityUtils.getStaffId();
        return ok(payrollOpsService.listDeclarations(scopeTenant(tenantId), scopeStore(storeId), scopedStaff, manager));
    }

    @PostMapping("/declarations")
    public ResponseEntity<ResponseModel> saveDeclaration(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                         @RequestBody ItDeclaration body) {
        if (!SecurityUtils.hasAuthority(Permissions.MANAGE_SALARY)) {
            body.setStaffId(SecurityUtils.getStaffId());
        }
        return created(payrollOpsService.saveDeclaration(scopeTenant(tenantId), scopeStore(storeId), body));
    }

    @PostMapping("/declarations/{id}/decide")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> decideDeclaration(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                           @PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal approvedTotal = body.get("approvedTotal") != null
                ? new BigDecimal(String.valueOf(body.get("approvedTotal"))) : null;
        return ok(payrollOpsService.decideDeclaration(scopeTenant(tenantId), scopeStore(storeId), id,
                Boolean.TRUE.equals(body.get("approved")), approvedTotal));
    }

    @GetMapping("/loans")
    public ResponseEntity<ResponseModel> loans(@RequestParam Long tenantId, @RequestParam Long storeId,
                                               @RequestParam(required = false) Long staffId) {
        boolean manager = SecurityUtils.hasAnyAuthority(Permissions.MANAGE_SALARY);
        Long scopedStaff = manager ? staffId : SecurityUtils.getStaffId();
        return ok(payrollOpsService.listLoans(scopeTenant(tenantId), scopeStore(storeId), scopedStaff, manager));
    }

    @PostMapping("/loans")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> createLoan(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                    @RequestBody StaffLoan body) {
        return created(payrollOpsService.createLoan(scopeTenant(tenantId), scopeStore(storeId), body));
    }

    @PostMapping("/loans/{id}/close")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> closeLoan(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id) {
        return ok(payrollOpsService.closeLoan(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @GetMapping("/revisions")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> revisions(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                   @RequestParam(required = false) Long staffId) {
        return ok(payrollOpsService.listRevisions(scopeTenant(tenantId), scopeStore(storeId), staffId));
    }

    @PostMapping("/revisions")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> createRevision(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                        @RequestBody SalaryRevision body) {
        return created(payrollOpsService.createRevision(scopeTenant(tenantId), scopeStore(storeId), body));
    }

    @GetMapping("/settlements")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> settlements(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(payrollOpsService.listSettlements(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PostMapping("/settlements")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> createSettlement(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                          @RequestBody Map<String, Object> body) {
        return created(payrollOpsService.createSettlement(scopeTenant(tenantId), scopeStore(storeId), body));
    }

    @PostMapping("/settlements/{id}/settle")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> settle(@RequestParam Long tenantId, @RequestParam Long storeId, @PathVariable Long id) {
        return ok(payrollOpsService.settle(scopeTenant(tenantId), scopeStore(storeId), id));
    }

    @GetMapping("/cost")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).VIEW_REPORTS, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<ResponseModel> cost(@RequestParam Long tenantId, @RequestParam Long storeId,
                                              @RequestParam String month, @RequestParam Integer year) {
        return ok(payrollOpsService.costReport(scopeTenant(tenantId), scopeStore(storeId), month, year));
    }

    @GetMapping("/filings/{kind}")
    @PreAuthorize("hasAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<byte[]> filing(@RequestParam Long tenantId, @RequestParam Long storeId,
                                         @RequestParam String month, @RequestParam Integer year,
                                         @PathVariable String kind) {
        String csv = payrollOpsService.filingCsv(scopeTenant(tenantId), scopeStore(storeId), month, year, kind);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + kind + "-" + month + "-" + year + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/form16")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY)")
    public ResponseEntity<byte[]> form16(@RequestParam Long tenantId, @RequestParam Long storeId,
                                         @RequestParam Long staffId, @RequestParam Integer year) {
        SecurityUtils.requireSelfOrAnyAuthority(staffId, Permissions.MANAGE_SALARY);
        String html = payrollOpsService.form16Html(scopeTenant(tenantId), scopeStore(storeId), staffId, year);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=form16-" + staffId + "-" + year + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(html.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/logo")
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).VIEW_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_STORE_SETTINGS)")
    public ResponseEntity<ResponseModel> logo(@RequestParam Long tenantId, @RequestParam Long storeId) {
        return ok(payrollOpsService.logoStatus(scopeTenant(tenantId), scopeStore(storeId)));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority(T(com.relfor.pcs.payroll.security.Permissions).MANAGE_SALARY, T(com.relfor.pcs.payroll.security.Permissions).MANAGE_STORE_SETTINGS)")
    public ResponseEntity<ResponseModel> uploadLogo(@RequestParam Long tenantId, @RequestParam Long storeId,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        return ok(payrollOpsService.saveLogo(scopeTenant(tenantId), scopeStore(storeId), file));
    }

    @GetMapping("/me/totp")
    public ResponseEntity<ResponseModel> totpStatus() {
        return ok(totpLoginService.status());
    }

    @PostMapping("/me/totp/start")
    public ResponseEntity<ResponseModel> totpStart() {
        return ok(totpLoginService.start());
    }

    @PostMapping("/me/totp/confirm")
    public ResponseEntity<ResponseModel> totpConfirm(@RequestBody Map<String, Object> body) {
        return ok(totpLoginService.confirm(text(body.get("code")), text(body.get("secret"))));
    }

    @PostMapping("/me/totp/disable")
    public ResponseEntity<ResponseModel> totpDisable(@RequestBody Map<String, Object> body) {
        return ok(totpLoginService.disable(text(body.get("code"))));
    }

    private ResponseEntity<ResponseModel> ok(Object data) {
        ResponseModel model = new ResponseModel();
        model.setCode(HttpStatus.OK);
        model.setMessage("SUCCESS");
        model.setData(data);
        return ResponseHandler.generateResponseModel(model);
    }

    private ResponseEntity<ResponseModel> created(Object data) {
        ResponseModel model = new ResponseModel();
        model.setCode(HttpStatus.OK);
        model.setMessage("SUCCESS");
        model.setData(data);
        return ResponseHandler.generateResponseModel(model);
    }

    private Long scopeTenant(Long tenantId) {
        return SecurityUtils.getTenantId(tenantId);
    }

    private Long scopeStore(Long storeId) {
        return SecurityUtils.getStoreId(storeId);
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer intVal(Object value) {
        return value == null ? null : Integer.valueOf(String.valueOf(value));
    }

    private Long longVal(Object value) {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }
}
