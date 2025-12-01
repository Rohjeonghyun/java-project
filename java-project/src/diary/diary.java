package diary;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

// [중요] 팀 프로젝트의 DBConnection 클래스 사용
import database.DBConnection;

public class diary extends JPanel {
    
    // --- [UI 컴포넌트] ---
    private JTextField titleField;
    private JTextArea contentArea;
    private JTextField tagField;
    private JLabel imageLabel;        
    private JLabel imagePreviewLabel; 
    private JComboBox<String> weatherCombo;
    
    // 알람 시그널 라벨
    private JLabel signalStatusLabel;
    
    // 검색 탭 컴포넌트
    private JTextField searchTagField;
    private JTextArea searchResultArea;
    
    // --- [일기 목록 탭 컴포넌트] ---
    private JTable listTable;
    private DefaultTableModel tableModel;
    private JLabel listYearMonthLabel; 
    private Calendar listCalendar;     

    private String selectedImagePath = null;
    private long loggedInUserId; 

    public diary() {
        this(1L); 
    }

    public diary(long userId) {
        this.loggedInUserId = userId; 
        
        // 목록 조회용 달력 객체 생성
        listCalendar = Calendar.getInstance();

        setLayout(new BorderLayout());

        JTabbedPane mainTab = new JTabbedPane();
        mainTab.addTab("일기 쓰기", createWritePanel());
        mainTab.addTab("일기 모아보기", createListPanel()); 
        mainTab.addTab("태그 검색", createSearchPanel());

        mainTab.addChangeListener(e -> {
            if (mainTab.getSelectedIndex() == 1) { 
                loadDiaryListByMonth();
            }
        });

        add(mainTab, BorderLayout.CENTER);
        
        updateSignal();
    }

    // --- [UI 1] 일기 쓰기 화면 ---
    private JPanel createWritePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        JPanel dateWeatherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dateLabel = new JLabel("Date: " + LocalDate.now().toString());
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        String[] weathers = {"Sunny", "Cloudy", "Rainy", "Snowy"};
        weatherCombo = new JComboBox<>(weathers);
        
        signalStatusLabel = new JLabel(" Signal: ? ");
        signalStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        signalStatusLabel.setForeground(Color.GRAY);

        dateWeatherPanel.add(dateLabel);
        dateWeatherPanel.add(new JLabel("  |  Weather: "));
        dateWeatherPanel.add(weatherCombo);
        dateWeatherPanel.add(new JLabel("  |  ")); 
        dateWeatherPanel.add(signalStatusLabel); 

        titleField = new JTextField();
        titleField.setBorder(BorderFactory.createTitledBorder("Title"));
        
        tagField = new JTextField();
        tagField.setBorder(BorderFactory.createTitledBorder("Tags (공백 구분, #으로 시작. 예: #여행 #맛집)"));

        topPanel.add(dateWeatherPanel);
        topPanel.add(titleField);
        topPanel.add(tagField);
        panel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        contentArea = new JTextArea(10, 20);
        contentArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Dear Diary..."));
        
        imagePreviewLabel = new JLabel("사진 미리보기", SwingConstants.CENTER);
        imagePreviewLabel.setPreferredSize(new Dimension(200, 200)); 
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(imagePreviewLabel, BorderLayout.EAST);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddImage = new JButton("Attach Image");
        imageLabel = new JLabel("No image selected");
        imagePanel.add(btnAddImage);
        imagePanel.add(imageLabel);

        JButton btnSave = new JButton("Save Diary");
        btnSave.setBackground(new Color(100, 149, 237));
        btnSave.setForeground(Color.WHITE);

        bottomPanel.add(imagePanel, BorderLayout.WEST);
        bottomPanel.add(btnSave, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        btnAddImage.addActionListener(e -> chooseImage());
        btnSave.addActionListener(e -> saveDiaryToDB());

        return panel;
    }

    // --- [UI 2] 일기 목록 화면 ---
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        
        listYearMonthLabel = new JLabel("");
        listYearMonthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        updateListLabel(); 

        prevBtn.addActionListener(e -> {
            listCalendar.add(Calendar.MONTH, -1); 
            updateListLabel();      
            loadDiaryListByMonth(); 
        });

