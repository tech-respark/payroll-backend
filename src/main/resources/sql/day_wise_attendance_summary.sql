CREATE TABLE payroll_management.day_wise_attendance_summary (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attendance_date` date DEFAULT NULL,
  `attendance_day_of_week` varchar(255) DEFAULT NULL,
  `staff_id` bigint DEFAULT NULL,
  `terminal_serial_number` varchar(255) DEFAULT NULL,
  `total_break_time_inaday` decimal(19,2) DEFAULT NULL,
  `total_hours_worked_inaday` decimal(19,2) DEFAULT NULL,
  `application_name` varchar(255) DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  `sum_of_actual_hours_worked_inaday` decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
