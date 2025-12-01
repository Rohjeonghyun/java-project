package login;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.sql.*; 

import database.DBConnection; 
import main.TestFile; 

// [수정] 회원가입 창 : 이제 DB에 진짜로 저장합니다!
class NewMember extends JFrame implements ActionListener {
    JTextField idField, nameField; // 변수명 명확하게 변경 (idField: 로그인ID, nameField: 실명)
    JPasswordField passwdField;
    JButton b1, b2;

    NewMember(String title) {
        setTitle(title);
        Container ct = getContentPane();
        ct.setLayout(new BorderLayout(10, 10)); 
        ((JPanel)ct).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridLayout(3, 2, 5, 5));
        
        // 1. 로그인에 사용할 ID 입력
        top.add(new JLabel("ID (로그인용) :", SwingConstants.RIGHT));
        idField = new JTextField(15); 
        top.add(idField);

        // 2. 비밀번호 입력
        top.add(new JLabel("PASSWORD :", SwingConstants.RIGHT));
        passwdField = new JPasswordField(15); 
        top.add(passwdField);

        // 3. 사용자 실명 입력 (DB 스키마상 저장할 곳이 마땅치 않으면 일단 입력만 받음)
        top.add(new JLabel("이름 (실명) :", SwingConstants.RIGHT));
        nameField = new JTextField(15); 
        top.add(nameField);
        
        ct.add(top, BorderLayout.CENTER);

        JPanel bottom = new JPanel(); 
        b1 = new JButton("확인 (가입)"); 
        b2 = new JButton("취소");
        
        b1.addActionListener(this);
        b2.addActionListener(this);
        
        bottom.add(b1); 
        bottom.add(b2); 
        ct.add(bottom, BorderLayout.SOUTH); 

        pack(); 
    }

    public void actionPerformed(ActionEvent ae) { 
        String s = ae.getActionCommand(); 
        
        if (s.equals("취소")) {
            dispose(); 
        } 
        else if (s.equals("확인 (가입)")) {
            // ★ [핵심] DB 저장 로직 시작
            registerUser();
        }
    }

    // 회원가입 DB 저장 메서드
    private void registerUser() {
        String inputId = idField.getText().trim();
        String inputPw = new String(passwdField.getPassword()).trim();
        String inputName = nameField.getText().trim();

        if (inputId.isEmpty() || inputPw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID와 비밀번호는 필수입니다.");
            return;
        }

        // DB 스키마에 맞춰서 저장
        // 현재 DB 테이블(users) 구조: id(자동증가), name, password_hash
        // 주의: LoginWindow에서는 'name' 컬럼을 아이디처럼 쓰고 있습니다.
        // 따라서 여기서 입력받은 'inputId'를 DB의 'name' 컬럼에 저장해야 로그인이 됩니다.
        String sql = "INSERT INTO users (name, password_hash) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, inputId); // 로그인 ID를 name 컬럼에 저장
            pstmt.setString(2, inputPw); // 비밀번호 저장
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "🎉 회원가입 성공!\n이제 로그인 해주세요.");
                dispose(); // 가입 성공하면 창 닫기
            } else {
                JOptionPane.showMessageDialog(this, "가입 실패");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "이미 존재하는 ID입니다.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 에러: " + e.getMessage());
        }
    }
}

// 로그인 창 클래스 (기존 로직 유지)
public class login extends JFrame implements ActionListener { 
    JTextField id; 
    JPasswordField passwd;
    JLabel result;
    JButton b1, b2, b3;

    public login(String title) {
        setTitle(title);
        Container ct = getContentPane();
        ct.setLayout(new BorderLayout(10, 10));
        ((JPanel)ct).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pCenter = new JPanel(new GridLayout(2, 2, 5, 5));
        pCenter.add(new JLabel("ID :", SwingConstants.RIGHT));
        id = new JTextField(8);
        pCenter.add(id);
        
        pCenter.add(new JLabel("PW :", SwingConstants.RIGHT));
        passwd = new JPasswordField(8);
        pCenter.add(passwd);
        ct.add(pCenter, BorderLayout.CENTER);

        JPanel pSouth = new JPanel(new GridLayout(2, 1, 5, 5));
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
        pSouth.add(pButtons);

        result = new JLabel(" ", SwingConstants.CENTER);
        pSouth.add(result);
        ct.add(pSouth, BorderLayout.SOUTH);
        
        passwd.addActionListener(e -> checkLoginLogic()); 
    }

    public void actionPerformed(ActionEvent ae) { 
        String s = ae.getActionCommand(); 
        
        if (s.equals("로그인")) { 
            checkLoginLogic();
        } 
        else if (s.equals("취소")) { 
            id.setText(""); 
            passwd.setText("");
            result.setText("취소 되었습니다.");
        } 
        else if (s.equals("회원가입")) {
            // 회원가입 창 열기
            NewMember my = new NewMember("회원가입");
            my.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            my.setLocationRelativeTo(this);
            my.setVisible(true);
            result.setText("회원가입 창이 열렸습니다.");
        }
    } 

    private void checkLoginLogic() {
        String userIdStr = id.getText().trim();
        String password = new String(passwd.getPassword()).trim(); 

        if (userIdStr.isEmpty() || password.isEmpty()) {
            result.setText("ID와 PW를 입력하세요.");
            result.setForeground(Color.RED);
            return;
        }

        // 로그인 체크 쿼리
        String sql = "SELECT id, name FROM users WHERE name = ? AND password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userIdStr); 
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                long dbId = rs.getLong("id"); 
                String dbName = rs.getString("name");
                JOptionPane.showMessageDialog(this, dbName + "님 환영합니다!");
                
                // 메인 화면 실행
                new TestFile(dbId).setVisible(true);
                dispose(); 

            } else {
                result.setText("로그인 실패: ID나 비번을 확인하세요.");
                result.setForeground(Color.RED);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setText("DB 오류 발생");
            JOptionPane.showMessageDialog(this, "DB 연결 실패: " + e.getMessage());
        }
    }

    public static void main (String args[]) {
        SwingUtilities.invokeLater(() -> {
            login win = new login("로그인"); 
            win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
            win.pack(); 
            win.setLocationRelativeTo(null);
            win.setVisible(true);
        });
    }
}