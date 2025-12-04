package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Vector;
import java.time.LocalTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

import Mypage.MyPageFrame;
import calendars.CalendarDAO;
import calendars.CalendarPanel;
import calendars.CategoryItem;
import diary.diary;
import login.login;
import todo.TodoPanel;
import database.DBConnection;

public class TestFile extends JFrame {
	
	private long userId;

    public TestFile(long userId) {

        CalendarDAO dao = new CalendarDAO(userId);
        this.userId = userId;
        
        Vector<CategoryItem> categories = dao.getCategories();
        if (categories.isEmpty()) {
            categories.add(new CategoryItem("기본", Color.LIGHT_GRAY));
        }
        JTabbedPane tabs = new JTabbedPane();
        CalendarPanel calendarPanel = new CalendarPanel(categories, dao);
        TodoPanel todoPanel = new TodoPanel(userId, calendarPanel, categories, dao);
        tabs.addTab("Todo",     todoPanel);
        tabs.addTab("Calendar", calendarPanel);
        tabs.addTab("MyPage",   new MyPageFrame());
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
        
     // [NEW] 리마인더 감시 시작
        startReminderChecker();
    }
    
 // [NEW] 리마인더 백그라운드 체크 서비스
    private void startReminderChecker() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    checkAndAlert(); // 시간 확인 및 알림 로직 실행
                    Thread.sleep(10000); // 10초마다 검사
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        t.setDaemon(true); // 프로그램 종료 시 스레드도 함께 종료
        t.start();
    }

    // 실제 DB 조회 및 알림 로직
    private void checkAndAlert() {
        LocalTime now = LocalTime.now();
        int curHour = now.getHour();
        int curMin = now.getMinute();

        // 1. 리마인더 설정 조회
        String settingSql = "SELECT on_off, remind_time FROM reminder_settings WHERE user_id = ?";
        // 2. 오늘 일기 작성 여부 조회
        String diaryCheckSql = "SELECT count(*) FROM diary_entries WHERE user_id = ? AND entry_date = CURDATE()";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement psSetting = con.prepareStatement(settingSql)) {

            psSetting.setLong(1, userId); // (주의) TestFile 클래스의 멤버변수 userId 사용
            
            try (ResultSet rs = psSetting.executeQuery()) {
                if (rs.next()) {
                    boolean isOn = rs.getBoolean("on_off");
                    Time dbTime = rs.getTime("remind_time");

                    // 알림이 켜져있고(ON), 시간이 설정되어 있다면
                    if (isOn && dbTime != null) {
                        LocalTime targetTime = dbTime.toLocalTime();

                        // [중요] 시(Hour)와 분(Minute)이 일치하는지 확인
                        if (targetTime.getHour() == curHour && targetTime.getMinute() == curMin) {
                            
                            // 3. 일기를 아직 안 썼는지 확인
                            try (PreparedStatement psDiary = con.prepareStatement(diaryCheckSql)) {
                                psDiary.setLong(1, userId);
                                try (ResultSet rsDiary = psDiary.executeQuery()) {
                                    // 일기 개수가 0개면 (=안 썼으면) 알림 발생
                                    if (rsDiary.next() && rsDiary.getInt(1) == 0) {
                                        
                                        // Swing UI 스레드에서 팝업 띄우기
                                        SwingUtilities.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(TestFile.this, 
                                                "오늘의 일기를 아직 작성하지 않으셨습니다.️", 
                                                "일기 작성 알림", 
                                                JOptionPane.INFORMATION_MESSAGE);
                                        });

                                        // [중요] 1분 동안은 다시 알림이 뜨지 않도록 대기 (중복 알림 방지)
                                        Thread.sleep(60000); 
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("리마인더 체크 중 오류 발생: " + e.getMessage());
        }
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