package main;

import java.awt.BorderLayout;

import java.awt.Color;
import java.util.Vector; // [NEW] Vector import

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import Mypage.MyPageFrame;
import calendars.CalendarPanel;
import calendars.CategoryItem;
import diary.diary;
import todo.TodoPanel;

public class TestFile extends JFrame{
	
	private CalendarPanel calendarPanel;
	private TodoPanel todoPanel;

    public TestFile() {
        // (DB 연동 전 임시 데이터)
    	Vector<CategoryItem> categories = new Vector<>();
        categories.add(new CategoryItem("학교", new Color(255, 200, 200))); // 연한 빨강
        categories.add(new CategoryItem("운동", new Color(200, 200, 255))); // 연한 파랑
        categories.add(new CategoryItem("개인", new Color(255, 255, 200))); // 연한 노랑
        categories.add(new CategoryItem("팀프로젝트", new Color(200, 255, 200)));
        
        calendarPanel=new CalendarPanel(categories);
        todoPanel=new TodoPanel(calendarPanel);
        
        calendarPanel.setTodoPanel(todoPanel);
        
         // Calendar Todofh 줘버림
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Todo",     todoPanel);
        tabs.addTab("Calendar", calendarPanel); 
        tabs.addTab("MyPage",    new MyPageFrame());
        tabs.addTab("diary",   new diary());

        getContentPane().add(tabs, BorderLayout.CENTER);
        setTitle("Diary Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TestFile().setVisible(true));
    }
}