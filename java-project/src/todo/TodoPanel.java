package todo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class TodoPanel extends JPanel {

	// 오늘,내일 모델 
    private final DefaultListModel<TodoItem> todoToday = new DefaultListModel<>();
    private final JList<TodoItem> todoListToday = new JList<>(todoToday);
    private final DefaultListModel<TodoItem> todoTomorrow = new DefaultListModel<>();
    private final JList<TodoItem> todoListTomorrow = new JList<>(todoTomorrow);

    // 루틴 모델 
    private final DefaultListModel<RoutineItem> routineModel = new DefaultListModel<>();
    private final JList<RoutineItem> routineList = new JList<>(routineModel);

   // 전체
    public TodoPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.white);
        JComponent homeCard=buildHomeCard();
        add(homeCard,BorderLayout.CENTER);
    }
    private JComponent buildHomeCard() {
    	JTabbedPane tabs=new JTabbedPane();
    	tabs.setBorder(new EmptyBorder(20,20,20,20));
    	JPanel todotab=buildTodoTab();
    	JPanel routinetab=buildRoutineTab();
    	tabs.addTab("할일",todotab);
    	tabs.addTab("루틴",routinetab);
    	return tabs;
    	
    }
    
    private JPanel buildTodoTab() {
    	//틀
    	JPanel todoTab=new JPanel(new GridLayout(1,2,16,0));
    	todoTab.setBackground(Color.white);
    	// 오늘
    	JPanel today=new JPanel(new BorderLayout(10,10));
    	today.setBackground(Color.white);
    	JLabel todayTitle=new JLabel("오늘 할일");
    	todayTitle.setFont(todayTitle.getFont().deriveFont(Font.BOLD, 16f));
        today.add(todayTitle, BorderLayout.NORTH);
        today.add(new JLabel(" UI "),BorderLayout.CENTER);
        // 내일
        JPanel tommorw=new JPanel(new BorderLayout(10,10));
        tommorw.setBackground(Color.white);
        JLabel tommorowTitle=new JLabel("내일 할 일");
        tommorowTitle.setFont(tommorowTitle.getFont().deriveFont(Font.BOLD, 16f));
        tommorw.add(tommorowTitle, BorderLayout.NORTH);
        tommorw.add(new JLabel("UI "),BorderLayout.CENTER);
        
        todoTab.add(today);
        todoTab.add(tommorw);
        return todoTab;
    }
    // Routine
    private JPanel buildRoutineTab() {
    	   JPanel routineTab = new JPanel(new BorderLayout(10, 10));
           routineTab.setBackground(Color.white);

           JLabel title = new JLabel("Routine");
           title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
           routineTab.add(title, BorderLayout.NORTH);

           
           routineTab.add(new JLabel("  UI 자리 "), BorderLayout.CENTER);

           return routineTab;
    }

   
    private static class TodoItem {
        String text;    
        boolean done;   

       
        TodoItem(String t) {
            this.text = t;
            this.done = false;
        }
        @Override
        public String toString() {
            return text;
        }
    }

    private enum RoutineState {
        TODO,  
        DONE,  
        SKIP   
    }

    private static class RoutineItem {
        String text;          // 루틴 내용
        RoutineState state = RoutineState.TODO;  
        RoutineItem(String t) {
            this.text = t;
        }
        void nextState() {
            switch (state) {
                case TODO: state = RoutineState.DONE; break;
                case DONE: state = RoutineState.SKIP; break;
                case SKIP: state = RoutineState.TODO; break;
            }
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
