package todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import calendars.CalendarPanel;
import calendars.ScheduleItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 누락수정
public class TodoPanel extends JPanel {

    private static final long USER_ID = 1L;   // CalendarDAO와 맞추기

    // 오늘, 내일 모델
    private final DefaultListModel<TodoItem> todoToday = new DefaultListModel<>();
    private final JList<TodoItem> todoListToday = new JList<>(todoToday);
    private final DefaultListModel<TodoItem> todoTomorrow = new DefaultListModel<>();
    private final JList<TodoItem> todoListTomorrow = new JList<>(todoTomorrow);

    // 루틴 모델
    private final DefaultListModel<RoutineItem> routineModel = new DefaultListModel<>();
    private final JList<RoutineItem> routineList = new JList<>(routineModel);
    private final DefaultListModel<RoutineItem> skippedRoutineModel = new DefaultListModel<>();

    // 캘린더 패널 참조 + 날짜 포맷
    private final CalendarPanel calendarPanel;
    private final SimpleDateFormat dateKeyFormat =
            new SimpleDateFormat("yyyy-MM-dd");

    // 전체
    public TodoPanel(CalendarPanel calendarPanel) {
        this.calendarPanel = calendarPanel;

        setLayout(new BorderLayout());
        setBackground(Color.white);

        // Renderer 설정
        routineList.setCellRenderer(new RoutineCellRenderer());
        todoListToday.setCellRenderer(new TodoCellRenderer());
        todoListTomorrow.setCellRenderer(new TodoCellRenderer());

        // Todo 클릭 이벤트 (체크, 상세보기)
        addTodoToggleListener(todoListToday, todoToday);
        addTodoToggleListener(todoListTomorrow, todoTomorrow);

        // 루틴 상태 / 시간 수정 리스너
        addRoutineStateListener();

        // DB에서 루틴 목록 읽기
        loadRoutinesFromDB();

        // 캘린더에서 오늘/내일 일정 + 루틴 불러오기
        TTC();

        // 어제 미완료 일정 → 오늘로 가져오기 (밀린 할 일)
        loadYesterdayUnfinished();

        // 탭 구성
        JComponent homeCard = buildHomeCard();
        add(homeCard, BorderLayout.CENTER);
    }

    // (필요 시) CalendarPanel 쪽에서 호출
    public void refreshFromCalendar() {
        TTC();
        loadYesterdayUnfinished();   // 새로 불러올 때도 어제 미완료 반영
        repaint();
    }

    // 캘린더에서 오늘, 내일 일정 불러오기
    private void TTC() {

        // 기존 내용 초기화
        todoToday.clear();
        todoTomorrow.clear();

        // 오늘 / 내일 날짜 계산
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        String todayKey = dateKeyFormat.format(today.getTime());
        String tomorrowKey = dateKeyFormat.format(tomorrow.getTime());

        // CalendarPanel 에서 scheduleData 꺼내오기
        Map<String, Vector<ScheduleItem>> data = calendarPanel.getScheduleData();

        // 오늘 일정 Todo 모델에 추가
        Vector<ScheduleItem> todaySchedules = data.get(todayKey);
        if (todaySchedules != null) {
            for (ScheduleItem s : todaySchedules) {
                long groupId   = s.getId();          // calendars 쪽에서 이미 세팅
                String title   = s.getTitle();
                String time    = s.getStartTime();
                String endTime = s.getEndTime();
                String category= s.getCategory();

                boolean done = isDoneInDB(groupId);

                todoToday.addElement(
                        new TodoItem(groupId, title, done, false, time, endTime, category)
                );
            }
        }

        // 루틴을 오늘 TODO에 다시 반영
        for (int i = 0; i < routineModel.size(); i++) {
            RoutineItem r = routineModel.get(i);
            if (r.state != RoutineState.SKIP) {      // 스킵 아닌 루틴만
                todoToday.addElement(
                        new TodoItem(r.text, false, false, r.startTime, null, "루틴")
                );
            }
        }

        // 내일 일정 -> 내일 Todo 모델에 추가
        Vector<ScheduleItem> tomorrowSchedules = data.get(tomorrowKey);
        if (tomorrowSchedules != null) {
            for (ScheduleItem s : tomorrowSchedules) {
                long groupId   = s.getId();
                String title   = s.getTitle();
                String time    = s.getStartTime();
                String endTime = s.getEndTime();
                String category= s.getCategory();

                boolean done = isDoneInDB(groupId);

                todoTomorrow.addElement(
                        new TodoItem(groupId, title, done, false, time, endTime, category)
                );
            }
        }
    }

