import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class HashGen {
    public static void main(String[] args) throws Exception {
        String hash = BCrypt.hashpw("admin123", BCrypt.gensalt(10));
        System.out.println("Generated Hash: " + hash);
        
        String url = "jdbc:mysql://localhost:3306/payroll_management?autoReconnect=true&useSSL=false";
        String user = "root";
        String pass = "innobliss123$";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE personnel_details SET password = '" + hash + "' WHERE username = 'admin_t2'");
                System.out.println("Updated password for admin_t2");
            }
        }
    }
}
