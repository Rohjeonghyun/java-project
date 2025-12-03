package todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

import calendars.CalendarDAO;
import calendars.CalendarPanel;
import calendars.CategoryItem;
import calendars.ScheduleDialog;
import calendars.ScheduleItem;

public class TodoPanel extends JPanel implements ActionListener {

	private final long userId;

    // --- DAO & Data ---
    private final CalendarPanel calendarPanel;
    private final CalendarDAO calendarDao;
    private final TodoDao todoDao;
    private final RoutineDao routineDao;
    
    private Vector<CategoryItem> categories;

    // --- UI Models ---
    private final DefaultListModel<TodoItem> todoToday = new DefaultListModel<>();
    private final JList<TodoItem> todoListToday = new JList<>(todoToday);
    private final DefaultListModel<TodoItem> todoTomorrow = new DefaultListModel<>();
    private final JList<TodoItem> todoListTomorrow = new JList<>(todoTomorrow);

    private final DefaultListModel<RoutineItem> routineModel = new DefaultListModel<>();
    private final JList<RoutineItem> routineList = new JList<>(routineModel);
    private final DefaultListModel<RoutineItem> skippedRoutineModel = new DefaultListModel<>();

    private final SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd");


    private JButton addTodayTodoBtn, addTomorrowTodoBtn; // [NEW] 할 일 추가 버튼

    /**
     * 생성자
     */
    public TodoPanel(long userId, CalendarPanel calendarPanel, Vector<CategoryItem> categories, CalendarDAO calendarDao) {
    	this.userId = userId;
        this.calendarPanel = calendarPanel;
        this.categories = categories;
        this.calendarDao = calendarDao;
        
        // [NEW] DAO 초기화
        this.todoDao = new TodoDao();
        this.routineDao = new RoutineDao();

        setLayout(new BorderLayout());
        setBackground(Color.white);

        // 렌더러 설정
        routineList.setCellRenderer(new RoutineCellRenderer());
        todoListToday.setCellRenderer(new TodoCellRenderer());
        todoListTomorrow.setCellRenderer(new TodoCellRenderer());

        // 리스너 연결
        addTodoToggleListener(todoListToday, todoToday);
        addTodoToggleListener(todoListTomorrow, todoTomorrow);
        addRoutineStateListener();

        // 초기 데이터 로드 (DB -> DAO -> UI)
        loadRoutines();          // 루틴 목록
        loadTodos();             // 오늘/내일 할 일 (TTC 대체)
        loadYesterdayUnfinished(); // 밀린 할 일

        // 화면 구성
        add(buildHomeCard(), BorderLayout.CENTER);
    }

    /**
     * 캘린더 등 외부에서 데이터 변경 시 호출
     */
    public void refreshFromCalendar() {
        loadTodos();
        loadYesterdayUnfinished();
        repaint();
    }

    // =============================================================
    // [1] 데이터 로드 로직 (DAO 사용)
    // =============================================================

 // [수정] loadRoutines 메소드
    private void loadRoutines() {
        routineModel.clear();
        // [수정] this.userId 사용
        Vector<RoutineItem> list = routineDao.getRoutines(userId);
        for (RoutineItem item : list) {
            routineModel.addElement(item);
        }
    }

    /**
     * [NEW] 오늘/내일 할 일을 DB에서 불러옴 (기존 TTC 메소드 대체)
     */
    private void loadTodos() {
        todoToday.clear();
        todoTomorrow.clear();

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        String todayKey = dateKeyFormat.format(today.getTime());
        String tomorrowKey = dateKeyFormat.format(tomorrow.getTime());

     // [수정] this.userId 사용
        Vector<TodoItem> todayItems = todoDao.getTodosByDate(userId, todayKey);
        for (TodoItem item : todayItems) {
            todoToday.addElement(item);
        }

        // 2. 루틴을 오늘 할 일에 병합
        // (스킵되지 않은 루틴은 오늘 할 일 목록에도 표시)
        for (int i = 0; i < routineModel.size(); i++) {
            RoutineItem r = routineModel.get(i);
            if (r.state != RoutineState.SKIP) {
                boolean done = (r.state == RoutineState.DONE);
                // 루틴 아이템을 TodoItem으로 변환하여 추가
                todoToday.addElement(
                        new TodoItem(r.text, done, false, r.startTime, null, "루틴")
                );
            }
        }

     // [수정] this.userId 사용
        Vector<TodoItem> tomorrowItems = todoDao.getTodosByDate(userId, tomorrowKey);
        for (TodoItem item : tomorrowItems) {
            todoTomorrow.addElement(item);
        }
    }

    /**
     * 어제 미완료된 할 일을 불러와서 '밀린 할 일'로 표시
     */
    private void loadYesterdayUnfinished() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        String yesterdayKey = dateKeyFormat.format(yesterday.getTime());