        nextBtn.addActionListener(e -> {
            listCalendar.add(Calendar.MONTH, 1);  
            updateListLabel();      
            loadDiaryListByMonth(); 
        });

        navPanel.add(prevBtn);
        navPanel.add(listYearMonthLabel);
        navPanel.add(nextBtn);
        
        panel.add(navPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "날짜", "날씨", "제목"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        listTable = new JTable(tableModel);
        listTable.setRowHeight(25);
        listTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        listTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        listTable.getColumnModel().getColumn(3).setPreferredWidth(300);

        listTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = listTable.getSelectedRow();
                    if (row != -1) {
                        String id = listTable.getValueAt(row, 0).toString();
                        showDiaryDetail(id);
                    }
                }
            }
        });

        panel.add(new JScrollPane(listTable), BorderLayout.CENTER);
        
        JButton btnRefresh = new JButton("현재 달 목록 새로고침");
        btnRefresh.addActionListener(e -> loadDiaryListByMonth());
        panel.add(btnRefresh, BorderLayout.SOUTH);

        return panel;
    }

    // --- [UI 3] 태그 검색 화면 ---
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchTagField = new JTextField(20);
        searchTagField.setBorder(BorderFactory.createTitledBorder("태그 검색"));
        JButton btnSearch = new JButton("검색");
        searchBar.add(searchTagField);
        searchBar.add(btnSearch);
        panel.add(searchBar, BorderLayout.NORTH);

        searchResultArea = new JTextArea();
        searchResultArea.setEditable(false);
        panel.add(new JScrollPane(searchResultArea), BorderLayout.CENTER);

        btnSearch.addActionListener(e -> searchDiariesByTag());
        searchTagField.addActionListener(e -> searchDiariesByTag());

        return panel;
    }

    // --- [기능 구현] ---

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            selectedImagePath = file.getAbsolutePath();
            imageLabel.setText(file.getName());
            try {
                ImageIcon icon = new ImageIcon(selectedImagePath);
                Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                imagePreviewLabel.setIcon(new ImageIcon(img));
                imagePreviewLabel.setText(""); 
            } catch (Exception e) {
                imagePreviewLabel.setText("이미지 로드 실패");
            }
        }
    }

    private void saveDiaryToDB() {
        String title = titleField.getText();
        String content = contentArea.getText();
        String weather = (String) weatherCombo.getSelectedItem();
        String tags = tagField.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "제목과 내용을 입력해주세요.");
            return;
        }

        if (!tags.isEmpty()) { 
            String[] tagParts = tags.split("\\s+");
            for (String part : tagParts) {
                if (!part.startsWith("#")) {
                    JOptionPane.showMessageDialog(this, 
                        "태그 형식이 잘못되었습니다.\n모든 태그는 '#'으로 시작해야 합니다.\n(예: #안녕 #하세요)");
                    return; 
                }
            }
        }

        String sql = "INSERT INTO diary_entries (user_id, entry_date, title, content, weather, image_path, tags) VALUES (?, CURDATE(), ?, ?, ?, ?, ?)";

        // [DB 충돌 방지] DBConnection 사용
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, loggedInUserId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, weather);
            pstmt.setString(5, selectedImagePath);
            pstmt.setString(6, tags);

            int result = pstmt.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "일기가 저장되었습니다!");
                
                titleField.setText("");
                contentArea.setText("");
                tagField.setText("");
                imageLabel.setText("No image selected");
                imagePreviewLabel.setIcon(null);
                imagePreviewLabel.setText("사진 미리보기");
                selectedImagePath = null;
                
                loadDiaryListByMonth(); 
                updateSignal();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "저장 에러: " + ex.getMessage());
        }
    }

    private void loadDiaryListByMonth() {
        tableModel.setRowCount(0);
        
        int year = listCalendar.get(Calendar.YEAR);
        int month = listCalendar.get(Calendar.MONTH) + 1; 

        String sql = "SELECT id, entry_date, weather, title FROM diary_entries " +
                     "WHERE user_id = ? AND YEAR(entry_date) = ? AND MONTH(entry_date) = ? " +
                     "ORDER BY entry_date ASC, id ASC"; 
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setLong(1, loggedInUserId);
            pstmt.setInt(2, year);
            pstmt.setInt(3, month);

            ResultSet rs = pstmt.executeQuery();
            
            while(rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("id"));
                row.add(rs.getString("entry_date"));
                row.add(rs.getString("weather"));
                row.add(rs.getString("title"));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void updateListLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월");
        listYearMonthLabel.setText(sdf.format(listCalendar.getTime()));
    }

    // [삭제 기능 포함] 상세 보기 메서드
    private void showDiaryDetail(String diaryId) {
        String sql = "SELECT * FROM diary_entries WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, diaryId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String date = rs.getString("entry_date");
                String title = rs.getString("title");
                String weather = rs.getString("weather");
                String content = rs.getString("content");
                String tags = rs.getString("tags");
                String imgPath = rs.getString("image_path");
                
                JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
                JTextArea detailArea = new JTextArea(content);
                detailArea.setEditable(false);
                detailArea.setLineWrap(true);
                
                String header = String.format("[날짜] %s  [날씨] %s\n[제목] %s\n[태그] %s\n", date, weather, title, (tags==null?"":tags));
                
                JPanel textPanel = new JPanel(new BorderLayout());
                textPanel.add(new JLabel("<html><pre>" + header + "</pre></html>"), BorderLayout.NORTH);
                textPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);
                detailPanel.add(textPanel, BorderLayout.CENTER);
                
                if (imgPath != null && !imgPath.isEmpty()) {
                    try {
                        ImageIcon icon = new ImageIcon(imgPath);
                        Image img = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                        detailPanel.add(new JLabel(new ImageIcon(img)), BorderLayout.SOUTH);
                    } catch (Exception e) {}
                }
                
                // 삭제 버튼 추가된 옵션
                Object[] options = {"닫기", "삭제"};
                
                int choice = JOptionPane.showOptionDialog(
                    this, 
                    detailPanel, 
                    "일기 상세 보기", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.PLAIN_MESSAGE, 
                    null, 
                    options, 
                    options[0]
                );

                if (choice == 1) { // 삭제 버튼 클릭 시
                    deleteDiary(diaryId);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // [추가된 삭제 로직]
    private void deleteDiary(String diaryId) {
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "정말로 이 일기를 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.", 
            "삭제 확인", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM diary_entries WHERE id = ?";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, diaryId);
                int result = pstmt.executeUpdate();
                
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "일기가 삭제되었습니다.");
                    loadDiaryListByMonth();
                    updateSignal(); 
                } else {
                    JOptionPane.showMessageDialog(this, "삭제에 실패했습니다.");
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB 오류: " + e.getMessage());
            }
        }
    }

    private void searchDiariesByTag() {
        String keyword = searchTagField.getText().trim();
        if (keyword.isEmpty()) return;

        searchResultArea.setText(""); 
        String sql = "SELECT * FROM diary_entries WHERE user_id = ? AND tags LIKE ? ORDER BY entry_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, loggedInUserId);
            pstmt.setString(2, "%" + keyword + "%"); 

            ResultSet rs = pstmt.executeQuery();
            boolean found = false;

            while (rs.next()) {
                found = true;
                String date = rs.getDate("entry_date").toString();
                String title = rs.getString("title");
                String content = rs.getString("content");
                String tags = rs.getString("tags");

                searchResultArea.append("----------------------------------------\n");
                searchResultArea.append("[" + date + "] " + title + "\n");
                searchResultArea.append("태그: " + (tags != null ? tags : "") + "\n");
                searchResultArea.append("내용: " + content + "\n");
                searchResultArea.append("\n");
            }
            if (!found) searchResultArea.append("검색 결과가 없습니다.");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // [신호 로직 1]
    public int getTodaySignal() {
        int signal = 0;
        String sql = "SELECT count(*) FROM diary_entries WHERE user_id = ? AND entry_date = CURDATE()";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    signal = 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signal; 
    }
    
    // [신호 로직 2]
    public void updateSignal() {
        int signal = getTodaySignal();
        System.out.println(">>> [SIGNAL SEND] 현재 신호 값: " + signal);
        
        if (signal == 1) {
            signalStatusLabel.setText(" 작성 완료 (Signal: 1) ");
            signalStatusLabel.setForeground(new Color(0, 150, 0)); 
        } else {
            signalStatusLabel.setText(" 오늘 미작성 "); 
            signalStatusLabel.setForeground(Color.RED); 
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("심플 다이어리");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new diary()); 
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}