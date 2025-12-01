package Mypage;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.sql.*;

import database.DBConnection;

/**
* 일기 알림 설정 화면
*  알림 사용 여부 + 시간 설정
*  현재 DB 연동 저장/불러오기 가능
*/
public class ReminderFrame extends JFrame {

    private JCheckBox cbEnableDailyReminder;
    private JSpinner spHour;
    private JSpinner spMinute;

    private long userId;

    public ReminderFrame(Component owner) {
        this(owner, 1L);//임의유저아이디값
    }

    //생성자=>로그인 구현 시 userId 전달 필요성 있음
    public ReminderFrame(Component owner, long userId) {
        this.userId = userId;

        setTitle("리마인더 설정");
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("리마인더 설정"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        cbEnableDailyReminder = new JCheckBox("일기 알림 설정");
        cbEnableDailyReminder.setAlignmentX(Component.CENTER_ALIGNMENT);
        cbEnableDailyReminder.setBackground(Color.WHITE);
        cbEnableDailyReminder.setFont(new Font("맑은고딕", Font.BOLD, 14));

        JPanel timePanel = new JPanel();
        timePanel.setBackground(Color.WHITE);

        spHour = new JSpinner(new SpinnerNumberModel(22, 0, 23, 1));
        spMinute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        spHour.setPreferredSize(new Dimension(45, 30));
        spMinute.setPreferredSize(new Dimension(45, 30));

        spHour.setFont(new Font("맑은고딕", Font.BOLD, 18));
        spMinute.setFont(new Font("맑은고딕", Font.BOLD, 18));

        timePanel.add(spHour);
        timePanel.add(new JLabel(":"));
        timePanel.add(spMinute);

        JButton btnSave = new JButton("저장");
        btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSave.addActionListener(e -> saveReminder());

        JButton btnClose = new JButton("닫기");
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(e -> dispose());

        panel.add(Box.createVerticalStrut(20));
        panel.add(cbEnableDailyReminder);
        panel.add(Box.createVerticalStrut(10));
        panel.add(timePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnSave);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnClose);
        panel.add(Box.createVerticalGlue());

        add(panel);

        //창 열림 기점으로 기존 설정 불러옴
        loadReminderFromDB();
    }

    //DB기존 리마인더 설정 불러옴
    private void loadReminderFromDB() {
        String sql = "SELECT enabled, remind_time FROM reminder_settings WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean enabled = rs.getBoolean("enabled");
                    cbEnableDailyReminder.setSelected(enabled);

                    Time t = rs.getTime("remind_time");
                    if (t != null) {
                        int hour = t.toLocalTime().getHour();
                        int min  = t.toLocalTime().getMinute();
                        spHour.setValue(hour);
                        spMinute.setValue(min);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //DB 리마인더 설정 저장
    private void saveReminder() {
        boolean enabled = cbEnableDailyReminder.isSelected();
        int hour = (Integer) spHour.getValue();
        int min  = (Integer) spMinute.getValue();
        
        String sql =
            "INSERT INTO reminder_settings (user_id, enabled, remind_time) " +
            "VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "enabled = VALUES(enabled), remind_time = VALUES(remind_time)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setBoolean(2, enabled);

            //0시:0분:0초
            String timeStr = String.format("%02d:%02d:00", hour, min);
            ps.setTime(3, java.sql.Time.valueOf(timeStr));

            ps.executeUpdate();

            String enabledMsg = enabled ? "켜짐" : "꺼짐";
            
            JOptionPane.showMessageDialog(this,
                "리마인더 설정이 저장되었습니다.\n" +
                "사용 여부: " + enabledMsg + "\n" +
                "시간: " + hour + ":" + String.format("%02d", min));

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "저장 중 오류 발생:\n" + ex.getMessage(),
                "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }
}
