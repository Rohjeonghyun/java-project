package main;

import java.awt.BorderLayout;
import java.util.Vector; // [NEW] Vector import

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import Mypage.MypagePanel;
import calendars.CalendarPanel;
import diary.diary;
import todo.TodoPanel;

public class TestFile extends JFrame {

    public TestFile() {
        // (DB 연동 전 임시 데이터)
        Vector<String> categories = new Vector<>();
        categories.add("학교");
        categories.add("팀프로젝트");
        categories.add("운동");
        categories.add("개인");

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Todo",     new TodoPanel());
        tabs.addTab("Calendar", new CalendarPanel(categories)); 
        tabs.addTab("MyPage",   new MypagePanel());
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