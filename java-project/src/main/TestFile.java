package main;

import javax.swing.*;

import Mypage.MyPageFrame;

import java.awt.*;
import java.util.Vector;
import todo.TodoPanel;
import calendars.CalendarDAO; // [NEW]
import calendars.CalendarPanel;
import calendars.CategoryItem;
import diary.diary;
import Mypage.MypagePanel;

public class TestFile extends JFrame {

    public TestFile() {
        // 1. DAO 생성
        CalendarDAO dao = new CalendarDAO();
        
        // 2. DB에서 카테고리 목록 불러오기
        Vector<CategoryItem> categories = dao.getCategories();
        
        // (만약 DB가 비어있다면 기본 카테고리 추가 - 선택 사항)
        if (categories.isEmpty()) {
            categories.add(new CategoryItem("기본", Color.LIGHT_GRAY));
        }

        JTabbedPane tabs = new JTabbedPane();

        // 3. UI 생성 시 DAO와 categories 전달
        // tabs.addTab("Todo",     new TodoPanel(categories)); // TodoPanel도 나중에 DAO 받도록 수정 필요
        tabs.addTab("Calendar", new CalendarPanel(categories, dao));
        tabs.addTab("MyPage",   new MyPageFrame());
        tabs.addTab("diary", new diary());

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