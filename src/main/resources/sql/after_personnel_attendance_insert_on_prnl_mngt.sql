DELIMITER $$

CREATE TRIGGER after_personnel_attendance_insert
AFTER INSERT ON payroll_management.personnel_attendance
FOR EACH ROW
BEGIN
    DECLARE last_punch_time DATETIME;
    DECLARE first_punch_time DATETIME;
    DECLARE hours_worked DECIMAL(19,2);
	DECLARE total_hours_worked DECIMAL(19,2);
	DECLARE break_time DECIMAL(19,2);
    DECLARE existing_summary_id BIGINT;

	-- Check if the upload source is 'TERMINAL'
    IF NEW.upload_source = 'TERMINAL' THEN

		-- Odd sequence logic
		IF MOD(NEW.sequence_number_of_punch, 2) = 1 AND NEW.sequence_number_of_punch > 1 THEN
			SELECT punch_timestamp
			INTO last_punch_time
			FROM payroll_management.personnel_attendance
			WHERE terminal_serial_number = NEW.terminal_serial_number
			  AND attendance_date = NEW.attendance_date
			  AND staff_id = NEW.staff_id
			  AND sequence_number_of_punch = NEW.sequence_number_of_punch - 1;

			SET hours_worked = 0;
			SET break_time = TIMESTAMPDIFF(SECOND, last_punch_time, NEW.punch_timestamp) / 3600;
		END IF;

		-- Even sequence logic
		IF MOD(NEW.sequence_number_of_punch, 2) = 0 THEN
			SELECT punch_timestamp
			INTO last_punch_time
			FROM payroll_management.personnel_attendance
			WHERE terminal_serial_number = NEW.terminal_serial_number
			  AND attendance_date = NEW.attendance_date
			  AND staff_id = NEW.staff_id
			  AND sequence_number_of_punch = NEW.sequence_number_of_punch - 1;

			SET hours_worked = TIMESTAMPDIFF(SECOND, last_punch_time, NEW.punch_timestamp) / 3600;
			SET break_time = 0;
		END IF;

		-- Non-time-based logic to calculate total_hours_worked_inaday
		IF NEW.sequence_number_of_punch > 1 THEN
			SELECT MIN(punch_timestamp)
			INTO first_punch_time
			FROM payroll_management.personnel_attendance
			WHERE terminal_serial_number = NEW.terminal_serial_number
			  AND attendance_date = NEW.attendance_date
			  AND staff_id = NEW.staff_id;

			SET total_hours_worked = TIMESTAMPDIFF(SECOND, first_punch_time, NEW.punch_timestamp) / 3600;
		END IF;

		-- Check if summary entry exists
		SELECT id
		INTO existing_summary_id
		FROM payroll_management.day_wise_attendance_summary
		WHERE terminal_serial_number = NEW.terminal_serial_number
		  AND attendance_date = NEW.attendance_date
		  AND staff_id = NEW.staff_id;

		IF existing_summary_id IS NOT NULL THEN
			UPDATE payroll_management.day_wise_attendance_summary
			SET sum_of_actual_hours_worked_inaday = COALESCE(sum_of_actual_hours_worked_inaday, 0) + hours_worked,
				total_break_time_inaday = COALESCE(total_break_time_inaday, 0) + break_time,
				total_hours_worked_inaday = total_hours_worked
			WHERE id = existing_summary_id;
		ELSE
			INSERT INTO payroll_management.day_wise_attendance_summary (
				attendance_date,
				attendance_day_of_week,
				staff_id,
				terminal_serial_number,
				tenant_id,
				store_id,
				application_name,
				total_break_time_inaday,
				sum_of_actual_hours_worked_inaday,
				total_hours_worked_inaday
			)
			VALUES (
				NEW.attendance_date,
				NEW.attendance_day_of_week,
				NEW.staff_id,
				NEW.terminal_serial_number,
				NEW.tenant_id,
				NEW.store_id,
				NEW.application_name,
				break_time,
				hours_worked,
				total_hours_worked
			);
		END IF;

	END IF;
END $$

DELIMITER ;
