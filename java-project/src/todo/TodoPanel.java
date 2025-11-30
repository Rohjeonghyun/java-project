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

   
    // 오늘,내일 모델
    private final DefaultListModel<TodoItem> todoToday = new DefaultListModel<>();
    private final JList<TodoItem> todoListToday = new JList<>(todoToday);
    private final DefaultListModel<TodoItem> todoTomorrow = new DefaultListModel<>();
    private final JList<TodoItem> todoListTomorrow = new JList<>(todoTomorrow);
    

    // 루틴 모델
    private final DefaultListModel<RoutineItem> routineModel = new DefaultListModel<>();
    private final JList<RoutineItem> routineList = new JList<>(routineModel);
    private final DefaultListModel<RoutineItem> skippedRoutineModel = new DefaultListModel<>();
    
    // 캘린더 패널 참조 + 날짜 초기화
    private final CalendarPanel calendarPanel;
    private final SimpleDateFormat dateKeyFormat =
            new SimpleDateFormat("yyyy-MM-dd");


    // 전체
    public TodoPanel(CalendarPanel calendarPanel) {
        this.calendarPanel = calendarPanel;   

        setLayout(new BorderLayout());
        setBackground(Color.white);
        
        
        // routine Renderer
        routineList.setCellRenderer(new RoutineCellRenderer());
        
        // Todo Renderer
        todoListToday.setCellRenderer(new TodoCellRenderer());
        todoListTomorrow.setCellRenderer(new TodoCellRenderer());
         
        // Todo에서 오늘 내일 클릭으로 체크
        addTodoToggleListener(todoListToday, todoToday);
        addTodoToggleListener(todoListTomorrow, todoTomorrow);

        // Routine 상태
        addRoutineStateListener();
        
        // DB 읽어오기ㄹ
        loadRoutinesFromDB();
        
        // 캘린더에서 오늘/내일 일정 불러오기
        TTC();
        
        JComponent homeCard = buildHomeCard();
        add(homeCard, BorderLayout.CENTER);
        
    }
  //CalendarPanel 쪽에서 호출 메소드
    public void refreshFromCalendar() {
       TTC(); //오늘 내일 일정 읽어오기
       repaint(); // 화면 다시 그리
    }
    
 // 캘린더에서 오늘,내일 일정 불러오기
    private void TTC() {   

        //  기존 내용 초기화
        todoToday.clear();
        todoTomorrow.clear();

        //  오늘 / 내일 날짜 계산
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);   // (Calendar, DAY_OF_MONTH)

        //  캘린더의 키 포맷으로 문자열 생성 (yyyy-MM-dd)
        String todayKey = dateKeyFormat.format(today.getTime());
        String tomorrowKey = dateKeyFormat.format(tomorrow.getTime());

        //  CalendarPanel 에서 scheduleData 꺼내오기
        Map<String, Vector<ScheduleItem>> data = calendarPanel.getScheduleData();


     // 오늘 일정 Todo 모델에 추가
        Vector<ScheduleItem> todaySchedules = data.get(todayKey);
        if (todaySchedules != null) {
        	 for (ScheduleItem s : todaySchedules) {
                 long groupId   = s.getId();          // calendars 쪽에서 이미 세팅해둔 값
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

    
    // 상단 Tab
    private JComponent buildHomeCard() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel todotab =buildTodoMainTab();
        JPanel routinetab = buildRoutineTab();
        tabs.addTab("할일", todotab);
        tabs.addTab("루틴", routinetab);
        return tabs;
    }
    
    private JPanel buildTodoMainTab() {
       
       JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(Color.white);
        
       JPanel todoTab=new JPanel(new GridLayout(1,2,16,0));
       todoTab.setBackground(Color.white);
       JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        topBar.setBackground(Color.white);

        JButton btnRefresh = new JButton("캘린더에서 다시 불러오기");
        JButton btnMove = new JButton("미완료 내일넘기기");

        topBar.add(btnRefresh);
        topBar.add(btnMove);
        
        root.add(topBar,BorderLayout.NORTH);
       
       
         // 오늘 내일 날짜 계산
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
       
        // 날짜 초기화
        SimpleDateFormat labelFormat = new SimpleDateFormat("yyyy-MM-dd");

        String todayLabel = "오늘 할 일 (" + labelFormat.format(today.getTime()) + ")";
        String tomorrowLabel = "내일 할 일 (" + labelFormat.format(tomorrow.getTime()) + ")";

        JPanel todayPanel = buildTodoTab(todayLabel, todoListToday, todoToday);
        JPanel tomorrowPanel = buildTodoTab(tomorrowLabel, todoListTomorrow, todoTomorrow);
        
        todoTab.add(todayPanel);
        todoTab.add(tomorrowPanel);
        
        root.add(todoTab,BorderLayout.CENTER);
       
        root.add(todoTab,BorderLayout.NORTH);
        root.add(todoTab,BorderLayout.CENTER);
        

        // 오늘 미완료 내일로 넘기기
        btnRefresh.addActionListener(e -> refreshFromCalendar());
        btnMove.addActionListener(e -> {
            moveUnfinishedToTomorrow();
            // 필요하면 repaint();
            repaint();
        });

        return root;
        
    }
    
    // Todo Tab
    private JPanel buildTodoTab(String titleText,
            JList<TodoItem> list,
            DefaultListModel<TodoItem> model){
       JPanel todopanel= new JPanel(new BorderLayout(10,10));
       todopanel.setBackground(Color.white);
       
       JLabel title=new JLabel(titleText);
       title.setFont(title.getFont().deriveFont(Font.BOLD,16f));
       todopanel.add(title,BorderLayout.NORTH);
       
       JScrollPane scrollPane=new JScrollPane(list);
       todopanel.add(scrollPane,BorderLayout.CENTER);
        
        return todopanel;}
    
   // Todo완료
    private void addTodoToggleListener(JList<TodoItem> list, DefaultListModel<TodoItem> model) {
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int range=list.locationToIndex(e.getPoint());
                if(range<0)return; // 밖 클릭
                
                Rectangle cellBounds = list.getCellBounds(range, range);
                if (cellBounds == null) return;
               
                int rel = e.getX() - cellBounds.x;
                boolean inCheckBox=(rel>=0&&rel<=25);
                
                if(inCheckBox && e.getClickCount()==1) {
                   TodoItem item=model.get(range);
                   item.done=!item.done; // 메모리 상태 토글
                   updateStatusInDB(item.groupId, item.done); //DB 업데이트
                   list.repaint();
                }
                else if (!inCheckBox && e.getClickCount() == 2) {
                    TodoItem item = model.get(range);
                    Window owner = SwingUtilities.getWindowAncestor(TodoPanel.this);
                    TodoDetailDialog dialog = new TodoDetailDialog(owner, item, list);
                    dialog.setVisible(true);
                }

              
 
            }
        });
    }
    
    // in루틴 탭
    private JPanel buildRoutineTab() {
       JPanel routineTab=new JPanel(new BorderLayout(10,10));
       routineTab.setBackground(Color.white);
       
       JLabel title=new JLabel("루틴");
       title.setFont(title.getFont().deriveFont(Font.BOLD,16f));
       routineTab.add(title,BorderLayout.NORTH);
       
       JScrollPane scroll=new JScrollPane(routineList);
       routineTab.add(scroll,BorderLayout.CENTER);
       
       JPanel input=new JPanel(new BorderLayout(5,5));
       JTextField inputField= new JTextField();
       JButton addButton=new JButton("추가");
       JButton deleteButton=new JButton("삭제");
       JButton skipButton=new JButton("스킵");
       JButton viewSkipButton=new JButton("스킵 목록");
       
       input.add(inputField,BorderLayout.CENTER);
       
       JPanel btnPanel=new JPanel(new GridLayout(1,4,5,0));
       btnPanel.add(addButton);
       btnPanel.add(deleteButton);
       btnPanel.add(skipButton);
       btnPanel.add(viewSkipButton);
       input.add(btnPanel,BorderLayout.EAST);
       routineTab.add(input,BorderLayout.SOUTH);
       

       addButton.addActionListener(e -> {
    	    String text = inputField.getText().trim();
    	    if (!text.isEmpty()) {
    	    	Window owner=SwingUtilities.getWindowAncestor(TodoPanel.this);
    	    	RoutineDetailDialog dialog=new RoutineDetailDialog(owner, text, null);
    	    	
    	    	dialog.setVisible(true);
    	    	
    	    	if(dialog.isConfirmed()) {
    	    		String startTime=dialog.getStartTime();
    	    	
    	        insertRoutineToDB(text);   //DB에 먼저 저장
    	        
    	        RoutineItem r=new RoutineItem(text);
    	        routineModel.addElement(r);
    	        todoToday.addElement(new TodoItem(text, false, false, startTime, null, "루틴"));

    	        inputField.setText("");
    	        refreshROutineColorsInTodo();
    	    	}
    	    }
    	});

        
        deleteButton.addActionListener(e->{
           int idx=routineList.getSelectedIndex();
           if(idx>=0) {
              routineModel.remove(idx);
              refreshROutineColorsInTodo(); 
           }
        });
        skipButton.addActionListener(e -> {
            int idx = routineList.getSelectedIndex();
            if (idx >= 0) {
                RoutineItem item = routineModel.get(idx);

                // 스킵 상태로 만들고
                item.state = RoutineState.SKIP;

                // 메인 루틴 목록에서 제거
                routineModel.remove(idx);

                // 스킵 전용 목록에 추가
                skippedRoutineModel.addElement(item);

                routineList.repaint();
                refreshROutineColorsInTodo(); 
            }
        });
     // SKIP만 모아서 보여주기
        viewSkipButton.addActionListener(e -> {
            JList<RoutineItem> skipList = new JList<>(skippedRoutineModel);
               skipList.setCellRenderer(new RoutineCellRenderer());

               JScrollPane skipScroll = new JScrollPane(skipList);
               
               Object[] options= {"되돌리기","닫기"};
               int result = JOptionPane.showOptionDialog(
                       routineTab,
                       skipScroll,
                       "스킵한 루틴 목록",
                       JOptionPane.DEFAULT_OPTION,
                       JOptionPane.PLAIN_MESSAGE,
                       null,
                       options,
                       options[1]   // 기본 선택: 닫기
               );
               // "되돌리기"를 눌렀을 때
               if (result == 0) {
                   int sel = skipList.getSelectedIndex();
                   if (sel >= 0) {
                       // 선택된 스킵 항목 가져오기
                       RoutineItem item = skippedRoutineModel.get(sel);

                       // 스킵 목록에서 제거
                       skippedRoutineModel.remove(sel);

                       // 상태를 TODO로 되돌리고
                       item.state = RoutineState.TODO;

                       // 메인 루틴 목록에 다시 추가
                       routineModel.addElement(item);

                       // 화면 갱신
                       routineList.repaint();
                       refreshROutineColorsInTodo();
                   } else {
                       // 아무것도 선택 안 했는데 되돌리기 누르면 안내만 (선택사항)
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
    // 루틴상태 변경리스너
    private void addRoutineStateListener() {
        routineList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = routineList.locationToIndex(e.getPoint());
                if (idx < 0) return;

                Rectangle cellBounds = routineList.getCellBounds(idx, idx);
                if (cellBounds == null) return;

                int relX = e.getX() - cellBounds.x;
                boolean inCheckBox = (relX >= 0 && relX <= 25); // 체크박스 가로 범위 대충 0~25

                if (inCheckBox && e.getClickCount() == 1) {
                    RoutineItem item = routineModel.get(idx);
                    
                    

                    // SKIP이면 체크박스 건들 x
                    if (item.state == RoutineState.SKIP) return;

                   
                    if (item.state == RoutineState.DONE) {
                        item.state = RoutineState.TODO;
                    } else {
                        item.state = RoutineState.DONE;
                    }

                    routineList.repaint(); refreshROutineColorsInTodo();
                }
                else if(e.getClickCount()==2) {
                	 RoutineItem item = routineModel.get(idx);

                     Window owner = SwingUtilities.getWindowAncestor(TodoPanel.this);
                     RoutineDetailDialog dialog =
                             new RoutineDetailDialog(owner, item.text, item.startTime);

                     dialog.setVisible(true);

                     if (dialog.isConfirmed()) {
                         item.startTime = dialog.getStartTime();
                         routineList.repaint();
                } 
            }
            }});
    }

    // 미완료 다음날로
    public void moveUnfinishedToTomorrow() {
        // 뒤에서부터 지우기 (인덱스 꼬임 방지)
        for (int i = todoToday.size() - 1; i >= 0; i--) {
            TodoItem item = todoToday.get(i);
            if (!item.done) {            // 완료 안 된 것만
                item.over = true;     // 밀린 할 일 표시
                todoTomorrow.addElement(item); // 내일 리스트로 옮기고
                todoToday.remove(i);           // 오늘에서는 삭제
            }
        }
    }
    private void refreshROutineColorsInTodo() {
    	
    	todoListToday.repaint();
    	todoListTomorrow.repaint();}
    
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
                    // DB에서 읽어온 루틴을 리스트에 추가
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
        String sql = "INSERT INTO routines (user_id, title,repeat_rule) VALUES (?,?,?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, USER_ID);
            ps.setString(2, title);
            ps.setString(3,"none");

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // TODOITEM 이 루틴에 해당하는지 확인
    private boolean isRoutineTodo(TodoItem item) {
    	if(item==null)return false;
    	String text=item.text;
    	for(int i=0;i<routineModel.size();i++) {
    		RoutineItem r=routineModel.get(i);
    		if(r.state!=RoutineState.SKIP && text.equals(r.text)) {
    			return true;
    		}
    	}
    	return false;
    	
    }
    
    
    // DB에서 현재 상태 DONE인지 확인
 //  DB에서 현재 status가 DONE인지 확인
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

    // 체크박스 눌렀을떄 DB 업데이트
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

    
    
    
private  class TodoCellRenderer extends JPanel implements ListCellRenderer<TodoItem>{
    private final JCheckBox checkBox;
       private final JLabel textLabel;

       public TodoCellRenderer() {
           setLayout(new BorderLayout(5, 0));
           setOpaque(true);

           checkBox = new JCheckBox();
           checkBox.setOpaque(false); // 배경은 패널이 담당

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

               // 텍스트
               textLabel.setText(value.text);

               // 기본 색
               Color fg = Color.BLACK;
               Color bg = list.getBackground();

               // 완료 / 밀린 할 일 색
               if (value.over) {
                   fg = Color.RED;                // 밀린 할 일: 빨강
               }

               // 루틴 Todo면 배경을 연한 파랑으로
               if (isRoutineTodo(value)) {
                   fg = new Color(0, 255,0 ); // 루틴: 연한 하늘색
               }

               // 선택 상태 처리
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
        checkBox.setOpaque(false); // (상태 유지는 할 수 있지만 화면에는 안 씀)

        textLabel = new JLabel();

        // [변경] 체크박스를 패널에 붙이지 않는다
        // add(checkBox, BorderLayout.WEST);
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
            // 체크박스: DONE이면 체크, 아니면 해제
            checkBox.setSelected(value.state == RoutineState.DONE);

            // 텍스트
            textLabel.setText(value.text);

            // 색상: SKIP만 회색, 나머지는 기본색
            if (value.state == RoutineState.SKIP) {
                textLabel.setForeground(Color.GRAY);
            } else {
                textLabel.setForeground(Color.BLACK);
            }
        }

        // 리스트 선택 색상 반영
        if (isSelected) {
            setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
        }

        return this;
    }
}
}
    
            
    
    



