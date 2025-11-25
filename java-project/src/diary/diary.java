package diary;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class diary extends JPanel {
    
    // --- [1] 일기 쓰기 화면용 컴포넌트 ---
    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField tagField;
    private JLabel imageLabel;
    private JComboBox<String> weatherCombo;
    private String selectedImagePath = null;
    private File selectedFile = null;

    // --- [2] 태그 검색 화면용 컴포넌트 ---
    private JTextField searchTagField; // 검색어 입력
    private JTextArea searchResultArea; // 결과 보여주는 곳

    // 사용자 ID (임시 고정)
    private final long CURRENT_USER_ID = 1L; 

    // DB 연결 정보 (본인 환경에 맞게 확인!)
    private final String DB_URL = "jdbc:mysql://localhost:3306/java?serverTimezone=UTC";
    private final String DB_USER = "root";
    private final String DB_PASS = "1234";

    public diary() {
        setLayout(new BorderLayout());

        // 메인 탭 패널 (쓰기 탭 / 검색 탭 분리)
        JTabbedPane mainTab = new JTabbedPane();

        // 1. 일기 쓰기 패널 생성 & 탭 추가
        JPanel writePanel = createWritePanel();
        mainTab.addTab("일기 쓰기", writePanel);

        // 2. 태그 검색 패널 생성 & 탭 추가
        JPanel searchPanel = createSearchPanel();
        mainTab.addTab("태그 검색", searchPanel);

        add(mainTab, BorderLayout.CENTER);
    }

    // --------------------------------------------------------
    // [UI 1] 일기 쓰기 화면 만들기
    // --------------------------------------------------------
    private JPanel createWritePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 상단 (날짜, 날씨, 제목, 태그)
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        JPanel dateWeatherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dateLabel = new JLabel("Date: " + LocalDate.now().toString());
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        String[] weathers = {"Sunny", "Cloudy", "Rainy", "Snowy"};
        weatherCombo = new JComboBox<>(weathers);
        
        dateWeatherPanel.add(dateLabel);
        dateWeatherPanel.add(new JLabel("  |  Weather: "));
        dateWeatherPanel.add(weatherCombo);

        titleField = new JTextField();
        titleField.setBorder(BorderFactory.createTitledBorder("Title"));

        tagField = new JTextField();
        tagField.setBorder(BorderFactory.createTitledBorder("Tags (예: #여행 #맛집 - 띄어쓰기로 구분)"));

        topPanel.add(dateWeatherPanel);
        topPanel.add(titleField);
        topPanel.add(tagField);
        
        panel.add(topPanel, BorderLayout.NORTH);

        // 중앙 (내용)
        contentArea = new JTextArea();
        contentArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Dear Diary..."));
        panel.add(scrollPane, BorderLayout.CENTER);

        // 하단 (이미지, 저장 버튼)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddImage = new JButton("Attach Image");
        imageLabel = new JLabel("No image selected");
        imageLabel.setForeground(Color.GRAY);
        imagePanel.add(btnAddImage);
        imagePanel.add(imageLabel);

        JButton btnSave = new JButton("Save Diary");
        btnSave.setBackground(new Color(100, 149, 237));
        btnSave.setForeground(Color.WHITE);

        bottomPanel.add(imagePanel, BorderLayout.WEST);
        bottomPanel.add(btnSave, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // 리스너 연결
        btnAddImage.addActionListener(e -> chooseImage());
        btnSave.addActionListener(e -> saveDiaryToDB());

        return panel;
    }

    // --------------------------------------------------------
    // [UI 2] 태그 검색 화면 만들기 (새로 추가된 기능!)
    // --------------------------------------------------------
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 검색창 부분
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchTagField = new JTextField(20);
        searchTagField.setBorder(BorderFactory.createTitledBorder("검색할 태그 (예: 여행)"));
        JButton btnSearch = new JButton("검색");
        
        searchBar.add(searchTagField);
        searchBar.add(btnSearch);
        panel.add(searchBar, BorderLayout.NORTH);

        // 결과 보여줄 영역
        searchResultArea = new JTextArea();
        searchResultArea.setEditable(false); // 읽기 전용
        searchResultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(searchResultArea);
        scroll.setBorder(BorderFactory.createTitledBorder("검색 결과"));
        panel.add(scroll, BorderLayout.CENTER);

        // 검색 버튼 누르면 실행
        btnSearch.addActionListener(e -> searchDiariesByTag());
        
        // 엔터키 쳐도 검색되게
        searchTagField.addActionListener(e -> searchDiariesByTag());

        return panel;
    }

    // --------------------------------------------------------
    // [Logic] 기능 구현 메소드들
    // --------------------------------------------------------

    // 1. 이미지 선택
    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            imageLabel.setText(selectedFile.getName());
            imageLabel.setForeground(Color.BLACK);
        }
    }

    // 2. DB 저장 (일기 쓰기)
    private void saveDiaryToDB() {
        String title = titleField.getText();
        String content = contentArea.getText();
        String weather = (String) weatherCombo.getSelectedItem();
        String tags = tagField.getText();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "제목과 내용을 입력해주세요.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            conn.setAutoCommit(false);

            // 일기 저장
            String sqlDiary = "INSERT INTO diary_entries (user_id, entry_date, title, content, weather) VALUES (?, CURDATE(), ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlDiary, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, CURRENT_USER_ID);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, weather);
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            long diaryId = 0;
            if (rs.next()) diaryId = rs.getLong(1);

            // 이미지 저장
            if (selectedImagePath != null) {
                String safePath = selectedImagePath.replace("\\", "/");
                try (PreparedStatement pstmtImg = conn.prepareStatement("INSERT INTO diary_images (diary_id, file_url) VALUES (?, ?)")) {
                    pstmtImg.setLong(1, diaryId);
                    pstmtImg.setString(2, safePath);
                    pstmtImg.executeUpdate();
                }
            }

            // 태그 저장
            if (!tags.trim().isEmpty()) {
                saveTags(conn, diaryId, tags);
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "일기가 저장되었습니다!");
            
            // 초기화
            titleField.setText("");
            contentArea.setText("");
            tagField.setText("");
            imageLabel.setText("No image selected");
            selectedImagePath = null;

        } catch (Exception ex) {
            try { if (conn != null) conn.rollback(); } catch (SQLException e) {}
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "저장 에러: " + ex.getMessage());
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // 3. 태그 검색 (검색 기능)
    private void searchDiariesByTag() {
        String keyword = searchTagField.getText().trim();
        if (keyword.isEmpty()) return;

        // '#' 빼고 검색 (사용자가 #여행 이라고 쳐도 여행으로 검색되게)
        keyword = keyword.replace("#", "");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        searchResultArea.setText(""); // 기존 결과 지우기

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            
            // JOIN 쿼리: 일기(entries) + 연결(entry_tags) + 태그(tags) 세 테이블을 연결
            String sql = "SELECT e.entry_date, e.title, e.content, e.weather " +
                         "FROM diary_entries e " +
                         "JOIN diary_entry_tags dt ON e.id = dt.diary_id " +
                         "JOIN diary_tags t ON dt.tag_id = t.id " +
                         "WHERE e.user_id = ? AND t.name LIKE ? " +
                         "ORDER BY e.entry_date DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, CURRENT_USER_ID);
            pstmt.setString(2, "%" + keyword + "%"); // 부분 검색 (여행 -> '배낭여행'도 검색됨)
            
            rs = pstmt.executeQuery();
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                String date = rs.getDate("entry_date").toString();
                String title = rs.getString("title");
                String content = rs.getString("content");
                String weather = rs.getString("weather");

                // 결과창에 예쁘게 출력
                searchResultArea.append("========================================\n");
                searchResultArea.append("[날짜] " + date + " (" + weather + ")\n");
                searchResultArea.append("[제목] " + title + "\n");
                searchResultArea.append("----------------------------------------\n");
                searchResultArea.append(content + "\n");
                searchResultArea.append("\n");
            }

            if (!found) {
                searchResultArea.append("'" + keyword + "' 태그가 포함된 일기가 없습니다.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "검색 에러: " + ex.getMessage());
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }

    // 태그 저장 로직 (분리됨)
    private void saveTags(Connection conn, long diaryId, String tagString) throws SQLException {
        String[] tagArray = tagString.split("\\s+");
        for (String t : tagArray) {
            String tagName = t.replace("#", "").trim();
            if (tagName.isEmpty()) continue;
            long tagId = 0;

            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM diary_tags WHERE user_id=? AND name=?")) {
                check.setLong(1, CURRENT_USER_ID);
                check.setString(2, tagName);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) tagId = rs.getLong(1);
                }
            }

            if (tagId == 0) {
                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO diary_tags (user_id, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ins.setLong(1, CURRENT_USER_ID);
                    ins.setString(2, tagName);
                    ins.executeUpdate();
                    try (ResultSet rs = ins.getGeneratedKeys()) {
                        if (rs.next()) tagId = rs.getLong(1);
                    }
                }
            }
            try (PreparedStatement link = conn.prepareStatement("INSERT INTO diary_entry_tags (diary_id, tag_id) VALUES (?, ?)")) {
                link.setLong(1, diaryId);
                link.setLong(2, tagId);
                link.executeUpdate();
            }
        }
    }

    // 리소스 닫기 (코드 중복 줄이기용)
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }

    // [테스트용 메인 함수]
    public static void main(String[] args) {
        JFrame frame = new JFrame("다이어리 테스트");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new diary());
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}