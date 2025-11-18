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
    	
        // 전체 배경
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // ★ 여기서 add(root); → this에 레이아웃 설정하고 add해야 함
        setLayout(new BorderLayout());
        add(root, BorderLayout.CENTER);

        // 가운데 세로 레이아웃 영역
        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40)); // 좌우 여백
        root.add(center, BorderLayout.CENTER);

        // ----- 프로필 원 -----
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
                    g2.setColor(Color.BLACK);
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

        // ----- 텍스트 라벨 -----
        JLabel lblName = new JLabel("Tester");
        lblName.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblName.setForeground(new Color(150, 150, 150));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBirth = new JLabel("Birth date: 2000-01-01");
        lblBirth.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblBirth.setForeground(new Color(190, 190, 190));
        lblBirth.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ----- 버튼들 -----
        btnEditProfile = createButton("Edit Profile", true); // 검은색
        btnChangePassword = createButton("Change Password", false);
        btnMyStats = createButton("My Stats", false);
        btnReminderSettings = createButton("Reminder Settings", false);
        btnLogout = createButton("Log Out", false);
        btnExportCsv = createButton("Export(.csv)", true);   // 검은색 작은 버튼
        btnExportCsv.setMaximumSize(new Dimension(120, 35));
        btnDeleteAccount = createButton("Delete Account", false);
        btnDeleteAccount.setEnabled(false); // 회색 비활성화

        // ----- 컴포넌트 배치 -----
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

        // Log Out + Export(.csv)
        JPanel bottomButtons = new JPanel();
        bottomButtons.setBackground(Color.WHITE);
        bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.X_AXIS));
        bottomButtons.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomButtons.add(btnLogout);
        bottomButtons.add(Box.createHorizontalStrut(10));
        bottomButtons.add(btnExportCsv);
        center.add(bottomButtons);

        center.add(Box.createVerticalStrut(12));
        center.add(btnDeleteAccount);

        center.add(Box.createVerticalGlue());

        // ----- 하단 자동저장 -----
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
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(220, 35));

        if (primary) { // 검은 버튼
            b.setBackground(Color.BLACK);
            b.setForeground(Color.WHITE);
        } else {       // 흰 버튼
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
        }

        b.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
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

    @Override//---- 화면 동작 처리 ----
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

        } else if (src == btnExportCsv) {
            JOptionPane.showMessageDialog(this, "CSV Export (DB 연동 예정)");
        }
    }

    public static void main(String[] args) {
    	SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("My Page Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(360, 620);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new MyPageFrame());
            frame.setVisible(true);
        });
    }
}
