package todo;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 마이페이지용 루틴 통계 DAO
 */
public class RoutineDao {

    public int routineTotalThisMonth;    // 이번 달 루틴 발생 수
    public int routineDoneThisMonth;     // 이번 달 루틴 완료 수
    public int routineSkippedThisMonth;  // 이번 달 루틴 스킵 수

    // 완료율
    public double getRoutineDoneRateThisMonth() {
        if (routineTotalThisMonth == 0) return 0.0;
        return (routineDoneThisMonth * 100.0) / routineTotalThisMonth;
    }

    // userId 기준으로 루틴 통계 불러오기
    public static RoutineDao load(long userId) {
        RoutineDao dao = new RoutineDao();
        fillRoutineStatsThisMonth(userId, dao);
        return dao;
    }

    // routine_occurrences + routines 를 이용한 이번 달 통계
    private static void fillRoutineStatsThisMonth(long userId, RoutineDao dao) {
        String sql =
                "SELECT " +
                "  COUNT(*) AS total_cnt, " +
                "  SUM(CASE WHEN ro.status = 'DONE' THEN 1 ELSE 0 END) AS done_cnt, " +
                "  SUM(CASE WHEN ro.status = 'SKIPPED' THEN 1 ELSE 0 END) AS skipped_cnt " +
                "FROM routine_occurrences ro " +
                "JOIN routines r ON ro.routine_id = r.id " +
                "WHERE r.user_id = ? " +
                "  AND YEAR(ro.occ_date) = YEAR(CURDATE()) " +
                "  AND MONTH(ro.occ_date) = MONTH(CURDATE())";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dao.routineTotalThisMonth = rs.getInt("total_cnt");
                    dao.routineDoneThisMonth = rs.getInt("done_cnt");
                    dao.routineSkippedThisMonth = rs.getInt("skipped_cnt");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
