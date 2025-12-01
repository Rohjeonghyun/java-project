package main;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

import Mypage.MyPageFrame;
import todo.TodoPanel;
import calendars.CalendarDAO;
import calendars.CalendarPanel;
import calendars.CategoryItem;
import diary.diary;

// [수정] LoginWindow 대신 login 클래스를 import
import login.login; 

public class TestFile extends JFrame {

    // 1. 생성자: 로그인한 유저의 ID(userId)를 받습니다.
    public TestFile(long userId) {
        
        CalendarDAO dao = new CalendarDAO();
        Vector<CategoryItem> categories = dao.getCategories();
        
        if (categories.isEmpty()) {
            categories.add(new CategoryItem("기본", Color.LIGHT_GRAY));
        }

        JTabbedPane tabs = new JTabbedPane();
        
        CalendarPanel calendarPanel = new CalendarPanel(categories, dao);
        TodoPanel todoPanel = new TodoPanel(calendarPanel);

        // 2. 탭 추가
        tabs.addTab("Todo",     todoPanel);
        tabs.addTab("Calendar", calendarPanel);
        tabs.addTab("MyPage",   new MyPageFrame());
        
        // [중요] 로그인할 때 받은 userId를 diary에게 전달합니다.
        tabs.addTab("diary", new diary(userId));
        
        // 탭 변경 시 새로고침 리스너
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

    // 3. 메인 메서드: 이제 여기서 'login' 창을 먼저 띄웁니다.
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