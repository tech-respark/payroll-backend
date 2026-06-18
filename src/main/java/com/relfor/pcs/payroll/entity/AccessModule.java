package com.relfor.pcs.payroll.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "access_modules")
public class AccessModule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    private Long storeId;

    private String category;
    
    @Column(name = "icon_svg", columnDefinition = "TEXT")
    private String icon;

    @OneToMany(mappedBy = "accessModule", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AccessPermission> permissions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<AccessPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<AccessPermission> permissions) {
        this.permissions = permissions;
    }
}
