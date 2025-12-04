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
 // [FIX] 루틴 상태 변경 시 이력 테이블(routine_occurrences)도 함께 관리하도록 수정함
    public boolean updateRoutineState(long userId, RoutineItem item) {
        // 1. routines 테이블 업데이트
        String updateRoutineSql = "UPDATE routines SET state = ? WHERE id = ? AND user_id = ?";
        
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); // 트랜잭션 시작
            
            try {
                // routines 테이블 상태 업데이트
                try (PreparedStatement ps = con.prepareStatement(updateRoutineSql)) {
                    ps.setString(1, item.state.name());
                    ps.setLong(2, item.id);
                    ps.setLong(3, userId);
                    ps.executeUpdate();
                }

                // outine_occurrences 테이블 이력 관리 (통계용이라 rutine탭에 직접적인 영향 없음 걱정 ㄴㄴ)
                // 오늘 날짜에 해당 루틴 기록이 있는지 확인
                String checkSql = "SELECT count(*) FROM routine_occurrences WHERE routine_id = ? AND occ_date = CURDATE()";
                boolean exists = false;
                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setLong(1, item.id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            exists = rs.getInt(1) > 0;
                        }
                    }
                }

                if (item.state == RoutineState.TODO) {
                    // 체크 해제(TODO) 시: 오늘자 기록이 있다면 삭제 (통계에서 빠짐)
                    if (exists) {
                        String deleteSql = "DELETE FROM routine_occurrences WHERE routine_id = ? AND occ_date = CURDATE()";
                        try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                            ps.setLong(1, item.id);
                            ps.executeUpdate();
                        }
                    }
                } else {
                    // 완료(DONE) 또는 스킵(SKIP) 시: 
                    if (exists) {
                        // 이미 기록이 있으면 상태 업데이트
                        String updateOccSql = "UPDATE routine_occurrences SET status = ? WHERE routine_id = ? AND occ_date = CURDATE()";
                        try (PreparedStatement ps = con.prepareStatement(updateOccSql)) {
                            ps.setString(1, item.state.name());
                            ps.setLong(2, item.id);
                            ps.executeUpdate();
                        }
                    } else {
                        // 기록이 없으면 새로 추가
                        String insertOccSql = "INSERT INTO routine_occurrences (routine_id, occ_date, status) VALUES (?, CURDATE(), ?)";
                        try (PreparedStatement ps = con.prepareStatement(insertOccSql)) {
                            ps.setLong(1, item.id);
                            ps.setString(2, item.state.name());
                            ps.executeUpdate();
                        }
                    }
                }
                con.commit(); // 모든 작업 성공 시 커밋
                return true;

            } catch (SQLException ex) {
                con.rollback(); // 에러 발생 시 롤백
                ex.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateRoutineTime(long userId, long routineId, String newStartTime) {
        String sql = "UPDATE routines SET start_time = ? WHERE id = ? AND user_id = ?";
        
        
        try (Connection con = DBConnection.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, newStartTime);
            ps.setLong(2, routineId);
            ps.setLong(3, userId);
            
            return ps.executeUpdate() > 0; // 업데이트 성공 시 true 반환
        } catch (SQLException e) { 
            e.printStackTrace(); 
            return false; 
        }
    }
}