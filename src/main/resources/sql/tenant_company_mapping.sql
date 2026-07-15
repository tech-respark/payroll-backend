CREATE TABLE payroll_management.tenant_company_mapping (
  `id` bigint NOT NULL,
  `application_name` varchar(255) DEFAULT NULL,
  `personnel_company_id` varchar(255) DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  `tenant_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
