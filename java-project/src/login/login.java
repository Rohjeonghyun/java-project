package login;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder; // 패딩을 위해 import
import java.awt.event.*;

// 사용되지 않는 MessageDialog 클래스 삭제

class NewMember extends JFrame implements ActionListener {
    JTextField id, name;
    JPasswordField passwd;
    JButton b1, b2;

    NewMember (String title) {
        setTitle(title);
        Container ct = getContentPane();
        // 1. BorderLayout과 패딩(EmptyBorder)을 사용해 여백 추가
        ct.setLayout(new BorderLayout(10, 10)); 
        ((JPanel)ct).setBorder(new EmptyBorder(10, 10, 10, 10)); // 상하좌우 10픽셀 여백

        // 2. GridLayout(3, 2)로 변경하여 레이블과 필드를 깔끔하게 정렬
        JPanel top = new JPanel(new GridLayout(3, 2, 5, 5)); // 3행 2열, 간격 5
        
        top.add(new JLabel("ID :", SwingConstants.RIGHT));
        id = new JTextField (15); 
        top.add(id);

        top.add(new JLabel("PASSWORD:", SwingConstants.RIGHT));
        passwd = new JPasswordField (15); 
        top.add(passwd);

        top.add(new JLabel("이름 :", SwingConstants.RIGHT));
        name = new JTextField (15); 
        top.add(name);
        
        ct.add(top, BorderLayout.CENTER);

        // 하단 버튼 패널 (기존과 동일하게 유지)
        JPanel bottom = new JPanel(); 
        b1 = new JButton("확인 (가입)"); 
        b2 = new JButton("취소");
        b1.addActionListener(this);
        b2.addActionListener(this);
        bottom.add(b1); 
        bottom.add(b2); 
        ct.add(bottom, BorderLayout.SOUTH); 

        // 창 크기를 내용물에 맞게 자동 조절
        pack();
    }

    public void actionPerformed(ActionEvent ae) { 
        String s = ae.getActionCommand(); 
        if (s.equals("취소")) {
            id.setText(""); 
            passwd.setText(""); 
            name.setText("");
            dispose(); // 창 닫기
        } 
        else if (s.equals("확인 (가입)")) {
            String inputId = id.getText();
            String inputPasswd = new String(passwd.getPassword());
            String inputName = name.getText();

            // 3. trim()을 추가해 앞뒤 공백만 입력한 경우도 감지
            if (inputId.trim().isEmpty() || inputPasswd.trim().isEmpty() || inputName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID, 비밀번호, 이름을 모두 입력해야 합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            } else {
                String msg = String.format("🎉 회원가입 완료\nID: %s\n이름: %s\n(이제 DB 저장 기능을 구현하면 됩니다!)", inputId, inputName);
                JOptionPane.showMessageDialog(this, msg, "회원가입 성공", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // 성공 후 창 닫기
            }
        }
    }
}

class LoginWindow extends JFrame implements ActionListener { 
    JTextField id; 
    JPasswordField passwd;
    JLabel result;
    JButton b1, b2, b3;

    LoginWindow(String title) {
        setTitle(title);
        Container ct = getContentPane();
        
        // 1. null 레이아웃 대신 BorderLayout 사용 (간격 10)
        ct.setLayout(new BorderLayout(10, 10));
        // 전체적으로 여백(padding)을 줌
        ((JPanel)ct).setBorder(new EmptyBorder(10, 10, 10, 10));

        // 2. 중앙 패널 (ID, PW 입력란) - GridLayout 사용
        JPanel pCenter = new JPanel(new GridLayout(2, 2, 5, 5)); // 2행 2열, 간격 5
        
        // 레이블을 오른쪽 정렬하여 보기 좋게 만듦
        pCenter.add(new JLabel("LoginID :", SwingConstants.RIGHT));
        id = new JTextField (8);
        pCenter.add(id);

        pCenter.add(new JLabel("PASSWORD :", SwingConstants.RIGHT));
        passwd = new JPasswordField (8);
        pCenter.add(passwd);
        
        ct.add(pCenter, BorderLayout.CENTER);

        // 3. 하단 패널 (버튼, 결과 메시지)
        // 2행 1열의 GridLayout으로 버튼 영역과 결과 영역을 나눔
        JPanel pSouth = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // 3-1. 버튼이 들어갈 패널 (FlowLayout)
        JPanel pButtons = new JPanel(new FlowLayout());
        b1 = new JButton("로그인");
        b2 = new JButton("취소");
        b3 = new JButton("회원가입");
        b1.addActionListener(this); 
        b2.addActionListener(this);
        b3.addActionListener(this);
        pButtons.add(b1); 
        pButtons.add(b2);
        pButtons.add(b3);
        
        pSouth.add(pButtons); // 하단 패널의 첫 번째 행에 버튼 패널 추가

        // 3-2. 결과 메시지 레이블 (중앙 정렬)
        result = new JLabel(" ", SwingConstants.CENTER); // 초기값은 공백
        pSouth.add(result); // 하단 패널의 두 번째 행에 결과 레이블 추가

        ct.add(pSouth, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent ae) { 
        String s = ae.getActionCommand(); 
        if (s.equals("로그인")) { 
            String userId = id.getText();
            String password = new String(passwd.getPassword()); 

            if (userId.trim().isEmpty() || password.trim().isEmpty()) {
                result.setText("ID와 PASSWORD를 모두 입력하세요.");
            } else {
                // (나중에 이곳에 DB 조회 로직 추가)
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
            my.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 이 창만 닫기
            // 4. setSize 대신 pack()으로 변경 (NewMember 생성자에서 호출)
            my.setLocationRelativeTo(this); // 로그인 창 기준으로 중앙에 표시
            my.setVisible(true);
            result.setText("회원가입 창이 열렸습니다.");
        }
    } 
}

public class login {
    
    public static void main (String args[]) {
        // 1. Swing 스레드 안정성을 위해 invokeLater 사용
        SwingUtilities.invokeLater(() -> {
            LoginWindow win = new LoginWindow("로그인"); 
            win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
            
            // 2. setSize 대신 pack()을 사용해 내용물에 맞게 창 크기 자동 설정
            win.pack(); 
            // 3. 화면 정중앙에 위치
            win.setLocationRelativeTo(null);
            win.setVisible(true);
        });
    }
}