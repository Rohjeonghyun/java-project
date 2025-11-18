package main;

import javax.swing.*;
import java.awt.*;
import todo.TodoPanel;
import calendars.CalendarPanel;
import mypage.MypagePanel;



public class TestFile extends JFrame {

    public TestFile() {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Todo",     new TodoPanel());
        tabs.addTab("Calendar", new CalendarPanel());
        tabs.addTab("MyPage",   new MypagePanel());

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
