import java.sql.*;

public class InsertData {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/payroll_management?autoReconnect=true&useSSL=false";
        String user = "root";
        String pass = "innobliss123$";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Creating Tenant 2 Admin Role...");
            String allPermissions = "VIEW_STAFF,VIEW_SHIFTS,VIEW_ATTENDANCE,VIEW_SALARY,MANAGE_SALARY,MANAGE_ATTENDANCE,MANAGE_SHIFTS,MANAGE_STAFF,VIEW_OTHER_STAFF,VIEW_LEAVES,MANAGE_LEAVES,MANAGE_ROLES,VIEW_REPORTS,MANAGE_REPORTS,MANAGE_HOLIDAYS,VIEW_HOLIDAYS,APPROVE_LEAVES,APPROVE_ATTENDANCE";
            
            long t2AdminRoleId = -1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id FROM s_role WHERE name = 'Admin' AND tenant_id = 2 LIMIT 1")) {
                if (rs.next()) {
                    t2AdminRoleId = rs.getLong(1);
                    stmt.executeUpdate("UPDATE s_role SET permissions = '" + allPermissions + "' WHERE id = " + t2AdminRoleId);
                } else {
                    stmt.executeUpdate("INSERT INTO s_role (name, permissions, active, tenant_id) VALUES ('Admin', '" + allPermissions + "', 1, 2)", Statement.RETURN_GENERATED_KEYS);
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) t2AdminRoleId = keys.getLong(1);
                    }
                }
            }
            
            System.out.println("Tenant 2 Admin Role ID: " + t2AdminRoleId);
            
            // Re-assign admin_t2 to this role for both stores
            try (Statement stmt = conn.createStatement()) {
                long staffId = -1;
                try (ResultSet rs = stmt.executeQuery("SELECT id FROM personnel_details WHERE username = 'admin_t2' LIMIT 1")) {
                    if (rs.next()) staffId = rs.getLong(1);
                }
                
                if (staffId != -1) {
                    stmt.executeUpdate("UPDATE s_store_staff_role SET role_id = " + t2AdminRoleId + " WHERE staff_id = " + staffId + " AND tenant_id = 2");
                    System.out.println("Updated staff role assignment to new tenant-specific role.");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
