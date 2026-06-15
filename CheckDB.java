import java.sql.*;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/payroll_management?autoReconnect=true&useSSL=false";
        String user = "root";
        String pass = "innobliss123$";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("tenant_company_mapping:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM tenant_company_mapping WHERE tenant_id = 1 LIMIT 5")) {
                while (rs.next()) {
                    System.out.println("id: " + rs.getLong("id") + ", app_name: " + rs.getString("application_name") + ", tenant_id: " + rs.getLong("tenant_id"));
                }
            }
            
            System.out.println("\nstore_details:");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM store_details WHERE store_id = 1 LIMIT 5")) {
                while (rs.next()) {
                    System.out.println("id: " + rs.getLong("id") + ", store_id: " + rs.getLong("store_id") + ", tenant_map_id: " + rs.getLong("tenant_company_mapping_id"));
                }
            }
        }
    }
}
