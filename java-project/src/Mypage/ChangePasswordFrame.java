package Mypage;
//ChangePasswordFrame.java
import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
* 비밀번호 변경 화면
*  - 현재 비밀번호, 새 비밀번호 입력
*  - Show 체크박스로 비밀번호 표시/숨기기
*  - 현재는 DB 없이 UI만 동작
*/
public class ChangePasswordFrame extends JFrame {

private JPasswordField txtCurrentPw;
private JPasswordField txtNewPw;
private JCheckBox cbShowCurrentPw;
private JCheckBox cbShowNewPw;
private char defaultEchoChar;

public ChangePasswordFrame(JFrame owner) {
   setTitle("Change Password");
   setSize(450, 250);
   setLocationRelativeTo(owner);
   setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

   JPanel panel = new JPanel(new GridBagLayout());
   panel.setBorder(new TitledBorder("Change Password"));
   panel.setBackground(Color.WHITE);

   GridBagConstraints c = new GridBagConstraints();
   c.insets = new Insets(5, 5, 5, 5);
   c.anchor = GridBagConstraints.WEST;

   c.gridx = 0; c.gridy = 0;
   panel.add(new JLabel("Current Password:"), c);
   c.gridx = 1;
   txtCurrentPw = new JPasswordField(12);
   panel.add(txtCurrentPw, c);
   c.gridx = 2;
   cbShowCurrentPw = new JCheckBox("Show");
   cbShowCurrentPw.setBackground(Color.WHITE);
   panel.add(cbShowCurrentPw, c);

   c.gridx = 0; c.gridy = 1;
   panel.add(new JLabel("New Password:"), c);
   c.gridx = 1;
   txtNewPw = new JPasswordField(12);
   panel.add(txtNewPw, c);
   c.gridx = 2;
   cbShowNewPw = new JCheckBox("Show");
   cbShowNewPw.setBackground(Color.WHITE);
   panel.add(cbShowNewPw, c);

   defaultEchoChar = txtCurrentPw.getEchoChar();

   // 체크박스 이벤트
   cbShowCurrentPw.addActionListener(e -> {
       txtCurrentPw.setEchoChar(
               cbShowCurrentPw.isSelected() ? (char) 0 : defaultEchoChar);
   });
   cbShowNewPw.addActionListener(e -> {
       txtNewPw.setEchoChar(
               cbShowNewPw.isSelected() ? (char) 0 : defaultEchoChar);
   });

   JPanel bottom = new JPanel();
   JButton btnSave = new JButton("Save");
   btnSave.addActionListener(e -> changePassword());
   JButton btnClose = new JButton("Close");
   btnClose.addActionListener(e -> dispose());
   bottom.add(btnSave);
   bottom.add(btnClose);

   getContentPane().setLayout(new BorderLayout());
   add(panel, BorderLayout.CENTER);
   add(bottom, BorderLayout.SOUTH);
}

private void changePassword() {
   String cur = new String(txtCurrentPw.getPassword());
   String nw = new String(txtNewPw.getPassword());

   // TODO:
   //   1) DB에서 현재 비밀번호 검증
   //   2) 맞으면 새 비밀번호로 UPDATE
   JOptionPane.showMessageDialog(this,
           "비밀번호 변경 시도(예시)\n현재: " + cur + "\n새 비밀번호: " + nw);
}
}