        // 어제 날짜의 모든 할 일 조회
        Vector<TodoItem> yesterdayItems = todoDao.getTodosByDate(userId, yesterdayKey);

        for (TodoItem item : yesterdayItems) {
            // 이미 완료된 것은 제외
            if (item.done) continue;

            // 이미 오늘 목록에 같은 내용(그룹ID)이 있으면 중복 추가 방지
            if (containsTodo(todoToday, item.groupId)) continue;

            // 'over'(밀림) 플래그를 true로 설정하여 추가
            item.over = true; 
            todoToday.addElement(item);
        }
    }

    private boolean containsTodo(DefaultListModel<TodoItem> model, long groupId) {
        if (groupId == 0) return false;
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).groupId == groupId) return true;
        }
        return false;
    }


    // =============================================================
    // [2] UI 구성 (Buttons & Layout)
    // =============================================================

    private JComponent buildHomeCard() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel todotab = buildTodoMainTab();
        JPanel routinetab = buildRoutineTab();

        tabs.addTab("할일", todotab);
        tabs.addTab("루틴", routinetab);
        return tabs;
    }

    private JPanel buildTodoMainTab() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Color.white);

        // 상단 카테고리 관리 버튼 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.white);
        root.add(topPanel, BorderLayout.NORTH);

        // 할 일 리스트 패널 (좌: 오늘, 우: 내일)
        JPanel todoTab = new JPanel(new GridLayout(1, 2, 16, 0));
        todoTab.setBackground(Color.white);

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        // [NEW] 할 일 추가 버튼 생성
        addTodayTodoBtn = new JButton("+");
        addTomorrowTodoBtn = new JButton("+");
        addTodayTodoBtn.addActionListener(this);
        addTomorrowTodoBtn.addActionListener(this);

        JPanel todayPanel = buildTodoTab("오늘 (" + fmt.format(today.getTime()) + ")", todoListToday, addTodayTodoBtn);
        JPanel tomorrowPanel = buildTodoTab("내일 (" + fmt.format(tomorrow.getTime()) + ")", todoListTomorrow, addTomorrowTodoBtn);

        todoTab.add(todayPanel);
        todoTab.add(tomorrowPanel);
        root.add(todoTab, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildTodoTab(String titleText, JList<TodoItem> list, JButton addBtn) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.white);

        // 헤더 (제목 + 추가 버튼)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.white);
        
        JLabel title = new JLabel(titleText);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        
        header.add(title, BorderLayout.CENTER);
        header.add(addBtn, BorderLayout.EAST); // 오른쪽에 + 버튼 배치

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRoutineTab() {
        JPanel routineTab = new JPanel(new BorderLayout(10, 10));
        routineTab.setBackground(Color.white);

        JLabel title = new JLabel("루틴");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        routineTab.add(title, BorderLayout.NORTH);

        routineTab.add(new JScrollPane(routineList), BorderLayout.CENTER);

        // 루틴 추가/삭제/스킵 버튼 패널
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

        // [루틴 추가]
        addButton.addActionListener(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                Window owner = SwingUtilities.getWindowAncestor(TodoPanel.this);
                RoutineDetailDialog dialog = new RoutineDetailDialog(owner, text, null);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    String startTime = dialog.getStartTime();
                    long newId = routineDao.addRoutine(userId, text, startTime);
                    
                    // UI 업데이트
                    RoutineItem r = new RoutineItem(newId, text, RoutineState.TODO, startTime);
                    routineModel.addElement(r);
                    todoToday.addElement(new TodoItem(text, false, false, startTime, null, "루틴"));
                    
                    inputField.setText("");
                    refreshROutineColorsInTodo();
                }
            }
        });

        // [루틴 삭제]
        deleteButton.addActionListener(e -> {
            int idx = routineList.getSelectedIndex();
            if (idx >= 0) {
                RoutineItem r = routineModel.get(idx);
                // [DAO] 루틴 삭제
                routineDao.deleteRoutine(userId, r.text);
                
                routineModel.remove(idx);
                removeTodoOfRoutine(r.text); // 할일 목록에서도 제거
                refreshROutineColorsInTodo();
            }
        });

        // [루틴 스킵]
        skipButton.addActionListener(e -> {
            int idx = routineList.getSelectedIndex();
            if (idx >= 0) {
                RoutineItem item = routineModel.get(idx);
                item.state = RoutineState.SKIP;
                // [DAO] 상태 업데이트
                routineDao.updateRoutineState(userId, item);

                routineModel.remove(idx);
                skippedRoutineModel.addElement(item);
                removeTodoOfRoutine(item.text); // 할일 목록에서도 제거
                refreshROutineColorsInTodo();
            }
        });

        // [스킵 목록 보기]
        viewSkipButton.addActionListener(e -> {
            JList<RoutineItem> skipList = new JList<>(skippedRoutineModel);
            skipList.setCellRenderer(new RoutineCellRenderer());
            JScrollPane skipScroll = new JScrollPane(skipList);

            Object[] options = {"되돌리기", "닫기"};
            int result = JOptionPane.showOptionDialog(routineTab, skipScroll, "스킵한 루틴 목록",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);

            if (result == 0) { // 되돌리기
                int sel = skipList.getSelectedIndex();
                if (sel >= 0) {
                    RoutineItem item = skippedRoutineModel.get(sel);
                    skippedRoutineModel.remove(sel);
                    item.state = RoutineState.TODO;
                    
                    routineDao.updateRoutineState(userId, item);
                    
                    routineModel.addElement(item);
                    todoToday.addElement(new TodoItem(item.text, false, false, item.startTime, null, "루틴"));
                    refreshROutineColorsInTodo();
                }
            }
        });

        return routineTab;
    }


    // =============================================================
    // [3] 이벤트 처리 (ActionListener, MouseListener)
    // =============================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == addTodayTodoBtn || source == addTomorrowTodoBtn) {
            // [NEW] 할 일 추가 버튼 클릭 시 ScheduleDialog 호출
            Calendar targetDate = Calendar.getInstance();
            if (source == addTomorrowTodoBtn) {
                targetDate.add(Calendar.DAY_OF_MONTH, 1); // 내일 날짜
            }

            Window parent = SwingUtilities.getWindowAncestor(this);
            String title = "일정 추가";
            
            // CalendarPanel의 데이터 맵을 참조 (ScheduleDialog가 요구함)
            Map<String, Vector<ScheduleItem>> globalScheduleData = calendarPanel.getScheduleData();
            
            // ScheduleDialog 띄우기
            // (주의: 여기서는 임시로 null을 넘기는 부분들이 있습니다. ScheduleDialog가 CalendarPanel 전용으로 설계되었기 때문입니다.
            //  TodoPanel에서 독립적으로 쓰려면 ScheduleDialog를 조금 수정하거나, 아래처럼 필요한 부분만 맞춰서 넘겨야 합니다.)
            ScheduleDialog dialog = new ScheduleDialog(
                parent, title, targetDate, categories, 
                globalScheduleData, null, null, calendarDao // currentVector/UiModel은 null로 전달 (저장 후 refreshFromCalendar로 갱신)
            );
            
            dialog.setVisible(true);
            
            // 다이얼로그 닫힌 후 화면 새로고침 (DB에서 다시 불러오기)
            refreshFromCalendar();
        }
    }

    // 할 일 클릭 리스너 (체크박스 토글 / 상세조회)
    private void addTodoToggleListener(JList<TodoItem> list, DefaultListModel<TodoItem> model) {
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index < 0) return;

                // 체크박스 영역 클릭 판별 (대략 앞쪽 25px)
                Rectangle cellBounds = list.getCellBounds(index, index);
                boolean inCheckBox = (e.getX() - cellBounds.x) <= 25;

                if (inCheckBox && e.getClickCount() == 1) {
                    TodoItem item = model.get(index);
                    item.done = !item.done;

                    // [DAO] 상태 업데이트
                    if (isRoutineTodo(item)) { // 루틴인 경우
                        RoutineItem r = findRoutineByText(item.text);
                        if (r != null) {
                            r.state = item.done ? RoutineState.DONE : RoutineState.TODO;
                            routineDao.updateRoutineState(userId, r);
                            routineList.repaint();
                        }
                    } else { // 일반 일정인 경우
                    	todoDao.updateTodoStatus(userId, item.groupId, item.done);
                    }
                    list.repaint();
                    
                } else if (!inCheckBox && e.getClickCount() == 2) {
                    // 더블 클릭 시 상세 보기
                    TodoItem item = model.get(index);
                    Window owner = SwingUtilities.getWindowAncestor(TodoPanel.this);
                    TodoDetailDialog dialog = new TodoDetailDialog(owner, item, list);
                    dialog.setVisible(true);
                }
            }
        });
    }

    // 루틴 리스트 리스너
    private void addRoutineStateListener() {
        routineList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = routineList.locationToIndex(e.getPoint());
                if (index < 0) return;

                Rectangle cellBounds = routineList.getCellBounds(index, index);
                boolean inCheckBox = (e.getX() - cellBounds.x) <= 25;

                if (inCheckBox && e.getClickCount() == 1) {
                    RoutineItem item = routineModel.get(index);
                    if (item.state == RoutineState.SKIP) return;

                    item.state = (item.state == RoutineState.DONE) ? RoutineState.TODO : RoutineState.DONE;
                    
                    // [DAO] 루틴 상태 업데이트
                    routineDao.updateRoutineState(userId, item);
                    
                    routineList.repaint();
                    refreshROutineColorsInTodo(); // 할일 탭의 루틴 색상도 갱신
            } else if (!inCheckBox && e.getClickCount() == 2) {
                // 더블 클릭 시 시간 수정
                RoutineItem item = routineModel.get(index);
                Window owner = SwingUtilities.getWindowAncestor(TodoPanel.this);
                RoutineDetailDialog dialog = new RoutineDetailDialog(owner, item.text, item.startTime);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    String newTime = dialog.getStartTime();

                    // [FIX] DB에 변경된 시간 저장 호출
                    if (routineDao.updateRoutineTime(userId, item.id, newTime)) {
                        // DB 저장 성공 시 메모리(UI)도 업데이트
                        item.startTime = newTime;
                        routineList.repaint();
                        
                        // 할 일 목록에 표시된 루틴 시간도 동기화
                        syncRoutineTimeToTodos(item.text, item.startTime);
                        repaint();
                    } else {
                        JOptionPane.showMessageDialog(TodoPanel.this, "루틴 시간 수정 실패 (DB 오류)");
                    }
                }
            }
            }
        });
    }

    // =============================================================
    // [4] 기타 헬퍼 메소드
    // =============================================================

    private boolean isRoutineTodo(TodoItem item) {
        if (item == null) return false;
        for (int i = 0; i < routineModel.size(); i++) {
            RoutineItem r = routineModel.get(i);
            if (r.state != RoutineState.SKIP && item.text.equals(r.text)) return true;
        }
        return false;
    }

    private RoutineItem findRoutineByText(String text) {
        for (int i = 0; i < routineModel.size(); i++) {
            RoutineItem r = routineModel.get(i);
            if (r.text.equals(text)) return r;
        }
        return null;
    }

    private void removeTodoOfRoutine(String text) {
        removeTodoFromModel(todoToday, text);
        removeTodoFromModel(todoTomorrow, text);
    }

    private void removeTodoFromModel(DefaultListModel<TodoItem> model, String text) {
        for (int i = model.size() - 1; i >= 0; i--) {
            if ("루틴".equals(model.get(i).category) && text.equals(model.get(i).text)) {
                model.remove(i);
            }
        }
    }

    private void syncRoutineTimeToTodos(String text, String time) {
        updateTimeInModel(todoToday, text, time);
        updateTimeInModel(todoTomorrow, text, time);
    }

    private void updateTimeInModel(DefaultListModel<TodoItem> model, String text, String time) {
        for (int i = 0; i < model.size(); i++) {
            TodoItem item = model.get(i);
            if ("루틴".equals(item.category) && text.equals(item.text)) {
                item.time = time;
            }
        }
    }

    private void refreshROutineColorsInTodo() {
        todoListToday.repaint();
        todoListTomorrow.repaint();
    }

    // [내부 클래스] 렌더러
    private class TodoCellRenderer extends JPanel implements ListCellRenderer<TodoItem> {
        private final JCheckBox checkBox = new JCheckBox();
        private final JLabel textLabel = new JLabel();

        public TodoCellRenderer() {
            setLayout(new BorderLayout(5, 0));
            setOpaque(true);
            checkBox.setOpaque(false);
            add(checkBox, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends TodoItem> list, TodoItem value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                checkBox.setSelected(value.done);
                textLabel.setText((value.time != null ? "[" + value.time + "] " : "") + value.text);
                
                Color fg = Color.BLACK;
                if (value.over) fg = Color.RED;
                if (isRoutineTodo(value)) fg = new Color(0, 150, 0);

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    textLabel.setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    textLabel.setForeground(fg);
                }
            }
            return this;
        }
    }

    private static class RoutineCellRenderer extends JPanel implements ListCellRenderer<RoutineItem> {
        private final JCheckBox checkBox = new JCheckBox();
        private final JLabel textLabel = new JLabel();

        public RoutineCellRenderer() {
            setLayout(new BorderLayout(5, 0));
            setOpaque(true);
            checkBox.setOpaque(false);
            add(textLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends RoutineItem> list, RoutineItem value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                checkBox.setSelected(value.state == RoutineState.DONE);
                textLabel.setText((value.startTime != null ? "[" + value.startTime + "] " : "") + value.text);
                textLabel.setForeground(value.state == RoutineState.SKIP ? Color.GRAY : Color.BLACK);
            }
            if (isSelected) setBackground(list.getSelectionBackground());
            else setBackground(list.getBackground());
            return this;
        }
    }
}