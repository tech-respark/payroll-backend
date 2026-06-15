package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "s_store_staff_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreStaffRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "store_id")
    private Long storeId;
    
    @Column(name = "staff_id")
    private Long staffId;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Column(name = "role_id")
    private Long roleId;
    
    @Column(name = "active")
    private Integer active;
    
    @Column(name = "system_created_on")
    private LocalDateTime systemCreatedOn;
    
    @Column(name = "system_updated_on")
    private LocalDateTime systemUpdatedOn;
    
    @Column(name = "enable_appointments")
    private Integer enableAppointments = 0;
}
