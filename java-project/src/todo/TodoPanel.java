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




public class TodoPanel extends JPanel {
   
    // 오늘,내일 모델
    private final DefaultListModel<TodoItem> todoToday = new DefaultListModel<>();
    private final JList<TodoItem> todoListToday = new JList<>(todoToday);
    private final DefaultListModel<TodoItem> todoTomorrow = new DefaultListModel<>();
    private final JList<TodoItem> todoListTomorrow = new JList<>(todoTomorrow);

    // 루틴 모델
    private final DefaultListModel<RoutineItem> routineModel = new DefaultListModel<>();
    private final JList<RoutineItem> routineList = new JList<>(routineModel);
    
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

        // 오늘 일정  Todo 모델에 추가
        Vector<ScheduleItem> todaySchedules = data.get(todayKey);
        if (todaySchedules != null) {
            for (ScheduleItem s : todaySchedules) {
                String text = s.getStartTime() + " " + s.getTitle();
                todoToday.addElement(new TodoItem(text));
            }
        }

        // 6) 내일 일정 → 내일 Todo 모델에 추가
        Vector<ScheduleItem> tomorrowSchedules = data.get(tomorrowKey);
        if (tomorrowSchedules != null) {
            for (ScheduleItem s : tomorrowSchedules) {
                String text = s.getStartTime() + " " + s.getTitle();
                todoTomorrow.addElement(new TodoItem(text));
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
       
        root.add(todoTab,BorderLayout.NORTH);
        root.add(todoTab,BorderLayout.CENTER);
        btnRefresh.addActionListener(e -> refreshFromCalendar());

        // 오늘 미완료 → 내일로 넘기기
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
                	item.done=!item.done;
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
       
       input.add(inputField,BorderLayout.CENTER);
       
       JPanel btnPanel=new JPanel(new GridLayout(1,2,5,0));
       btnPanel.add(addButton);
       btnPanel.add(deleteButton);
       input.add(btnPanel,BorderLayout.EAST);
       routineTab.add(input,BorderLayout.SOUTH);
       

        addButton.addActionListener(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                routineModel.addElement(new RoutineItem(text));
                inputField.setText("");
            }
        });
        
        deleteButton.addActionListener(e->{
        	int idx=routineList.getSelectedIndex();
        	if(idx>=0) {
        		routineModel.remove(idx);
        	}
        });

        return routineTab;
       }
    // 루틴상태 변경리스너
    private void addRoutineStateListener() {
       routineList.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
             int idx=routineList.locationToIndex(e.getPoint());
             if(idx>=0) {
                RoutineItem item=routineModel.get(idx);
                item.nextState();
                    routineList.repaint();
             }
          }
          
       });
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
    
private static class TodoCellRenderer extends JPanel implements ListCellRenderer<TodoItem>{
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

	            // 색상 (완료/밀림/일반)
	            if (value.done) {
	                textLabel.setForeground(new Color(0, 150, 0)); // 완료: 초록
	            } else if (value.over) {
	                textLabel.setForeground(Color.RED);           // 밀린 할 일
	            } else {
	                textLabel.setForeground(Color.BLACK);         // 일반
	            }
	        }

	        // 리스트 선택 색상 반영
	        if (isSelected) {
	            setBackground(list.getSelectionBackground());
	            textLabel.setForeground(list.getSelectionForeground());
	        } else {
	            setBackground(list.getBackground());
	        }

	        return this;
	    }
}
    
    
    private static class RoutineCellRenderer extends DefaultListCellRenderer{
       @Override
       public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            if (value instanceof RoutineItem) {
                RoutineItem item = (RoutineItem) value;

                // 텍스트는 루틴 내용만
                label.setText(item.text);
                
                if (item.state == RoutineState.TODO) {
                    label.setForeground(Color.RED);      // 미완료 
                } else if (item.state == RoutineState.DONE) {
                    label.setForeground(new Color(0,255,0)); // 완료 
                } else if (item.state == RoutineState.SKIP) {
                    label.setForeground(Color.GRAY);     // 건너뜀 = 회색
                }
            }
            return label;
       }
    }
}
    
            
    
    



