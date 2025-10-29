import com.sgu.qlhs.DatabaseConnection;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("==== KIỂM TRA KẾT NỐI DATABASE ====");
        Connection conn = DatabaseConnection.getConnection();

        if (conn != null) {
            System.out.println("✓ Kết nối thành công!");
            DatabaseConnection.closeConnection(conn);
        } else {
            System.out.println("✗ Kết nối thất bại!");
        }
    }
}
