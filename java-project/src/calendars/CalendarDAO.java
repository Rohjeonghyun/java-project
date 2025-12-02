package calendars;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Vector;

import database.DBConnection;

public class CalendarDAO {

    // [수정] 고정된 상수 제거 -> 멤버 변수로 변경
    private long userId;

    // [수정] 생성자에서 userId를 받아서 저장
    public CalendarDAO(long userId) {
        this.userId = userId;
    }

    /**
     * DB에서 사용자의 카테고리 목록을 불러옵니다.
     */
    public Vector<CategoryItem> getCategories() {
        Vector<CategoryItem> list = new Vector<>();
        String sql = "SELECT name, color_hex FROM categories WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId); // [수정] this.userId 사용
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String hexColor = rs.getString("color_hex");
                    Color color = (hexColor != null) ? Color.decode(hexColor) : Color.LIGHT_GRAY;
                    list.add(new CategoryItem(name, color));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addCategory(String name, Color color) {
        String sql = "INSERT INTO categories (user_id, name, color_hex) VALUES (?, ?, ?)";
        String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId); // [수정]
            pstmt.setString(2, name);
            pstmt.setString(3, hexColor);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategory(String name) {
        String sql = "DELETE FROM categories WHERE user_id = ? AND name = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId); // [수정]
            pstmt.setString(2, name);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateCategoryColor(String name, Color newColor) {
        String sql = "UPDATE categories SET color_hex = ? WHERE user_id = ? AND name = ?";
        String hexColor = String.format("#%02x%02x%02x", newColor.getRed(), newColor.getGreen(), newColor.getBlue());

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setString(1, hexColor);
            pstmt.setLong(2, userId); // [수정]
            pstmt.setString(3, name);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 일정 관련 ---

    public boolean addSchedule(ScheduleItem item) {
        String sql = "INSERT INTO todos (user_id, title, due_date, start_time, end_time, category_id, group_id, status) " +
                     "VALUES (?, ?, ?, ?, ?, (SELECT id FROM categories WHERE name = ? AND user_id = ?), ?, 'PENDING')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId); // [수정]
            pstmt.setString(2, item.getTitle());
            pstmt.setString(3, item.getDate()); 
            pstmt.setTime(4, convertStringToTime(item.getStartTime()));
            pstmt.setTime(5, convertStringToTime(item.getEndTime()));
            pstmt.setString(6, item.getCategory());
            pstmt.setLong(7, userId); // [수정] 서브쿼리용
            pstmt.setLong(8, item.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Vector<ScheduleItem> getSchedulesByMonth(int year, int month) {
        Vector<ScheduleItem> list = new Vector<>();
        String sql = "SELECT t.*, c.name AS cat_name, c.color_hex " +
                     "FROM todos t LEFT JOIN categories c ON t.category_id = c.id " +
                     "WHERE t.user_id = ? AND YEAR(t.due_date) = ? AND MONTH(t.due_date) = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId); // [수정]
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long groupId = rs.getLong("group_id");
                    String title = rs.getString("title");
                    String date = rs.getString("due_date");
                    String startTime = convertTimeToString(rs.getTime("start_time"));
                    String endTime = convertTimeToString(rs.getTime("end_time"));
                    
                    String catName = rs.getString("cat_name");
                    if (catName == null) catName = "미지정";
                    
                    String colorHex = rs.getString("color_hex");
                    Color color = (colorHex != null) ? Color.decode(colorHex) : Color.LIGHT_GRAY;
                    
                    list.add(new ScheduleItem(groupId, title, catName, color, date, startTime, endTime));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteScheduleByGroupId(long groupId) {
        String sql = "DELETE FROM todos WHERE group_id = ?"; 
        // group_id는 유니크하므로 user_id 검사 생략 가능하지만, 안전하게 하려면 추가해도 됨

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setLong(1, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 유틸리티 ---
    private Time convertStringToTime(String timeStr) {
        try {
            if (timeStr == null || timeStr.isEmpty()) return null;
            String[] parts = timeStr.replace("시", "").replace("분", "").split(" ");
            int hour = Integer.parseInt(parts[0].trim());
            int min = 0;
            if (parts.length > 1) min = Integer.parseInt(parts[1].trim());
            String timeFormat = String.format("%02d:%02d:00", hour, min);
            return Time.valueOf(timeFormat);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String convertTimeToString(Time time) {
        if (time == null) return "";
        String t = time.toString(); 
        String[] parts = t.split(":"); 
        return parts[0] + "시 " + parts[1] + "분";
    }
}