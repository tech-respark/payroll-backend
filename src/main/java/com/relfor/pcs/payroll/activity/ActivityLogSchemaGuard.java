package com.relfor.pcs.payroll.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Ensures append-only triggers exist after Hibernate creates {@code activity_log}.
 */
@Component
@Order(200)
public class ActivityLogSchemaGuard implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogSchemaGuard.class);

    private final DataSource dataSource;

    public ActivityLogSchemaGuard(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection)) {
                log.warn("activity_log table is not present yet; skip immutability triggers");
                return;
            }
            ensureTrigger(connection, "activity_log_prevent_update", "UPDATE");
            ensureTrigger(connection, "activity_log_prevent_delete", "DELETE");
        } catch (Exception ex) {
            throw new IllegalStateException("Could not install activity_log immutability triggers: " + ex.getMessage(), ex);
        }
    }

    private boolean tableExists(Connection connection) throws Exception {
        try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, "activity_log", null)) {
            return tables.next();
        }
    }

    private void ensureTrigger(Connection connection, String name, String event) throws Exception {
        if (triggerExists(connection, name)) {
            return;
        }
        String sql = "CREATE TRIGGER " + name + " BEFORE " + event + " ON activity_log FOR EACH ROW "
                + "BEGIN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'activity_log is append-only'; END";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            log.info("Installed {} on activity_log", name);
        }
    }

    private boolean triggerExists(Connection connection, String name) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA = DATABASE() AND TRIGGER_NAME = ?")) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
