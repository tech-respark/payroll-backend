DELIMITER $$

CREATE TRIGGER after_insert_personnel_employee
AFTER INSERT ON easywdms.personnel_employee
FOR EACH ROW
BEGIN
    DECLARE tenant_mapping_id BIGINT;
    DECLARE tenant_application_name VARCHAR(255);

    -- Lookup the tenant_company_mapping_id and application_name
    SELECT tcm.id, tcm.application_name
    INTO tenant_mapping_id, tenant_application_name
    FROM payroll_management.tenant_company_mapping tcm
    WHERE tcm.personnel_company_id = NEW.company_id;

    -- Check if the entry exists in payroll_management.personnel_details
    IF EXISTS (
        SELECT 1
        FROM payroll_management.personnel_details pd
        WHERE pd.staff_id = NEW.emp_code
            AND pd.application_name = tenant_application_name
    ) THEN
        -- Update the existing entry
        UPDATE payroll_management.personnel_details pd
        SET
            pd.active = TRUE,
            pd.tenant_company_mapping_id = tenant_mapping_id
        WHERE
            pd.staff_id = NEW.emp_code
            AND pd.application_name = tenant_application_name;
    END IF;
END $$

DELIMITER ;
