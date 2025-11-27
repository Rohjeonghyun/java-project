package Mypage;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class MyPageFrame extends JPanel implements ActionListener {

    private JPanel profilePanel;
    private Image profileImage;

    private JButton btnEditProfile;
    private JButton btnChangePassword;
    private JButton btnMyStats;
    private JButton btnReminderSettings;
    private JButton btnLogout;
    private JButton btnDeleteAccount;

    // 색상 (배경/테두리/글자)
    private static final Color BG_OUTER    = new Color(236, 240, 245);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color BG_BAR      = new Color(245, 245, 245);
    private static final Color BORDER_CARD = new Color(205, 210, 220);
    private static final Color BORDER_GRP  = new Color(210, 215, 225);
    private static final Color TEXT_MAIN   = new Color(40, 40, 40);
    private static final Color TEXT_SUB    = new Color(110, 110, 110);
    private static final Color TEXT_FADE   = new Color(150, 150, 150);
    private static final Color PROFILE_BLUE = new Color(70, 120, 210);

    public MyPageFrame() {

        // ===== 전체 레이아웃 =====
        setLayout(new BorderLayout());
        setBackground(BG_OUTER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(outer, BorderLayout.CENTER);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(new LineBorder(BORDER_CARD));
        outer.add(card, BorderLayout.CENTER);

        // ===== 상단 바 =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_BAR);
        topBar.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel lblLeft = new JLabel("My Page");
        lblLeft.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblLeft.setForeground(TEXT_MAIN);
        topBar.add(lblLeft, BorderLayout.WEST);

        JLabel lblRight = new JLabel("Profile  ·  Settings");
        lblRight.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRight.setForeground(TEXT_SUB);
        topBar.add(lblRight, BorderLayout.EAST);

        card.add(topBar, BorderLayout.NORTH);

        // ===== 가운데 : 좌/우 2분할 =====
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setBackground(BG_CARD);
        center.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.add(center, BorderLayout.CENTER);

        // ---------- 왼쪽 : 프로필 ----------
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(BG_CARD);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(BORDER_GRP),
                        "프로필",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("SansSerif", Font.PLAIN, 12),
                        TEXT_SUB
                )
        );

        profilePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int size = Math.min(getWidth(), getHeight()) - 6;
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
                    g2.setColor(PROFILE_BLUE);
                    g2.fillOval(x, y, size, size);
                }
                g2.dispose();
            }
        };
        profilePanel.setPreferredSize(new Dimension(200, 200));
        profilePanel.setMaximumSize(new Dimension(220, 220));
        profilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profilePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeProfileImage();
            }
        });

        JLabel lblName = new JLabel("사용자명");
        lblName.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lblName.setForeground(TEXT_MAIN);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBirth = new JLabel("생년월일 : 2000-01-01");
        lblBirth.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblBirth.setForeground(TEXT_FADE);
        lblBirth.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 위·아래 균형 맞춰 중앙 배치
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(profilePanel);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(lblName);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(lblBirth);
        leftPanel.add(Box.createVerticalGlue());

        center.add(leftPanel);

        // ---------- 오른쪽 : 버튼 영역 ----------
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(BG_CARD);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(BORDER_GRP),
                        "설정",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("SansSerif", Font.PLAIN, 12),
                        TEXT_SUB
                )
        );

        btnEditProfile      = createButton("프로필 변경");
        btnChangePassword   = createButton("비밀번호 변경");
        btnMyStats          = createButton("통계");
        btnReminderSettings = createButton("리마인더 설정");
        btnLogout           = createButton("로그아웃");
        btnDeleteAccount    = createButton("계정 삭제");
        btnDeleteAccount.setEnabled(false);

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(btnEditProfile);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(btnChangePassword);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(btnMyStats);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(btnReminderSettings);
        rightPanel.add(Box.createVerticalStrut(18));
        rightPanel.add(btnLogout);
        rightPanel.add(Box.createVerticalStrut(12));
        rightPanel.add(btnDeleteAccount);
        rightPanel.add(Box.createVerticalGlue());

        center.add(rightPanel);

        // ===== 하단 상태바 =====
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(BG_BAR);
        statusBar.setBorder(new EmptyBorder(3, 10, 3, 10));

        JLabel lblAutoSave = new JLabel("Saved automatically at 22:00");
        lblAutoSave.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblAutoSave.setForeground(TEXT_SUB);
        statusBar.add(lblAutoSave, BorderLayout.WEST);

        card.add(statusBar, BorderLayout.SOUTH);
    }

    // 기본 Swing 버튼 느낌 유지 (색/그라데이션은 Look&Feel에 맡김)
    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);

        b.setMaximumSize(new Dimension(220, 32));
        b.setPreferredSize(new Dimension(220, 32));
        b.setMargin(new Insets(3, 15, 3, 15));

        // 색/테두리 건드리지 않음 → OS/Swing 기본 스타일 그대로
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

    @Override
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

    // 단독 실행 테스트용
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("My Page Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new MyPageFrame());
            frame.setVisible(true);
        });
    }
}