    // ★ 어제 미완료 할 일 → 오늘 리스트로 가져오기 (밀린 할 일로 표시)
    private void loadYesterdayUnfinished() {
        // 어제 날짜 계산
        Calendar today = Calendar.getInstance();
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        String yesterdayKey = dateKeyFormat.format(yesterday.getTime());

        Map<String, Vector<ScheduleItem>> data = calendarPanel.getScheduleData();
        Vector<ScheduleItem> yesterdaySchedules = data.get(yesterdayKey);
        if (yesterdaySchedules == null) return;

        for (ScheduleItem s : yesterdaySchedules) {
            long groupId   = s.getId();
            String title   = s.getTitle();
            String time    = s.getStartTime();
            String endTime = s.getEndTime();
            String category= s.getCategory();

            // 이미 오늘 목록에 같은 groupId가 있으면 스킵 (중복 방지)
            if (containsTodo(todoToday, groupId)) continue;

            // DB에서 DONE이면 스킵, 아니면 미완료 → 밀린 할 일로 추가
            boolean done = isDoneInDB(groupId);
            if (!done) {
                TodoItem overdue = new TodoItem(
                        groupId,
                        title,
                        false,      // done = false
                        true,       // over = true → 밀린 할 일
                        time,
                        endTime,
                        category
                );
                todoToday.addElement(overdue);
            }
        }
    }

    // 해당 groupId를 가진 Todo가 이미 있는지 검사
    private boolean containsTodo(DefaultListModel<TodoItem> model, long groupId) {
        for (int i = 0; i < model.size(); i++) {
            TodoItem ti = model.get(i);
            if (ti.groupId == groupId) {
                return true;
            }
        }
        return false;
    }

    // 상단 Tab
    private JComponent buildHomeCard() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel todotab = buildTodoMainTab();
        JPanel routinetab = buildRoutineTab();

