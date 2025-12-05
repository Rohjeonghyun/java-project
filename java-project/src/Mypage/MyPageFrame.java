package Mypage;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import database.DBConnection;

public class MyPageFrame extends JPanel implements ActionListener {

    private JPanel profilePanel;
    private Image profileImage;

    private JButton btnEditProfile;
    private JButton btnChangePassword;
    private JButton btnMyStats;
    private JButton btnReminderSettings;
    private JButton btnLogout;
    private JButton btnDeleteAccount;

    // 로그인한 사용자 id
    private long userId;

    //left 프로필란에 표시할 라벨->필드로 올림(DB)
    private JLabel lblName;
    private JLabel lblBirth;
    
    //색상
    private static final Color BG_OUTER    = new Color(236, 240, 245);
    private static final Color BG_CARD     = Color.WHITE;
    private static final Color BG_BAR      = new Color(245, 245, 245);
    private static final Color BORDER_CARD = new Color(205, 210, 220);
    private static final Color BORDER_GRP  = new Color(210, 215, 225);
    private static final Color TEXT_MAIN   = new Color(150, 150, 150);
    private static final Color TEXT_SUB    = new Color(110, 110, 110);
    private static final Color PROFILE_BLUE = new Color(70, 120, 210);

    public MyPageFrame(long userId) {
    	this.userId = userId;
    	
        //all layout
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

        //상단 바
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_BAR);
        topBar.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel lblLeft = new JLabel("MyPage");
        lblLeft.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblLeft.setForeground(TEXT_MAIN);
        topBar.add(lblLeft, BorderLayout.WEST);

        JLabel lblRight = new JLabel("Profile  ·  Settings");
        lblRight.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblRight.setForeground(TEXT_SUB);
        topBar.add(lblRight, BorderLayout.EAST);

        card.add(topBar, BorderLayout.NORTH);

        //좌우분할
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setBackground(BG_CARD);
        center.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.add(center, BorderLayout.CENTER);

        //왼쪽(프로필)
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

        lblName = new JLabel("사용자명");
        lblName.setFont(new Font("SansSerif", Font.BOLD | Font.PLAIN, 18));
        lblName.setForeground(TEXT_MAIN);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblBirth = new JLabel("생년월일 : 2000-01-01");
        lblBirth.setFont(new Font("SansSerif", Font.BOLD | Font.PLAIN, 14));
        lblBirth.setForeground(TEXT_MAIN);
        lblBirth.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(profilePanel);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(lblName);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(lblBirth);
        leftPanel.add(Box.createVerticalGlue());

        center.add(leftPanel);

        //오른쪽(버튼)
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(BG_CARD);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(BORDER_GRP),
                        "설정",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("SansSerif",Font.PLAIN, 12),
                        TEXT_SUB
                )
        );

        btnEditProfile      = createButton("프로필 변경");
        btnChangePassword   = createButton("비밀번호 변경");
        btnMyStats          = createButton("통계");
        btnReminderSettings = createButton("리마인더 설정");
        btnLogout           = createButton("로그아웃");
        btnDeleteAccount    = createButton("계정 삭제");  // 활성화

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
        
        loadProfileFromDB();//DB->프로필 읽어서 라벨반영
    }

    //버튼
    private JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif",Font.BOLD | Font.PLAIN, 14));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);

        b.setMaximumSize(new Dimension(220, 32));
        b.setPreferredSize(new Dimension(220, 32));
        b.setMargin(new Insets(3, 15, 3, 15));

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
    //DB->프로필 읽어서 라벨반영 메소드
    private void loadProfileFromDB() {
	String sql = "select name, birth from users where id = ?";
    	
    	try(Connection con = DBConnection.getConnection();
    		PreparedStatement ps = con.prepareStatement(sql)){
    		
    		ps.setLong(1, userId);
    		
    		try(ResultSet rs = ps.executeQuery()) {
    			if(rs.next()) {
    				String name = rs.getString("name");
    				String birth = rs.getString("birth");
    			
    			if(name == null || name.isEmpty()) {
    				name = "사용자명";
    			}
    			lblName.setText(name);
    			
    			if(birth == null || birth.isEmpty()) {
    				lblBirth.setText("생년월일 : -");
    			} else { 
    				lblBirth.setText("생년월일 : "+birth);
    				}
    			}
    		}
    		
    	} catch(Exception e) {
        	e.printStackTrace();
        }
    } 
    //EditProfileFrame 저장 후 호출 메소드
    public void refreshProfile() {
    	loadProfileFromDB();
    	}

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == btnEditProfile) {
            new EditProfileFrame(this, userId).setVisible(true);
        } else if (src == btnChangePassword) {
            new ChangePasswordFrame(this, userId).setVisible(true);
        } else if (src == btnMyStats) {
            new StatsFrame(this, userId).setVisible(true);
        } else if (src == btnReminderSettings) {
            new ReminderFrame(this, userId).setVisible(true);
        } else if (src == btnLogout) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "정말 로그아웃 하시겠습니까?",
                    "로그아웃",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                // 최상위 프레임 닫기
                SwingUtilities.getWindowAncestor(this).dispose();
            }
        } else if (src == btnDeleteAccount) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "정말 계정을 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.",
                    "계정 삭제",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                deleteUserAccount();
            }
        }
    }

    // 계정 삭제(DB)
    private void deleteUserAccount() {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, userId);
            int deleted = ps.executeUpdate();

            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, "계정이 삭제되었습니다.");
                SwingUtilities.getWindowAncestor(this).dispose();
            } else {
                JOptionPane.showMessageDialog(this, "계정 삭제 실패: 사용자 정보를 찾을 수 없습니다.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "계정 삭제 중 오류 발생: " + ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}