package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // MySQL 연결 정보
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String URL = "jdbc:mysql://localhost:3306/java_project?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    
    private static final String USER = "root"; 
    private static final String PASS = "jh10%@jh00"; 

    // [FIX] 예외를 내부에서 삼키지(try-catch) 않고, 호출한 곳으로 던지(throws)도록 수정
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            // 드라이버가 없으면 실행이 불가능하므로 예외 발생
            e.printStackTrace();
            throw new SQLException("JDBC 드라이버를 찾을 수 없습니다.", e);
        }
        // 연결 실패 시 SQLException이 자동으로 던져짐 -> 호출한 DAO의 catch 블록으로 이동
        return DriverManager.getConnection(URL, USER, PASS);
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