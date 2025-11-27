package Mypage;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import javax.swing.*;

public class MyPageFrame extends JPanel implements ActionListener {

    private JPanel profilePanel;
    private Image profileImage;

    private JButton btnEditProfile;
    private JButton btnChangePassword;
    private JButton btnMyStats;
    private JButton btnReminderSettings;
    private JButton btnLogout;
    private JButton btnExportCsv;
    private JButton btnDeleteAccount;

    public MyPageFrame() {
    	
        //전체 배경
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        
        setLayout(new BorderLayout());
        add(root, BorderLayout.CENTER);

        
        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40)); // 좌우 여백
        root.add(center, BorderLayout.CENTER);

        //프로필 사진
        profilePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int size = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Shape circle = new Ellipse2D.Float(x, y, size, size);
                g2.setClip(circle);

                if (profileImage != null) {
                    g2.drawImage(profileImage, x, y, size, size, this);
                } else {
                    g2.setColor(Color.decode("#4A90E2"));
                    g2.fillOval(x, y, size, size);
                }
                g2.dispose();
            }
        };
        profilePanel.setPreferredSize(new Dimension(150, 150));
        profilePanel.setMaximumSize(new Dimension(150, 150));
        profilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profilePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeProfileImage();
            }
        });

        //텍스트
        JLabel lblName = new JLabel("사용자명");
        lblName.setFont(new Font("SansSerif", Font.PLAIN, 20));
        lblName.setForeground(new Color(150, 150, 150));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBirth = new JLabel("생년월일 : 2000-01-01");
        lblBirth.setFont(new Font("SansSerif", Font.BOLD |Font.PLAIN, 18));
        lblBirth.setForeground(new Color(190, 190, 190));
        lblBirth.setAlignmentX(Component.CENTER_ALIGNMENT);

        //버튼
        btnEditProfile = createButton("프로필 변경", true);
        btnChangePassword = createButton("비밀번호 변경", false);
        btnMyStats = createButton("통계", false);
        btnReminderSettings = createButton("리마인더 설정", false);
        btnLogout = createButton("로그아웃", false);
        btnDeleteAccount = createButton("계정 삭제", false);
        btnDeleteAccount.setEnabled(false); // 회색 비활성화

        //컴포넌트 배치
        center.add(profilePanel);
        center.add(Box.createVerticalStrut(15));
        center.add(lblName);
        center.add(lblBirth);
        center.add(Box.createVerticalStrut(25));

        center.add(btnEditProfile);
        center.add(Box.createVerticalStrut(12));
        center.add(btnChangePassword);
        center.add(Box.createVerticalStrut(12));
        center.add(btnMyStats);
        center.add(Box.createVerticalStrut(12));
        center.add(btnReminderSettings);
        center.add(Box.createVerticalStrut(18));

        // Log Out
        JPanel bottomButtons = new JPanel();
        bottomButtons.setBackground(Color.WHITE);
        bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.X_AXIS));
        bottomButtons.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomButtons.add(btnLogout);
        bottomButtons.add(Box.createHorizontalStrut(10));
        center.add(bottomButtons);

        center.add(Box.createVerticalStrut(12));
        center.add(btnDeleteAccount);

        center.add(Box.createVerticalGlue());

        //autoSaveBar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(245, 245, 245));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        JLabel lblAutoSave = new JLabel("Saved automatically at 22:00");
        lblAutoSave.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblAutoSave.setForeground(Color.DARK_GRAY);
        statusBar.add(lblAutoSave, BorderLayout.WEST);
        root.add(statusBar, BorderLayout.SOUTH);
    }

    // 버튼 처리
    private JButton createButton(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD |Font.PLAIN, 18));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(320, 55));

        if (primary) {
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
        } else {
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
        }

        b.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
        b.setBorderPainted(true);
        b.setContentAreaFilled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(this);
        return b;
    }

    private void changeProfileImage() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            ImageIcon icon = new ImageIcon(chooser.getSelectedFile().getAbsolutePath());
            profileImage = icon.getImage();
            profilePanel.repaint();
        }
    }

    @Override//화면ActionEvent처리
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == btnEditProfile) {
            new EditProfileFrame(this).setVisible(true);

        } else if (src == btnChangePassword) {
            new ChangePasswordFrame(this).setVisible(true);

        } else if (src == btnMyStats) {
            new StatsFrame(this).setVisible(true);

        } else if (src == btnReminderSettings) {
            new ReminderFrame(this).setVisible(true);

        } else if (src == btnLogout) {
            JOptionPane.showMessageDialog(this, "로그아웃 (DB 연동 예정)");
        }
    }

    public static void main(String[] args) {
    	SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("My Page Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 700);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new MyPageFrame());
            frame.setVisible(true);
        });
    }
}
