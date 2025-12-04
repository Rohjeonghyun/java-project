package Mypage;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import database.DBConnection;

/**
 * My Stats 화면
 * 콤보박스 통계 항목 선택 (todo/루틴/카테고리/etc)
 * < > 버튼 사용자 원하는 월 이동
 * 해당 월 + 항목에 맞는 데이터를 DB에서 읽어 파이차트로 표시
 */
public class StatsFrame extends JFrame {

    // 색상
    private static final Color BG_OUTER     = new Color(236, 240, 245);
    private static final Color BG_CARD      = Color.WHITE;
    private static final Color BG_BAR       = new Color(245, 245, 245);
    private static final Color BORDER_CARD  = new Color(205, 210, 220);
    private static final Color BORDER_GRP   = new Color(210, 215, 225);
    private static final Color TEXT_MAIN    = new Color(150, 150, 150);
    private static final Color TEXT_SUB     = new Color(110, 110, 110);
    private static final Color PROFILE_BLUE = new Color(70, 120, 210);

    private long userId;               
    private YearMonth currentMonth;    

    private JLabel lblMonth;            
    private JComboBox<String> cbType;    
    private PieChartPanel chartPanel;   

    public StatsFrame(Component owner) {
        this(owner, 1L);//test기준
    }

    public StatsFrame(Component owner, long userId) {
        this.userId = userId;
        this.currentMonth = YearMonth.now();

        setTitle("MyStats");
        setSize(600, 600);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //메인
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("My Stats"));
        panel.setBackground(BG_OUTER);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_BAR);

        //월 이동
        JPanel monthNav = new JPanel(new FlowLayout(FlowLayout.LEFT));
        monthNav.setBackground(BG_BAR);
        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");

        lblMonth = new JLabel();
        lblMonth.setForeground(TEXT_SUB);
        updateMonthLabel();

        monthNav.add(btnPrev);
        monthNav.add(lblMonth);
        monthNav.add(btnNext);

        top.add(monthNav, BorderLayout.WEST);

        //통계 항목
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        typePanel.setBackground(BG_BAR);
        cbType = new JComboBox<>(new String[] { "통계 항목", "할 일 개수", "루틴 체크", "할 일 체크", "카테고리","일기" });
        typePanel.add(cbType);

        top.add(typePanel, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);

        //차트
        chartPanel = new PieChartPanel();
        chartPanel.setBackground(BG_CARD);
        chartPanel.setBorder(BorderFactory.createLineBorder(BORDER_CARD));
        panel.add(chartPanel, BorderLayout.CENTER);

        //버튼
        JPanel bottom = new JPanel();
        bottom.setBackground(BG_BAR);
        JButton btnClose = new JButton("닫기");
        btnClose.addActionListener(e -> dispose());
        bottom.add(btnClose);
        panel.add(bottom, BorderLayout.SOUTH);

        add(panel);

        //이벤트연결
        btnPrev.addActionListener(e -> { //달 이동(-)
            currentMonth = currentMonth.minusMonths(1);
            updateMonthLabel();
            reloadChartData();
        });

        btnNext.addActionListener(e -> {//달 이동(+)
            currentMonth = currentMonth.plusMonths(1);
            updateMonthLabel();
            reloadChartData();
        });
        
        // 항목(콤보박스) 데이터 변화
        cbType.addActionListener(e -> reloadChartData());

        chartPanel.setMessage("상단에서 통계 항목을 선택하세요.");
    }

    //라벨(상단)현재 달 표시
    private void updateMonthLabel() {
        lblMonth.setText(currentMonth.getYear() + "-" +
                String.format("%02d", currentMonth.getMonthValue()));
    }

    //월 변경(콤보박스)
    private void reloadChartData() {
        String selected = (String) cbType.getSelectedItem();
        if (selected == null || "통계 항목".equals(selected)) {
            chartPanel.setData(null, null);
            chartPanel.setMessage("상단에서 통계 항목을 선택하세요.");
            return;
        }

        //달의 첫날, 막날 계산
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        try {
            if ("할 일 개수".equals(selected)) {
                loadTodoStats(startDate, endDate);
            } else if ("루틴 체크".equals(selected)) {
                loadRoutineStats(startDate, endDate);
            } else if ("할 일 체크".equals(selected)) {
                loadTodoCheckStats(startDate, endDate);
            } else if ("카테고리".equals(selected)) {
                loadCategoryStats(startDate, endDate);
            } else if ("일기".equals(selected)) {
                loadDiaryStats(startDate, endDate);
            } else {
                chartPanel.setData(null, null);
                chartPanel.setMessage("지원하지 않는 통계 항목입니다.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            chartPanel.setData(null, null);
            chartPanel.setMessage("통계 로딩 중 오류: " + ex.getMessage());
        }
    }

    //통계쿼리
    
 // [FIX] 할 일(Todo) + 루틴(Routine) 합산 통계 (완료거나 미완료 개수 표시)
    private void loadTodoStats(LocalDate start, LocalDate end) throws Exception {
        int totalDone = 0;
        int totalPending = 0; // 미완료 (Pending, Skipped 등 포함)

        // 할 일(Todo) 통계 조회
        String sqlTodo = "SELECT status, COUNT(*) AS cnt FROM todos " +
                         "WHERE user_id = ? AND due_date BETWEEN ? AND ? GROUP BY status";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlTodo)) {
            
            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int cnt = rs.getInt("cnt");
                    
                    if ("DONE".equalsIgnoreCase(status)) {
                        totalDone += cnt;
                    } else {
                        // PENDING, CANCELED 등은 미완료로 간주하도록 함
                        totalPending += cnt;
                    }
                }
            }
        }

        // 루틴(Routine) 통계 조회 (routine_occurrences 테이블)
        String sqlRoutine = "SELECT ro.status, COUNT(*) AS cnt " +
                            "FROM routine_occurrences ro " +
                            "JOIN routines r ON ro.routine_id = r.id " +
                            "WHERE r.user_id = ? AND ro.occ_date BETWEEN ? AND ? " +
                            "GROUP BY ro.status";
                            
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlRoutine)) {
             
            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int cnt = rs.getInt("cnt");
                    
                    if ("DONE".equalsIgnoreCase(status)) {
                        totalDone += cnt;
                    } else {
                        // SKIPPED, PENDING 등은 미완료로 간주하도록 함
                        totalPending += cnt;
                    }
                }
            }
        }

        // 차트에 데이터 적용 부분
        if (totalDone == 0 && totalPending == 0) {
            chartPanel.setData(null, null);
            chartPanel.setMessage("해당 월에 데이터가 없습니다.");
        } else {
            String[] labels = { "완료", "미완료" };
            int[] values = { totalDone, totalPending };
            chartPanel.setData(labels, values);
            chartPanel.setMessage(null);
        }
    }

 // [FIX] 루틴체크 통계 (완료 / 건너뜀 / 미실행 계산 로직 개선)
    private void loadRoutineStats(LocalDate start, LocalDate end) throws Exception {
        // 선택한 달의 총 날짜 수 계산 (예: 30일, 31일)
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;

        // 현재 등록된 루틴 개수 조회 (ex: 매일 하는 루틴이 3개라면)
        int routineCount = 0;
        String sqlCount = "SELECT COUNT(*) FROM routines WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlCount)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) routineCount = rs.getInt(1);
            }
        }

        // '예상되는' 총 루틴 실행 횟수 (= 루틴 개수 * 날짜 수 로 계산함)
        long totalExpected = routineCount * daysBetween;

        // 실제 기록된(완료/스킵) 횟수 DB 조회 쿼리문
        String sql = "SELECT o.status, COUNT(*) AS cnt " +
                     "FROM routine_occurrences o " +
                     "JOIN routines r ON o.routine_id = r.id " +
                     "WHERE r.user_id = ? AND o.occ_date BETWEEN ? AND ? " +
                     "GROUP BY o.status";

        int doneCnt = 0;
        int skipCnt = 0;
        int recordedTotal = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int cnt = rs.getInt("cnt");
                    
                    // DB에는 "SKIP"으로 저장됨. (Enum 이름과 일치시킴)
                    if ("DONE".equalsIgnoreCase(status)) {
                        doneCnt = cnt;
                    } else if ("SKIP".equalsIgnoreCase(status) || "SKIPPED".equalsIgnoreCase(status)) {
                        skipCnt = cnt;
                    }
                    recordedTotal += cnt;
                }
            }
        }

        // '미실행' 횟수 계산 (전체 해야 할 횟수 - 기록된 횟수로)
        long pendingCnt = totalExpected - recordedTotal;
        if (pendingCnt < 0) pendingCnt = 0; // 음수 방지로 필요함

        // 차트에 데이터 적용
        if (totalExpected == 0) {
            chartPanel.setData(null, null);
            chartPanel.setMessage("등록된 루틴이 없습니다.");
        } else {
            String[] labels = { "완료", "건너뜀", "미실행" };
            int[] values = { doneCnt, skipCnt, (int)pendingCnt };
            
            chartPanel.setData(labels, values);
            chartPanel.setMessage(null);
        }
    }
    
    //[FIX] 카테고리별 todo 개수 비율
    private void loadCategoryStats(LocalDate start, LocalDate end) throws Exception {
        String sql =
                "SELECT COALESCE(c.name, '(미지정)') AS cat_name, COUNT(*) AS cnt " +
                "FROM todos t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? AND t.due_date BETWEEN ? AND ? " +
                "GROUP BY COALESCE(c.name, '(미지정)')";

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("cat_name");
                    int cnt = rs.getInt("cnt");
                    labels.add(name);
                    values.add(cnt);
                }
            }
        }

        if (labels.isEmpty()) {
            chartPanel.setData(null, null);
            chartPanel.setMessage("해당 월에 카테고리별 할 일 데이터가 없습니다.");
        } else {
            chartPanel.setData(
                    labels.toArray(new String[0]),
                    values.stream().mapToInt(i -> i).toArray()
            );
            chartPanel.setMessage(null);
        }
    }

    // [FIX} todo체크 통계(DONE/NOT_DONE)
    private void loadTodoCheckStats(LocalDate start, LocalDate end) throws Exception {
        String sql =
                "SELECT " +
                "  CASE WHEN status = 'DONE' THEN 'DONE' ELSE 'NOT_DONE' END AS done_flag, " +
                "  COUNT(*) AS cnt " +
                "FROM todos " +
                "WHERE user_id = ? AND due_date BETWEEN ? AND ? " +
                "GROUP BY done_flag";

        int doneCount = 0;
        int notDoneCount = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String flag = rs.getString("done_flag");
                    int cnt = rs.getInt("cnt");
                    if ("DONE".equals(flag)) {
                        doneCount = cnt;      // 체크함
                    } else {
                        notDoneCount = cnt;   // 안 함 (PENDING + CANCELED)
                    }
                }
            }
        }

        if (doneCount == 0 && notDoneCount == 0) {
            chartPanel.setData(null, null);
            chartPanel.setMessage("해당 월에 할 일 데이터가 없습니다.");
        } else {
            String[] labels = { "체크함", "안 함" };
            int[] values = { doneCount, notDoneCount };
            chartPanel.setData(labels, values);
            chartPanel.setMessage(null);
        }
    }

    // 일기통계(해당 달 일기 작성 한 날/안 쓴 날)
    private void loadDiaryStats(LocalDate start, LocalDate end) throws Exception {
        String sql =
                "SELECT COUNT(DISTINCT entry_date) AS cnt " +
                "FROM diary_entries " +
                "WHERE user_id = ? AND entry_date BETWEEN ? AND ?";

        int writtenDays = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    writtenDays = rs.getInt("cnt");
                }
            }
        }

        int totalDays = start.lengthOfMonth();
        int notWrittenDays = totalDays - writtenDays;
        if (notWrittenDays < 0) notWrittenDays = 0;

        if (writtenDays == 0 && notWrittenDays == 0) {
            chartPanel.setData(null, null);
            chartPanel.setMessage("해당 월에 일기 데이터가 없습니다.");
        } else {
            String[] labels = { "작성한 날", "안 쓴 날" };
            int[] values = { writtenDays, notWrittenDays };
            chartPanel.setData(labels, values);
            chartPanel.setMessage(null);
        }
    }

    //파이차트

    private static class PieChartPanel extends JPanel {
        private String[] labels;
        private int[] values;
        private String message;

        // 색상
        private final Color[] colors = {
                PROFILE_BLUE,
                new Color(90, 140, 220),
                new Color(110, 155, 225),
                new Color(130, 170, 230),
                new Color(150, 185, 235),
                new Color(170, 200, 240),
                new Color(190, 210, 245),
                new Color(210, 220, 250)
        };

        public void setData(String[] labels, int[] values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }

        public void setMessage(String msg) {
            this.message = msg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (labels == null || values == null || values.length == 0) {
                // 데이터 없을 때 메시지 표시
                if (message != null) {
                    g2.setColor(TEXT_SUB);
                    FontMetrics fm = g2.getFontMetrics();
                    int textW = fm.stringWidth(message);
                    int x = (w - textW) / 2;
                    int y = h / 2;
                    g2.drawString(message, x, y);
                }
                return;
            }

            // 전체 합
            int total = 0;
            for (int v : values) total += v;
            if (total == 0) {
                g2.setColor(TEXT_SUB);
                String msg = "데이터 합계가 0입니다.";
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(msg);
                int x = (w - textW) / 2;
                int y = h / 2;
                g2.drawString(msg, x, y);
                return;
            }

            int size = Math.min(w, h) - 150; //좌측상단여백
            if (size < 50) size = 50;
            int x = (w - size) / 2 - 80;
            int y = (h - size) / 2;

            int startAngle = 0;

            //파이조각
            for (int i = 0; i < values.length; i++) {
                float ratio = (float) values[i] / (float) total;
                int angle = Math.round(ratio * 360f);

                g2.setColor(colors[i % colors.length]);
                g2.fillArc(x, y, size, size, startAngle, angle);

                startAngle += angle;
            }

            g2.setColor(BORDER_CARD);
            g2.drawOval(x, y, size, size);

            //범례
            int legendX = x + size + 30;
            int legendY = y + 20;

            g2.setFont(g2.getFont().deriveFont(12f));

            for (int i = 0; i < labels.length; i++) {
                g2.setColor(colors[i % colors.length]);
                g2.fillRect(legendX, legendY + (i * 20), 12, 12);

                g2.setColor(TEXT_MAIN);
                String text = labels[i] + " (" + values[i] + ")";
                g2.drawString(text, legendX + 20, legendY + 10 + (i * 20));
            }
        }
    }
}
