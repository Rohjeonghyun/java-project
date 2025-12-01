package todo;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 마이페이지용 Todo + 일기 통계 DAO
 * - DB에서 통계를 조회해서 필드에 담아두는 역할
 */
public class TodoDao {

    
    public int totalTodos;          // 전체 Todo 개수
    public int doneTodos;           // 전체 완료 Todo 개수
    public int pendingTodos;        // 전체 미완료 Todo 개수

    public int todayTodos;          // 오늘 Todo 개수
    public int todayDoneTodos;      // 오늘 완료 Todo 개수

    public int diaryThisMonth;      // 이번 달 일기 쓴 날 수

    // 편의 메서드 (완료율 계산) 
    public double getTotalTodoDoneRate() {
        if (totalTodos == 0) return 0.0;
        return (doneTodos * 100.0) / totalTodos;
    }

    public double getTodayTodoDoneRate() {
        if (todayTodos == 0) return 0.0;
        return (todayDoneTodos * 100.0) / todayTodos;
    }

    //  정적 팩토리: userId 기준으로 통계를 한 번에 불러오기
    public static TodoDao load(long userId) {
        TodoDao dao = new TodoDao();

        fillTodoOverallStats(userId, dao);
        fillTodayTodoStats(userId, dao);
        fillDiaryStatsThisMonth(userId, dao);

        return dao;
    }

    // 1) 전체 Todo / 완료 / 미완료
    private static void fillTodoOverallStats(long userId, TodoDao dao) {
        String sql =
                "SELECT " +
                "  COUNT(*) AS total_cnt, " +
                "  SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done_cnt, " +
                "  SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending_cnt " +
                "FROM todos " +
                "WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dao.totalTodos = rs.getInt("total_cnt");
                    dao.doneTodos = rs.getInt("done_cnt");
                    dao.pendingTodos = rs.getInt("pending_cnt");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2) 오늘 Todo / 오늘 완료
    private static void fillTodayTodoStats(long userId, TodoDao dao) {
        String sql =
                "SELECT " +
                "  COUNT(*) AS total_cnt, " +
                "  SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) AS done_cnt " +
                "FROM todos " +
                "WHERE user_id = ? " +
                "  AND due_date = CURDATE()";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dao.todayTodos = rs.getInt("total_cnt");
                    dao.todayDoneTodos = rs.getInt("done_cnt");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    // 3) 이번 달 일기 쓴 날짜 수
    private static void fillDiaryStatsThisMonth(long userId, TodoDao dao) {
        String sql =
                "SELECT COUNT(*) AS diary_cnt " +
                "FROM diary_entries " +
                "WHERE user_id = ? " +
                "  AND YEAR(entry_date) = YEAR(CURDATE()) " +
                "  AND MONTH(entry_date) = MONTH(CURDATE())";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dao.diaryThisMonth = rs.getInt("diary_cnt");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
