CREATE TABLE pcs_personnel_management.terminal_details (
  `id` bigint NOT NULL,
  `store_details_id` bigint DEFAULT NULL,
  `terminal_serial_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);