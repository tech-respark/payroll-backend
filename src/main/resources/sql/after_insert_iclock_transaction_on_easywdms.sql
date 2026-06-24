DELIMITER $$

CREATE TRIGGER after_insert_iclock_transaction
AFTER INSERT ON easywdms.iclock_transaction
FOR EACH ROW
BEGIN
    DECLARE seq_no BIGINT;
    DECLARE tenant_id BIGINT DEFAULT NULL;
    DECLARE store_id BIGINT DEFAULT NULL;
	DECLARE application_name VARCHAR(255) DEFAULT NULL;

    -- Get the sequence number for the punch
    SELECT COALESCE(MAX(sequence_number_of_punch), 0) + 1
    INTO seq_no
    FROM pcs_personnel_management.personnel_attendance
    WHERE terminal_serial_number = NEW.terminal_sn
      AND personnel_code = NEW.emp_code
      AND attendance_date = DATE(NEW.punch_time);

    -- Retrieve tenantId and storeId if the related entries exist
    SELECT 
        sd.store_id, 
        tcm.tenant_id,
		tcm.application_name
    INTO 
        store_id, 
        tenant_id,
		application_name
    FROM 
        pcs_personnel_management.terminal_details td
    JOIN 
        pcs_personnel_management.store_details sd ON td.store_details_id = sd.id
    JOIN 
        pcs_personnel_management.tenant_company_mapping tcm ON sd.tenant_company_mapping_id = tcm.id
    WHERE 
        td.terminal_serial_number = NEW.terminal_sn
    LIMIT 1;

    -- Insert the new record with calculated values
    INSERT INTO pcs_personnel_management.personnel_attendance (
        terminal_serial_number,
        personnel_code,
        attendance_date,
        attendance_day_of_week,
        punch_event,
        punch_timestamp,
        iclock_transaction_id,
        upload_source,
        created_timestamp,
        created_by,
        modified_timestamp,
        modified_by,
        sequence_number_of_punch,
        tenant_id,
        store_id,
		application_name,
		version_id,
		current_status
    )
    VALUES (
        NEW.terminal_sn,
        NEW.emp_code,
        DATE(NEW.punch_time),
        DAYNAME(NEW.punch_time),
        CASE 
            WHEN NEW.punch_state = '0' THEN 'CHECKIN'
            WHEN NEW.punch_state = '1' THEN 'CHECKOUT'
            WHEN NEW.punch_state = '4' THEN 'OVERTIME_CHECKIN'
            WHEN NEW.punch_state = '5' THEN 'OVERTIME_CHECKOUT'
            ELSE NULL
        END,
        NEW.punch_time,
        NEW.id,
        'TERMINAL',
        NOW(),
        0,
        NOW(),
        0,
        seq_no,
        tenant_id,
        store_id,
		application_name,
		0,
		'APPROVED'
    );
END $$

DELIMITER ;