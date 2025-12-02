package todo;

import database.DBConnection;
import java.sql.*;
import java.util.Vector;

public class RoutineDao {

    // --- [마이페이지 통계용] ---
    public int routineTotalThisMonth;
    public int routineDoneThisMonth;
    public int routineSkippedThisMonth;

    public double getRoutineDoneRateThisMonth() {
        if (routineTotalThisMonth == 0) return 0.0;
        return (routineDoneThisMonth * 100.0) / routineTotalThisMonth;
    }

    public static RoutineDao load(long userId) {
        RoutineDao dao = new RoutineDao();
        String sql = "SELECT COUNT(*) AS total, SUM(CASE WHEN status='DONE' THEN 1 ELSE 0 END) AS done, SUM(CASE WHEN status='SKIPPED' THEN 1 ELSE 0 END) AS skipped " +
                     "FROM routine_occurrences ro JOIN routines r ON ro.routine_id = r.id " +
                     "WHERE r.user_id = ? AND YEAR(ro.occ_date)=YEAR(CURDATE()) AND MONTH(ro.occ_date)=MONTH(CURDATE())";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dao.routineTotalThisMonth = rs.getInt("total");
                    dao.routineDoneThisMonth = rs.getInt("done");
                    dao.routineSkippedThisMonth = rs.getInt("skipped");
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dao;
    }

    // --- [기능 2: TodoPanel용 CRUD (새로 추가됨)] ---

    // 루틴 목록 조회
    public Vector<RoutineItem> getRoutines(long userId) {
        Vector<RoutineItem> list = new Vector<>();
        String sql = "SELECT id, title, start_time, state FROM routines WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String title = rs.getString("title");
                    String startTime = rs.getString("start_time");
                    // DB state 문자열 -> Enum 변환
                    RoutineState state = RoutineState.TODO;
                    String s = rs.getString("state");
                    if ("DONE".equalsIgnoreCase(s)) state = RoutineState.DONE;
                    else if ("SKIP".equalsIgnoreCase(s)) state = RoutineState.SKIP;
                    
                    list.add(new RoutineItem(id, title, state, startTime));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 루틴 추가
    public long addRoutine(long userId, String title, String startTime) {
        String sql = "INSERT INTO routines (user_id, title, repeat_rule, start_time, state) VALUES (?, ?, 'none', ?, 'TODO')";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(3, startTime);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0L;
    }

    // 루틴 삭제
    public boolean deleteRoutine(long userId, String title) {
        String sql = "DELETE FROM routines WHERE user_id = ? AND title = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, title);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 루틴 상태 변경
    public boolean updateRoutineState(long userId, RoutineItem item) {
        String sql = "UPDATE routines SET state = ? WHERE id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, item.state.name());
            ps.setLong(2, item.id);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}