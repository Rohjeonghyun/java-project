package Mypage;
//EditProfileFrame.java
import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
* 프로필 수정 화면
*  - 이름, 생일 등 기본 정보 수정
*  - 현재는 DB 없이 입력값만 확인
*/
public class EditProfileFrame extends JFrame {

private JTextField txtUserName;
private JTextField txtBirthDate;

public EditProfileFrame(JFrame owner) {
   setTitle("Edit Profile");
   setSize(400, 250);
   setLocationRelativeTo(owner);
   setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

   JPanel panel = new JPanel(new GridBagLayout());
   panel.setBorder(new TitledBorder("Edit Profile"));
   panel.setBackground(Color.WHITE);

   GridBagConstraints c = new GridBagConstraints();
   c.insets = new Insets(5, 5, 5, 5);
   c.anchor = GridBagConstraints.WEST;

   c.gridx = 0; c.gridy = 0;
   panel.add(new JLabel("User Name:"), c);
   c.gridx = 1;
   txtUserName = new JTextField("Tester", 15); // 예시
   panel.add(txtUserName, c);

   c.gridx = 0; c.gridy = 1;
   panel.add(new JLabel("Birth date:"), c);
   c.gridx = 1;
   txtBirthDate = new JTextField("2000-01-01", 15); // 예시
   panel.add(txtBirthDate, c);

   JPanel bottom = new JPanel();
   JButton btnSave = new JButton("Save");
   btnSave.addActionListener(e -> saveProfile());
   JButton btnClose = new JButton("Close");
   btnClose.addActionListener(e -> dispose());
   bottom.add(btnSave);
   bottom.add(btnClose);

   getContentPane().setLayout(new BorderLayout());
   add(panel, BorderLayout.CENTER);
   add(bottom, BorderLayout.SOUTH);
}

private void saveProfile() {
   String name = txtUserName.getText();
   String birth = txtBirthDate.getText();

   // TODO: users 테이블 UPDATE
   JOptionPane.showMessageDialog(this,
           "프로필 저장(예시)\nName: " + name + "\nBirth: " + birth);
}
}
