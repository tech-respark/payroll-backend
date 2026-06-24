CREATE TABLE pcs_personnel_management.personnel_details (
  `id` bigint NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `application_name` varchar(255) DEFAULT NULL,
  `application_tenant_id` bigint DEFAULT NULL,
  `designation` varchar(255) DEFAULT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `gender` varchar(15) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `personnel_code` bigint DEFAULT NULL,
  `personnel_mobile_number` varchar(255) DEFAULT NULL,
  `tenant_company_mapping_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
);