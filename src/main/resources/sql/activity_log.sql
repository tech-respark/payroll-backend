-- Append-only store activity log. Hibernate ddl-auto=update also creates this
-- table from ActivityLog. Keep this script as the operator reference.

CREATE TABLE IF NOT EXISTS activity_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  occurred_at DATETIME(3) NOT NULL,
  tenant_id BIGINT NOT NULL,
  store_id BIGINT NULL,
  actor_type VARCHAR(20) NOT NULL,
  actor_staff_id BIGINT NULL,
  actor_username VARCHAR(128) NULL,
  actor_display_name VARCHAR(255) NULL,
  module VARCHAR(40) NOT NULL,
  action VARCHAR(80) NOT NULL,
  resource_type VARCHAR(40) NULL,
  resource_id VARCHAR(64) NULL,
  resource_label VARCHAR(255) NULL,
  summary VARCHAR(500) NOT NULL,
  outcome VARCHAR(20) NOT NULL,
  correlation_id VARCHAR(64) NULL,
  request_method VARCHAR(12) NULL,
  request_path VARCHAR(255) NULL,
  ip_address VARCHAR(45) NULL,
  user_agent VARCHAR(255) NULL,
  context_json JSON NULL,
  PRIMARY KEY (id),
  INDEX idx_activity_tenant_time (tenant_id, occurred_at),
  INDEX idx_activity_store_time (tenant_id, store_id, occurred_at),
  INDEX idx_activity_module_time (tenant_id, module, occurred_at),
  INDEX idx_activity_action_time (tenant_id, action, occurred_at),
  INDEX idx_activity_actor_time (tenant_id, actor_staff_id, occurred_at),
  INDEX idx_activity_resource (tenant_id, resource_type, resource_id, occurred_at)
);

DROP TRIGGER IF EXISTS activity_log_prevent_update;
CREATE TRIGGER activity_log_prevent_update
BEFORE UPDATE ON activity_log
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'activity_log is append-only';
END;

DROP TRIGGER IF EXISTS activity_log_prevent_delete;
CREATE TRIGGER activity_log_prevent_delete
BEFORE DELETE ON activity_log
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'activity_log is append-only';
END;