        tabs.addTab("할일", todotab);
        tabs.addTab("루틴", routinetab);
        return tabs;
    }

    // 할일 탭 전체
    private JPanel buildTodoMainTab() {

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Color.white);

        JPanel todoTab = new JPanel(new GridLayout(1, 2, 16, 0));
        todoTab.setBackground(Color.white);

        // 오늘 / 내일 날짜 라벨
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat labelFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todayLabel = "오늘 할 일 (" + labelFormat.format(today.getTime()) + ")";
        String tomorrowLabel = "내일 할 일 (" + labelFormat.format(tomorrow.getTime()) + ")";

        JPanel todayPanel = buildTodoTab(todayLabel, todoListToday, todoToday);
        JPanel tomorrowPanel = buildTodoTab(tomorrowLabel, todoListTomorrow, todoTomorrow);

        todoTab.add(todayPanel);
        todoTab.add(tomorrowPanel);

        root.add(todoTab, BorderLayout.CENTER);

        return root;
    }

    // 개별 Todo 리스트 패널
    private JPanel buildTodoTab(String titleText,
                                JList<TodoItem> list,
                                DefaultListModel<TodoItem> model) {
        JPanel todopanel = new JPanel(new BorderLayout(10, 10));
        todopanel.setBackground(Color.white);

        JLabel title = new JLabel(titleText);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        todopanel.add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(list);
        todopanel.add(scrollPane, BorderLayout.CENTER);

        return todopanel;
    }

    // Todo 완료 / 상세보기
    private void addTodoToggleListener(JList<TodoItem> list, DefaultListModel<TodoItem> model) {
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int range = list.locationToIndex(e.getPoint());
                if (range < 0) return; // 밖 클릭

                Rectangle cellBounds = list.getCellBounds(range, range);
                if (cellBounds == null) return;

                int rel = e.getX() - cellBounds.x;
                boolean inCheckBox = (rel >= 0 && rel <= 25);

                if (inCheckBox && e.getClickCount() == 1) {
                    // 체크박스 클릭 → 완료 토글
                    TodoItem item = model.get(range);
                    item.done = !item.done;
                    updateStatusInDB(item.groupId, item.done); // DB 업데이트
                    list.repaint();
                } else if (!inCheckBox && e.getClickCount() == 2) {
                    // 더블클릭 → 상세보기
                    TodoItem item = model.get(range);
                    Window owner = SwingUtilities.getWindowAncestor(TodoPanel.this);
                    TodoDetailDialog dialog = new TodoDetailDialog(owner, item, list);
                    dialog.setVisible(true);
                }
            }
        });
    }

    // 루틴 탭
    private JPanel buildRoutineTab() {
        JPanel routineTab = new JPanel(new BorderLayout(10, 10));
        routineTab.setBackground(Color.white);

        JLabel title = new JLabel("루틴");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        routineTab.add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(routineList);
        routineTab.add(scroll, BorderLayout.CENTER);

        JPanel input = new JPanel(new BorderLayout(5, 5));
        JTextField inputField = new JTextField();
        JButton addButton = new JButton("추가");
        JButton deleteButton = new JButton("삭제");
        JButton skipButton = new JButton("스킵");
        JButton viewSkipButton = new JButton("스킵 목록");

        input.add(inputField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        btnPanel.add(addButton);
        btnPanel.add(deleteButton);
        btnPanel.add(skipButton);
        btnPanel.add(viewSkipButton);
        input.add(btnPanel, BorderLayout.EAST);
        routineTab.add(input, BorderLayout.SOUTH);

        // 루틴 추가: 시간 입력 다이얼로그 띄우고 Todo에도 추가
        addButton.addActionListener(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                Window owner = SwingUtilities.getWindowAncestor(TodoPanel.this);
                RoutineDetailDialog dialog = new RoutineDetailDialog(owner, text, null);

                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    String startTime = dialog.getStartTime(); // 시간 입력

                    insertRoutineToDB(text);   // (지금은 title만 저장)

                    RoutineItem r = new RoutineItem(text, startTime); // startTime 포함
                    routineModel.addElement(r);

                    // 오늘 Todo에도 이 루틴 추가
                    todoToday.addElement(new TodoItem(text, false, false, startTime, null, "루틴"));

                    inputField.setText("");
                    refreshROutineColorsInTodo();
                    repaint();
                }
            }
        });

        // 루틴 삭제: 루틴 + Todo + DB 모두에서 제거
        deleteButton.addActionListener(e -> {
            int idx = routineList.getSelectedIndex();
            if (idx >= 0) {
                RoutineItem r = routineModel.get(idx);
                String routineText = r.text;

                // DB에서 삭제
                deleteRoutineFromDB(routineText);

                // 메모리에서 삭제
                routineModel.remove(idx);

                // TodoToday/Tomorrow 에서도 루틴 항목 제거
                removeTodoOfRoutine(routineText);

                refreshROutineColorsInTodo();
                repaint();
            }
        });

        // 스킵: 루틴 모델에서 빼고, Todo에서도 제거 (DB에는 남겨둠)
        skipButton.addActionListener(e -> {
            int idx = routineList.getSelectedIndex();
            if (idx >= 0) {
                RoutineItem item = routineModel.get(idx);
                String routineText = item.text;

                item.state = RoutineState.SKIP;

                routineModel.remove(idx);
                skippedRoutineModel.addElement(item);

                // Todo에서도 제거
                removeTodoOfRoutine(routineText);

                routineList.repaint();
                refreshROutineColorsInTodo();
                repaint();
            }
        });

        // SKIP만 모아서 보여주기 + 되돌리기
        viewSkipButton.addActionListener(e -> {
            JList<RoutineItem> skipList = new JList<>(skippedRoutineModel);
            skipList.setCellRenderer(new RoutineCellRenderer());

            JScrollPane skipScroll = new JScrollPane(skipList);

            Object[] options = {"되돌리기", "닫기"};
            int result = JOptionPane.showOptionDialog(
                    routineTab,
                    skipScroll,
                    "스킵한 루틴 목록",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            // "되돌리기"를 눌렀을 때
            if (result == 0) {
                int sel = skipList.getSelectedIndex();
                if (sel >= 0) {
                    RoutineItem item = skippedRoutineModel.get(sel);
                    skippedRoutineModel.remove(sel);

                    item.state = RoutineState.TODO;
                    routineModel.addElement(item);

                    // 되돌릴 때 오늘 Todo에도 다시 추가
                    todoToday.addElement(
                            new TodoItem(item.text, false, false, item.startTime, null, "루틴")
                    );

                    routineList.repaint();
                    refreshROutineColorsInTodo();
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(
                            routineTab,
                            "되돌릴 루틴을 먼저 선택하세요.",
                            "알림",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });

        return routineTab;
    }

    // 루틴 상태 변경 / 더블클릭으로 시간 수정
    private void addRoutineStateListener() {
        routineList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = routineList.locationToIndex(e.getPoint());
                if (idx < 0) return;

                Rectangle cellBounds = routineList.getCellBounds(idx, idx);
                if (cellBounds == null) return;

                int relX = e.getX() - cellBounds.x;
                boolean inCheckBox = (relX >= 0 && relX <= 25);

                if (inCheckBox && e.getClickCount() == 1) {
                    RoutineItem item = routineModel.get(idx);

                    // SKIP이면 건드리지 않음
                    if (item.state == RoutineState.SKIP) return;

                    if (item.state == RoutineState.DONE) {
                        item.state = RoutineState.TODO;
                    } else {
                        item.state = RoutineState.DONE;
                    }

                    routineList.repaint();
                    refreshROutineColorsInTodo();
                }
                // 더블클릭 → 시간 수정, Todo에도 반영
                else if (e.getClickCount() == 2) {
                    RoutineItem item = routineModel.get(idx);

                    Window owner = SwingUtilities.getWindowAncestor(TodoPanel.this);
                    RoutineDetailDialog dialog =
                            new RoutineDetailDialog(owner, item.text, item.startTime);

                    dialog.setVisible(true);

                    if (dialog.isConfirmed()) {
                        item.startTime = dialog.getStartTime();
                        routineList.repaint();

                        // Todo 목록에도 시간 동기화
                        syncRoutineTimeToTodos(item.text, item.startTime);
                        repaint();
                    }
                }
            }
        });
    }

    private void refreshROutineColorsInTodo() {
        todoListToday.repaint();
        todoListTomorrow.repaint();
    }

    // 루틴 호출 메서드
    private void loadRoutinesFromDB() {
        routineModel.clear();

        String sql = "SELECT title FROM routines WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, USER_ID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String title = rs.getString("title");
                    RoutineItem r = new RoutineItem(title);
                    routineModel.addElement(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 새 루틴을 DB에 INSERT
    private void insertRoutineToDB(String title) {
        String sql = "INSERT INTO routines (user_id, title, repeat_rule) VALUES (?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, USER_ID);
            ps.setString(2, title);
            ps.setString(3, "none");

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 루틴을 DB에서 삭제
    private void deleteRoutineFromDB(String title) {
        String sql = "DELETE FROM routines WHERE user_id = ? AND title = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, USER_ID);
            ps.setString(2, title);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // TODOITEM 이 루틴에 해당하는지 확인
    private boolean isRoutineTodo(TodoItem item) {
        if (item == null) return false;
        String text = item.text;
        for (int i = 0; i < routineModel.size(); i++) {
            RoutineItem r = routineModel.get(i);
            if (r.state != RoutineState.SKIP && text.equals(r.text)) {
                return true;
            }
        }
        return false;
    }

    // DB에서 현재 status가 DONE인지 확인
    private boolean isDoneInDB(long groupId) {
        if (groupId == 0L) return false;  // 루틴 등 DB 없는 항목

        String sql = "SELECT status FROM todos WHERE user_id = ? AND group_id = ? LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, USER_ID);
            ps.setLong(2, groupId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    return "DONE".equalsIgnoreCase(status);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 체크박스 눌렀을 때 DB 업데이트
    private void updateStatusInDB(long groupId, boolean done) {
        if (groupId == 0L) return;

        String sql = "UPDATE todos SET status = ? WHERE user_id = ? AND group_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, done ? "DONE" : "PENDING");
            ps.setLong(2, USER_ID);
            ps.setLong(3, groupId);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 루틴 텍스트로 TodoToday/Tomorrow에서 해당 루틴 삭제
    private void removeTodoOfRoutine(String routineText) {
        for (int i = todoToday.size() - 1; i >= 0; i--) {
            TodoItem ti = todoToday.get(i);
            if ("루틴".equals(ti.category) && routineText.equals(ti.text)) {
                todoToday.remove(i);
            }
        }
        for (int i = todoTomorrow.size() - 1; i >= 0; i--) {
            TodoItem ti = todoTomorrow.get(i);
            if ("루틴".equals(ti.category) && routineText.equals(ti.text)) {
                todoTomorrow.remove(i);
            }
        }
    }

    // 루틴 시간 변경 시 Todo 목록의 time 필드 동기화
    private void syncRoutineTimeToTodos(String routineText, String newTime) {
        for (int i = 0; i < todoToday.size(); i++) {
            TodoItem ti = todoToday.get(i);
            if ("루틴".equals(ti.category) && routineText.equals(ti.text)) {
                ti.time = newTime;
            }
        }
        for (int i = 0; i < todoTomorrow.size(); i++) {
            TodoItem ti = todoTomorrow.get(i);
            if ("루틴".equals(ti.category) && routineText.equals(ti.text)) {
                ti.time = newTime;
            }
        }
    }

    // ===== 렌더러들 =====

    private class TodoCellRenderer extends JPanel implements ListCellRenderer<TodoItem> {
        private final JCheckBox checkBox;
        private final JLabel textLabel;

        public TodoCellRenderer() {
            setLayout(new BorderLayout(5, 0));
            setOpaque(true);

            checkBox = new JCheckBox();
            checkBox.setOpaque(false);

            textLabel = new JLabel();

            add(checkBox, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends TodoItem> list,
                TodoItem value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (value != null) {
                // 체크 상태
                checkBox.setSelected(value.done);

                // 시간 + 텍스트 같이 표시
                if (value.time != null && !value.time.isEmpty()) {
                    textLabel.setText("[" + value.time + "] " + value.text);
                } else {
                    textLabel.setText(value.text);
                }

                // 기본 색
                Color fg = Color.BLACK;
                Color bg = list.getBackground();

                // 밀린 할 일
                if (value.over) {
                    fg = Color.RED;
                }

                // 루틴 Todo면 색 다르게
                if (isRoutineTodo(value)) {
                    fg = new Color(0, 150, 0); // 루틴: 초록 톤
                }

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    textLabel.setForeground(list.getSelectionForeground());
                } else {
                    setBackground(bg);
                    textLabel.setForeground(fg);
                }
            }

            return this;
        }
    }

    private static class RoutineCellRenderer extends JPanel implements ListCellRenderer<RoutineItem> {

        private final JCheckBox checkBox;
        private final JLabel textLabel;

        public RoutineCellRenderer() {
            setLayout(new BorderLayout(5, 0));
            setOpaque(true);

            checkBox = new JCheckBox();
            checkBox.setOpaque(false);

            textLabel = new JLabel();

            add(textLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends RoutineItem> list,
                RoutineItem value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (value != null) {
                checkBox.setSelected(value.state == RoutineState.DONE);

                // 루틴 리스트에서도 시간 보이게
                if (value.startTime != null && !value.startTime.isEmpty()) {
                    textLabel.setText("[" + value.startTime + "] " + value.text);
                } else {
                    textLabel.setText(value.text);
                }

                if (value.state == RoutineState.SKIP) {
                    textLabel.setForeground(Color.GRAY);
                } else {
                    textLabel.setForeground(Color.BLACK);
                }
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }

            return this;
        }
    }
}
