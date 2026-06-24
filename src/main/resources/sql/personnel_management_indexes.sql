CREATE INDEX dwas_idx_tenant_store_app_date
ON day_wise_attendance_summary (tenant_id, store_id, application_name, attendance_date);

CREATE INDEX dwas_idx_tenant_store_personnel_app_date
ON day_wise_attendance_summary (tenant_id, store_id, personnel_code, application_name, attendance_date);

CREATE INDEX pa_idx_date_app_tenant_store_personnel
ON personnel_attendance (attendance_date, application_name, tenant_id, store_id, personnel_code);