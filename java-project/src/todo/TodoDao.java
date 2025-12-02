package todo;

import database.DBConnection;
import java.sql.*;
import java.util.Vector;

public class TodoDao {
    
    // --- [마이페이지 통계용 필드] ---
    public int totalTodos;
    public int doneTodos;
    public int pendingTodos;
    public int todayTodos;
    public int todayDoneTodos;
    public int diaryThisMonth;

    public double getTotalTodoDoneRate() {
        if (totalTodos == 0) return 0.0;
        return (doneTodos * 100.0) / totalTodos;
    }
    public double getTodayTodoDoneRate() {
        if (todayTodos == 0) return 0.0;
        return (todayDoneTodos * 100.0) / todayTodos;
    }

    // --- [기능 1: 마이페이지 통계 로드] ---
    public static TodoDao load(long userId) {
        TodoDao dao = new TodoDao();
        fillTodoOverallStats(userId, dao);
        fillTodayTodoStats(userId, dao);
        fillDiaryStatsThisMonth(userId, dao);
        return dao;
    }

    private static void fillTodoOverallStats(long userId, TodoDao dao) {
        String sql = "SELECT COUNT(*) AS total, SUM(CASE WHEN status='DONE' THEN 1 ELSE 0 END) AS done FROM todos WHERE user_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dao.totalTodos = rs.getInt("total");
                    dao.doneTodos = rs.getInt("done");
                    dao.pendingTodos = dao.totalTodos - dao.doneTodos;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void fillTodayTodoStats(long userId, TodoDao dao) {
        String sql = "SELECT COUNT(*) AS total, SUM(CASE WHEN status='DONE' THEN 1 ELSE 0 END) AS done FROM todos WHERE user_id=? AND due_date=CURDATE()";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dao.todayTodos = rs.getInt("total");
                    dao.todayDoneTodos = rs.getInt("done");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void fillDiaryStatsThisMonth(long userId, TodoDao dao) {
        String sql = "SELECT COUNT(*) FROM diary_entries WHERE user_id=? AND YEAR(entry_date)=YEAR(CURDATE()) AND MONTH(entry_date)=MONTH(CURDATE())";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) dao.diaryThisMonth = rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- [기능 2: TodoPanel용 CRUD (새로 추가됨)] ---
    
    // 날짜별 할 일 목록 조회
    public Vector<TodoItem> getTodosByDate(long userId, String dateStr) {
        Vector<TodoItem> list = new Vector<>();
        String sql = "SELECT t.*, c.name as category_name FROM todos t LEFT JOIN categories c ON t.category_id = c.id WHERE t.user_id = ? AND t.due_date = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long groupId = rs.getLong("group_id");
                    String title = rs.getString("title");
                    String startTime = convertTimeToString(rs.getTime("start_time"));
                    String endTime = convertTimeToString(rs.getTime("end_time"));
                    boolean isDone = "DONE".equalsIgnoreCase(rs.getString("status"));
                    String category = rs.getString("category_name");
                    if(category == null) category = "미지정";
                    
                    list.add(new TodoItem(groupId, title, isDone, false, startTime, endTime, category));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 할 일 상태 변경 (체크/해제)
    public boolean updateTodoStatus(long userId, long groupId, boolean done) {
        String sql = "UPDATE todos SET status = ? WHERE user_id = ? AND group_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, done ? "DONE" : "PENDING");
            ps.setLong(2, userId);
            ps.setLong(3, groupId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    // DB 완료 여부 확인
    public boolean isTodoDone(long userId, long groupId) {
        String sql = "SELECT status FROM todos WHERE user_id = ? AND group_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return "DONE".equalsIgnoreCase(rs.getString("status"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // 시간 변환 유틸리티
    private String convertTimeToString(Time time) {
        if (time == null) return "";
        String t = time.toString(); 
        String[] parts = t.split(":"); 
        return parts[0] + "시 " + parts[1] + "분";
    }
}