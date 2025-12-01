package Mypage;

// ChangePasswordFrame.java

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import database.DBConnection;

public class ChangePasswordFrame extends JFrame {

	private JPasswordField txtCurrentPw;
	private JPasswordField txtNewPw;
	private JCheckBox cbShowCurrentPw;
	private JCheckBox cbShowNewPw;
	private char defaultEchoChar;

	private long userId;

	public ChangePasswordFrame(Component owner) {
		this(owner, 1L);
	}

	// 생성자
	public ChangePasswordFrame(Component owner, long userId) {
		this.userId = userId;

		setTitle("비밀번호 변경");
		setSize(450, 250);
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Change Password"));
		panel.setBackground(Color.WHITE);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.WEST;

		//현재 비번
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel("현재 비밀번호 :"), c);

		c.gridx = 1;
		txtCurrentPw = new JPasswordField(12);
		panel.add(txtCurrentPw, c);

		c.gridx = 2;
		cbShowCurrentPw = new JCheckBox("Show");
		cbShowCurrentPw.setBackground(Color.WHITE);
		panel.add(cbShowCurrentPw, c);

		//새 비번
		c.gridx = 0;
		c.gridy = 1;
		panel.add(new JLabel("새로운 비밀번호 :"), c);

		c.gridx = 1;
		txtNewPw = new JPasswordField(12);
		panel.add(txtNewPw, c);

		c.gridx = 2;
		cbShowNewPw = new JCheckBox("Show");
		cbShowNewPw.setBackground(Color.WHITE);
		panel.add(cbShowNewPw, c);

		//비번 블러처리
		defaultEchoChar = txtCurrentPw.getEchoChar();

		//show박스이벤트처리
		cbShowCurrentPw.addActionListener(
				e -> txtCurrentPw.setEchoChar(cbShowCurrentPw.isSelected() ? (char) 0 : defaultEchoChar));

		cbShowNewPw.addActionListener(e -> txtNewPw.setEchoChar(cbShowNewPw.isSelected() ? (char) 0 : defaultEchoChar));

		//버튼
		JPanel bottom = new JPanel();
		JButton btnSave = new JButton("저장");
		btnSave.addActionListener(e -> changePassword());

		JButton btnClose = new JButton("닫기");
		btnClose.addActionListener(e -> dispose());

		bottom.add(btnSave);
		bottom.add(btnClose);

		getContentPane().setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
	}

	//비번 변경
	private void changePassword() {
		try {
			String currentPw = new String(txtCurrentPw.getPassword()).trim();
			String newPw = new String(txtNewPw.getPassword()).trim();

			if (currentPw.isEmpty() || newPw.isEmpty()) {
				JOptionPane.showMessageDialog(this, "모든 항목을 입력하세요.");
				return;
			}

			//현재 비번 vs 새 비번 같은 경우 방지
			if (currentPw.equals(newPw)) {
				JOptionPane.showMessageDialog(this,
						"현재 비밀번호와 새 비밀번호가 같습니다.\n다른 비밀번호를 입력하세요.");
				return;
			}

			//DB 현 비번 읽음
			String dbPw = null;
			String selectSql = "SELECT password_hash FROM users WHERE id = ?";

			try (Connection con = DBConnection.getConnection();
				 PreparedStatement ps = con.prepareStatement(selectSql)) {

				ps.setLong(1, userId);

				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						dbPw = rs.getString("password_hash");
					}
				}
			}

			//유저 정보찾기
			if (dbPw == null) {
				JOptionPane.showMessageDialog(this, "유저 정보를 찾지 못했습니다.");
				return;
			}

			//비번 일치확인
			if (!dbPw.equals(currentPw)) {
				JOptionPane.showMessageDialog(this, "현재 비밀번호가 일치하지 않습니다.");
				return;
			}

			//새 비번 업뎃
			String updateSql = "UPDATE users SET password_hash = ? WHERE id = ?";

			try (Connection con = DBConnection.getConnection();
				 PreparedStatement ps = con.prepareStatement(updateSql)) {

				ps.setString(1, newPw);
				ps.setLong(2, userId);

				int updated = ps.executeUpdate();

				if (updated > 0) {
					JOptionPane.showMessageDialog(this, "비밀번호가 성공적으로 변경되었습니다.");
					dispose();
				} else {
					JOptionPane.showMessageDialog(this, "비밀번호 변경 실패: 저장된 행 없음");
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "오류 발생: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
