package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import Mypage.MyPageFrame;
import calendars.CalendarDAO;
import calendars.CalendarPanel;
import calendars.CategoryItem;
import diary.diary;
import login.login;
import todo.TodoPanel; 

public class TestFile extends JFrame {

    public TestFile(long userId) {
        
        // 1. [수정] DAO 생성 시 userId 전달
        CalendarDAO dao = new CalendarDAO(userId);
        
        Vector<CategoryItem> categories = dao.getCategories();
        if (categories.isEmpty()) {
            categories.add(new CategoryItem("기본", Color.LIGHT_GRAY));
        }

        JTabbedPane tabs = new JTabbedPane();
        
        // CalendarPanel은 categories와 dao만 받으면 됨 (dao가 이미 userId를 가지고 있음)
        CalendarPanel calendarPanel = new CalendarPanel(categories, dao);
        
        // 2. [수정] TodoPanel 생성 시 userId 전달
        TodoPanel todoPanel = new TodoPanel(userId, calendarPanel, categories, dao);

        tabs.addTab("Todo",     todoPanel);
        tabs.addTab("Calendar", calendarPanel);
        // 3. [수정] MyPageFrame 생성 시 userId 전달
        tabs.addTab("MyPage",   new MyPageFrame(userId));
        tabs.addTab("diary",    new diary(userId));
        
        tabs.addChangeListener(e -> {
            JTabbedPane tp = (JTabbedPane)e.getSource();
            Component selected = tp.getSelectedComponent();
            if(selected == todoPanel) {
                todoPanel.refreshFromCalendar();
            }
        });

        getContentPane().add(tabs, BorderLayout.CENTER);
        setTitle("Diary Project - 접속 ID: " + userId);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // [수정] LoginWindow -> login 으로 클래스 이름 변경
            login win = new login("로그인"); 
            win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            win.pack();
            win.setLocationRelativeTo(null);
            win.setVisible(true);
        });
    }

}