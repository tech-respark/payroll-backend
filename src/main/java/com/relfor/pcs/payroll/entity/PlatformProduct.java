package com.relfor.pcs.payroll.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "platform_product")
public class PlatformProduct {

    public enum IntegrationMode {
        STANDALONE,
        CONNECTOR
    }

    public enum Category {
        BILLING,
        POS,
        HRIS,
        ATTENDANCE,
        OTHER
    }

    public enum Origin {
        FIRST_PARTY,
        THIRD_PARTY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(length = 400)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_mode", nullable = false, length = 20)
    private IntegrationMode integrationMode = IntegrationMode.STANDALONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category = Category.BILLING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Origin origin = Origin.THIRD_PARTY;

    @Column(name = "default_product", nullable = false)
    private Boolean defaultProduct = Boolean.FALSE;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IntegrationMode getIntegrationMode() {
        return integrationMode;
    }

    public void setIntegrationMode(IntegrationMode integrationMode) {
        this.integrationMode = integrationMode;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public Boolean getDefaultProduct() {
        return defaultProduct;
    }

    public void setDefaultProduct(Boolean defaultProduct) {
        this.defaultProduct = defaultProduct;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
