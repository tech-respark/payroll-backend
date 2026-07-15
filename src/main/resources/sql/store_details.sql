CREATE TABLE payroll_management.store_details (
  `id` bigint NOT NULL,
  `is_actual_time_based_attendance` bit(1) DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `store_name` varchar(255) DEFAULT NULL,
  `tenant_company_mapping_id` bigint DEFAULT NULL,
  `time_zone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
