package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // MySQL 연결 정보 (본인의 설정에 맞게 수정 필요)
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/java_project?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root"; // 본인의 MySQL 아이디
    private static final String PASS = "jh10%@jh00"; // 본인의 MySQL 비밀번호

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName(DRIVER);
            

            con = DriverManager.getConnection(URL, USER, PASS);
            
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 드라이버를 찾을 수 없습니다.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("DB 연결에 실패했습니다.");
            e.printStackTrace();
        }
        return con;
    }
    
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}