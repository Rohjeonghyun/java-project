package test1234;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

// MessageDialog 클래스
class MessageDialog extends JDialog implements ActionListener {
    JButton ok;

    MessageDialog(JFrame parent, String title, boolean mode, String msg) {
        super(parent, title, mode); 
        setTitle(title);
        JPanel pc = new JPanel();
        JLabel label = new JLabel(msg);
        pc.add(label);
        add(pc, BorderLayout.CENTER);
        JPanel ps = new JPanel();
        ok = new JButton("OK");
        ok.addActionListener(this);
        ps.add(ok);
        add(ps, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    public void actionPerformed(ActionEvent ae) { 
        dispose(); 
    }
}

// NewMember 클래스
class NewMember extends JFrame implements ActionListener {
    JTextField id, name;
    JPasswordField passwd;
    JButton b1, b2;

    NewMember (String title) {
        setTitle(title);
        Container ct = getContentPane();
        ct.setLayout(new BorderLayout(0, 20)); 
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(3, 1, 0, 5)); 
        Dimension labelSize = new Dimension(90, 20); 
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5)); 
        JLabel l1 = new JLabel("ID       :");
        id = new JTextField (15); 
        l1.setPreferredSize(labelSize); 
        p1.add(l1); p1.add(id); 
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        JLabel l2 = new JLabel("PASSWORD:");
        passwd = new JPasswordField (15); 
        l2.setPreferredSize(labelSize); 
        p2.add(l2); p2.add(passwd);
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        JLabel l3 = new JLabel("이름       :");
        name = new JTextField (15); 
        l3.setPreferredSize(labelSize); 
        p3.add(l3); p3.add(name);
        top.add(p1); 
        top.add(p2); 
        top.add(p3); 
        ct.add(top, BorderLayout.CENTER);
        JPanel bottom = new JPanel(); 
        b1 = new JButton("확인 (가입)"); 
        b2 = new JButton("취소");
        b1.addActionListener(this);
        b2.addActionListener(this);
        bottom.add(b1); 
        bottom.add(b2); 
        ct.add(bottom, BorderLayout.SOUTH); 
    }

    public void actionPerformed(ActionEvent ae) { 
        String s = ae.getActionCommand(); 
        if (s.equals("취소")) {
            id.setText(""); 
            passwd.setText(""); 
            name.setText("");
            dispose();
        } 
        else if (s.equals("확인 (가입)")) {
            String inputId = id.getText();
            String inputPasswd = new String(passwd.getPassword());
            String inputName = name.getText();
            if (inputId.isEmpty() || inputPasswd.isEmpty() || inputName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID, 비밀번호, 이름을 모두 입력해야 합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            } else {
                String msg = String.format("🎉 회원가입 완료\nID: %s\n이름: %s\n(이제 DB 저장 기능을 구현하면 됩니다!)", inputId, inputName);
                JOptionPane.showMessageDialog(this, msg, "회원가입 성공", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        }
    }
}

// ❗️❗️❗️ 1. 클래스 이름 변경 ❗️❗️❗️
// 충돌을 피하기 위해 클래스 이름을 "Login"에서 "LoginWindow"로 변경
class LoginWindow extends JFrame implements ActionListener { 
    JTextField id; 
    JPasswordField passwd;
    JLabel result;
    JButton b1, b2, b3;

    // ❗️❗️❗️ 2. 생성자 이름 변경 ❗️❗️❗️
    // 클래스 이름이 바뀌었으므로 생성자 이름도 "LoginWindow"로 변경
    LoginWindow(String title) {
        setTitle(title);
        Container ct = getContentPane();
        ct.setLayout(null); 
        JLabel l1 = new JLabel("LoginID :");
        id = new JTextField (8);
        l1.setBounds(80, 60, 70, 30);
        id.setBounds(170, 60, 120, 30);
        ct.add(l1); 
        ct.add(id);
        JLabel l2 = new JLabel("PASSWORD :");
        passwd = new JPasswordField (8);
        l2.setBounds(80, 100, 70, 30);
        passwd.setBounds(170, 100, 120, 30);
        ct.add(l2); 
        ct.add(passwd);
        b1 = new JButton("로그인");
        b2 = new JButton("취소");
        b3 = new JButton("회원가입");
        b1.addActionListener(this); 
        b2.addActionListener(this);
        b3.addActionListener(this);
        b1.setBounds(30, 170, 80, 30);
        b2.setBounds(120, 170, 80, 30);
        b3.setBounds(210, 170, 80, 30);
        ct.add(b1); 
        ct.add(b2);
        ct.add(b3);
        result = new JLabel("");
        result.setBounds(30, 230, 250, 30); 
        ct.add(result);
    }

    public void actionPerformed(ActionEvent ae) { 
        String s = ae.getActionCommand(); 
        if (s.equals("로그인")) { 
            String userId = id.getText();
            String password = new String(passwd.getPassword()); 
            if (userId.isEmpty() || password.isEmpty()) {
                result.setText("ID와 PASSWORD를 모두 입력하세요.");
            } else {
                result.setText(userId + "님! 로그인 되었습니다. (임시 메시지)");
            }
        } 
        else if (s.equals("취소")) { 
            id.setText(""); 
            passwd.setText("");
            result.setText("취소 되었습니다.");
        } 
        else if (s.equals("회원가입")) {
            NewMember my = new NewMember("회원가입");
            my.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
            my.setSize(380, 200); 
            my.setLocationRelativeTo(this);
            my.setVisible(true);
            result.setText("회원가입 창이 열렸습니다.");
        }
    } 
}

// ❗️❗️❗️ 3. 실행 클래스 ❗️❗️❗️
// 파일 이름이 login.java (소문자 l)이므로, public class 이름은 login (소문자 l)이 맞습니다.
public class login {
    
    public static void main (String args[]) {
        // 
        // ❗️❗️❗️ 4. 호출 이름 변경 ❗️❗️❗️
        // "Login" 대신 새로 바꾼 "LoginWindow"를 생성합니다.
        //
        LoginWindow win = new LoginWindow("로그인"); 
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        win.setSize(320, 300);
        win.setLocation(100, 200);
        win.setVisible(true);
    }
}