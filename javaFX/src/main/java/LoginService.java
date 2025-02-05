import java.sql.*;

public class LoginService {
    public static boolean authenticate(String email, String password) {
        String sql = "SELECT password FROM IT_consultant WHERE email = ?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return password.equals(storedPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addUser(String email, String password) {
        String sql = "INSERT INTO IT_consultant (email, password) VALUES (?, ?)";
        try (Connection conn = DBUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
