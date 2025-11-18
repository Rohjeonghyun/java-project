package Mypage;
//ReminderFrame.java
import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
* Reminder Settings 화면
*  - 알림 사용 여부 + 시간 설정
*  - 현재는 DB 없이 JOptionPane으로만 확인
*/
public class ReminderFrame extends JFrame {

private JCheckBox cbEnableDailyReminder;
private JSpinner spHour;
private JSpinner spMinute;

public ReminderFrame(Component owner) {
   setTitle("Reminder Settings");
   setSize(400, 300);
   setLocationRelativeTo(owner);
   setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

   JPanel panel = new JPanel();
   panel.setBorder(new TitledBorder("Reminder Settings"));
   panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
   panel.setBackground(Color.WHITE);

   cbEnableDailyReminder = new JCheckBox("Enable daily reminder");
   cbEnableDailyReminder.setAlignmentX(Component.CENTER_ALIGNMENT);
   cbEnableDailyReminder.setBackground(Color.WHITE);

   JPanel timePanel = new JPanel();
   timePanel.setBackground(Color.WHITE);
   spHour = new JSpinner(new SpinnerNumberModel(22, 0, 23, 1));
   spMinute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
   timePanel.add(spHour);
   timePanel.add(new JLabel(":"));
   timePanel.add(spMinute);

   JButton btnSave = new JButton("Save");
   btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
   btnSave.addActionListener(e -> saveReminder());

   JButton btnClose = new JButton("Close");
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
}

private void saveReminder() {
   boolean enabled = cbEnableDailyReminder.isSelected();
   int hour = (Integer) spHour.getValue();
   int min = (Integer) spMinute.getValue();

   // TODO: reminder_settings 테이블에 INSERT/UPDATE
   JOptionPane.showMessageDialog(this,
           "Reminder 설정 저장(예시)\n사용 여부: " + enabled +
                   "\n시간: " + hour + ":" + String.format("%02d", min));
}
}
