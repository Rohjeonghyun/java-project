package Mypage;
//EditProfileFrame.java

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import database.DBConnection;

public class EditProfileFrame extends JFrame {

    private JTextField txtUserName;

    private JTextField txtYear;
    private JComboBox<Integer> cbMonth;
    private JComboBox<Integer> cbDay;

    private long userId;

    public EditProfileFrame(Component owner) {
        this(owner, 1L);
    }

    public EditProfileFrame(Component owner, long userId) {
        this.userId = userId;

        setTitle("프로필 변경");
        setSize(400, 250);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Edit Profile"));
        panel.setBackground(Color.WHITE);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        //사용자명
        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("사용자명 :"), c);

        c.gridx = 1;
        txtUserName = new JTextField(15);
        panel.add(txtUserName, c);

        //생년월일
        c.gridx = 0; c.gridy = 1;
        panel.add(new JLabel("생년월일 :"), c);

        c.gridx = 1;
        JPanel birthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));

        // 연도
        txtYear = new JTextField(4);
        birthPanel.add(txtYear);
        birthPanel.add(new JLabel(" - "));

        // 월(1~12)
        cbMonth = new JComboBox<>();
        for (int i = 1; i <= 12; i++) cbMonth.addItem(i);
        birthPanel.add(cbMonth);
        birthPanel.add(new JLabel(" - "));

        // 일(1~31)
        cbDay = new JComboBox<>();
        for (int i = 1; i <= 31; i++) cbDay.addItem(i);
        birthPanel.add(cbDay);

        panel.add(birthPanel, c);

        //버튼
        JPanel bottom = new JPanel();
        JButton btnSave = new JButton("저장");
        btnSave.addActionListener(e -> saveProfile());

        JButton btnClose = new JButton("닫기");
        btnClose.addActionListener(e -> dispose());

        bottom.add(btnSave);
        bottom.add(btnClose);

        getContentPane().setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadProfileFromDB();
    }

    //DB
    private void loadProfileFromDB() {
        String sql = "SELECT name, birth_date FROM users WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    txtUserName.setText(rs.getString("name"));

                    Date birth = rs.getDate("birth_date");
                    if (birth != null) {
                        LocalDate d = birth.toLocalDate();
                        txtYear.setText(String.valueOf(d.getYear()));
                        cbMonth.setSelectedItem(d.getMonthValue());
                        cbDay.setSelectedItem(d.getDayOfMonth());
                    } else {
                        txtYear.setText("2000");
                        cbMonth.setSelectedItem(1);
                        cbDay.setSelectedItem(1);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "프로필 로드 실패: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //저장 예외처리
    private void saveProfile() {
        try {
            String name = txtUserName.getText().trim();

            // 연도 텍스트필드로 사용자 직접 타이핑
            int year = Integer.parseInt(txtYear.getText().trim());
            int month = (Integer) cbMonth.getSelectedItem();
            int day = (Integer) cbDay.getSelectedItem();

            String birthStr = String.format("%04d-%02d-%02d", year, month, day);
            Date birthDate = Date.valueOf(birthStr);

            String sql = "UPDATE users SET name=?, birth_date=? WHERE id=?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setDate(2, birthDate);
                ps.setLong(3, userId);

                int updated = ps.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "저장 완료!\nName: " + name + "\nBirth: " + birthStr);
            }

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this,
                "연도를 숫자로 입력하세요. 예: 2001",
                "입력 오류", JOptionPane.WARNING_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "프로필 저장 실패: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
