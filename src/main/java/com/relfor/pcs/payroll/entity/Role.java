package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "s_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "r_index")
    private Integer rIndex;
    
    @Column(name = "r_value")
    private Integer rValue;
    
    @Column(name = "active")
    private Integer active = 1;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Column(name = "hide_from_ui")
    private Boolean hideFromUi;
    
    @Column(name = "assigned_reports", columnDefinition = "TEXT")
    private String assignedReports;
    
    @Column(name = "restriction_days")
    private Long restrictionDays;
}
