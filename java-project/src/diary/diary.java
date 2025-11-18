package diary;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.*;

// 1. JFrame 대신 JPanel을 상속받도록 변경
public class diary extends JPanel implements ActionListener {

    private long currentUserId;

    JTextField tfDate;
    JButton btnLoad, btnDelete;
    JTextField tfTitle, tfWeather;
    JTextArea taContent;
    JTextField tfTags;
    JTextField tfImagePath;
    JButton btnSave;

    // 2. TestFile에서 new diary()로 호출할 수 있도록 기본 생성자 추가
    public diary() {
        this("나의 다이어리", 1001L);
    }

    public diary(String title, long userId) {
        this.currentUserId = userId;
        this.setLayout(new BorderLayout()); // ct.setLayout -> this.setLayout

        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pTop.setBorder(new TitledBorder("날짜 선택 (YYYY-MM-DD)"));

        tfDate = new JTextField("2025-11-10", 12);
        btnLoad = new JButton("불러오기");
        btnDelete = new JButton("삭제하기");

        pTop.add(new JLabel("날짜:"));
        pTop.add(tfDate);
        pTop.add(btnLoad);
        pTop.add(btnDelete);

        this.add(pTop, BorderLayout.NORTH); // ct.add -> this.add

        JPanel pCenter = new JPanel(new BorderLayout(10, 10));
        pCenter.setBorder(new TitledBorder("일기 작성"));

        JPanel pCenterTop = new JPanel(new GridLayout(2, 2, 5, 5));
        pCenterTop.add(new JLabel("제목:", SwingConstants.CENTER));
        tfTitle = new JTextField();
        pCenterTop.add(tfTitle);

        pCenterTop.add(new JLabel("날씨:", SwingConstants.CENTER));
        tfWeather = new JTextField();
        pCenterTop.add(tfWeather);
        pCenter.add(pCenterTop, BorderLayout.NORTH);

        taContent = new JTextArea();
        taContent.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(taContent);
        pCenter.add(scrollPane, BorderLayout.CENTER);

        this.add(pCenter, BorderLayout.CENTER); // ct.add -> this.add

        JPanel pBottom = new JPanel(new BorderLayout(10, 10));
        pBottom.setBorder(new TitledBorder("부가 정보"));

        JPanel pBottomInfo = new JPanel(new GridLayout(2, 2, 5, 5));
        pBottomInfo.add(new JLabel("태그 (쉼표로 구분):"));
        tfTags = new JTextField();
        pBottomInfo.add(tfTags);

        pBottomInfo.add(new JLabel("이미지 경로:"));
        tfImagePath = new JTextField();
        pBottomInfo.add(tfImagePath);
        pBottom.add(pBottomInfo, BorderLayout.CENTER);

        btnSave = new JButton("일기 저장하기");
        btnSave.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        pBottom.add(btnSave, BorderLayout.SOUTH);

        this.add(pBottom, BorderLayout.SOUTH); // ct.add -> this.add

        btnLoad.addActionListener(this);
        btnSave.addActionListener(this);
        btnDelete.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        String entryDate = tfDate.getText();

        if (entryDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "날짜를 먼저 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cmd.equals("불러오기")) {
            System.out.println(entryDate + " 날짜의 일기를 DB에서 불러옵니다 (User ID: " + currentUserId + ")");
            tfTitle.setText(entryDate + "의 임시 제목");
            taContent.setText("DB에서 불러온 일기 내용입니다.");
            tfWeather.setText("맑음");
            tfTags.setText("DB태그1, DB태그2");
            tfImagePath.setText("/img/db_image.png");
            JOptionPane.showMessageDialog(this, entryDate + " 일기를 불러왔습니다.");

        } else if (cmd.equals("일기 저장하기")) {
            String title = tfTitle.getText();
            String content = taContent.getText();
            JOptionPane.showMessageDialog(this, entryDate + " 일기를 저장합니다.");

        } else if (cmd.equals("삭제하기")) {
            int result = JOptionPane.showConfirmDialog(this,
                    entryDate + " 일기를 정말 삭제하시겠습니까?",
                    "삭제 확인", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, entryDate + " 일기를 삭제했습니다.");
                clearFields();
            }
        }
    }

    private void clearFields() {
        tfTitle.setText("");
        taContent.setText("");
        tfWeather.setText("");
        tfTags.setText("");
        tfImagePath.setText("");
    }
}