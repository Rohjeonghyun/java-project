package main;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import Mypage.MyPageFrame;
import calendars.CalendarPanel;
import todo.TodoPanel;

public class TestFile extends JFrame {

    public TestFile() {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Todo",     new TodoPanel());
        tabs.addTab("Calendar", new CalendarPanel());
        tabs.addTab("My Page",  new MyPageFrame());   // ★ JPanel로 변경된 마이페이지

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
