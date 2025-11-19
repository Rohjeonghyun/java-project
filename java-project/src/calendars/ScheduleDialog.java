package calendars;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ScheduleDialog extends JDialog implements ActionListener {
    private JTextField titleField;
    private JComboBox<CategoryItem> categoryCombo;
    private JComboBox<String> startHourCombo, startMinCombo;
    private JComboBox<String> endHourCombo, endMinCombo;
    private JButton saveButton, cancelButton, manageCategoryButton;

    private Vector<CategoryItem> categories;
    private Calendar selectedDate; // 선택된 날짜 (년, 월, 일 정보)
    
 // 데이터를 저장할 대상 리스트
    private Vector<ScheduleItem> targetVector;
    private DefaultListModel<ScheduleItem> targetUiModel;

    public ScheduleDialog(Window parent, String title, Calendar selectedDate, Vector<CategoryItem> categories,
    						Vector<ScheduleItem> targetVector, DefaultListModel<ScheduleItem> targetUiModel) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.selectedDate = selectedDate;
        this.categories = categories;
        
        // [NEW] 저장 대상 받기
        this.targetVector = targetVector;
        this.targetUiModel = targetUiModel;

        // --- 메인 패널 (BorderLayout) ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 입력 폼 패널 (GridLayout) ---
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5)); // 4행 2열, 간격 5
        formPanel.setBackground(Color.WHITE);

        // 일정 내용 (DB: todos.title)
        formPanel.add(new JLabel("일정 내용:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        // 카테고리 (DB: todos.category_id)
        // (DB 연동 전 임시 데이터)
        formPanel.add(new JLabel("카테고리:"));
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0)); // 콤보박스와 버튼을 담을 서브 패널
        categoryPanel.setBackground(Color.WHITE);
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setRenderer(new CategoryRenderer());
        categoryCombo.setLightWeightPopupEnabled(false);
        categoryPanel.add(categoryCombo, BorderLayout.CENTER);

        manageCategoryButton = new JButton("관리");
        manageCategoryButton.addActionListener(this);
        categoryPanel.add(manageCategoryButton, BorderLayout.EAST);
        
        formPanel.add(categoryPanel);
        
        // 시작 시간 (DB: todos.start_time)
        formPanel.add(new JLabel("시작 시간:"));
        JPanel startTimePanel = createTimePanel(); // 시간/분 패널 생성
        startHourCombo = (JComboBox<String>) startTimePanel.getComponent(0);
        startMinCombo = (JComboBox<String>) startTimePanel.getComponent(1);
        formPanel.add(startTimePanel);

        // 종료 시간 (DB: todos.end_time)
        formPanel.add(new JLabel("종료 시간:"));
        JPanel endTimePanel = createTimePanel(); // 시간/분 패널 생성
        endHourCombo = (JComboBox<String>) endTimePanel.getComponent(0);
        endMinCombo = (JComboBox<String>) endTimePanel.getComponent(1);
        formPanel.add(endTimePanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // --- 버튼 패널 (FlowLayout) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("저장");
        cancelButton = new JButton("취소");

        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- 다이얼로그 설정 ---
        setContentPane(mainPanel); // 생성한 메인 패널을 다이얼로그의 컨텐츠로 설정
        pack(); // 내용물에 맞게 창 크기 자동 조절
        setLocationRelativeTo(null); // 부모 창 중앙에 표시
    }

    /**
     * 시간(0-23시)과 분(0, 30분) JComboBox를 생성하여 JPanel에 담아 반환합니다.
     */
    private JPanel createTimePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(Color.WHITE);

        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) hours[i] = String.format("%02d시", i);
        JComboBox<String> hourCombo = new JComboBox<>(hours);
        hourCombo.setLightWeightPopupEnabled(false);
        
        String[] minutes = {"00분", "10분", "20분", "30분", "40분", "50분"};
        JComboBox<String> minCombo = new JComboBox<>(minutes);
        minCombo.setLightWeightPopupEnabled(false);
        
        panel.add(hourCombo);
        panel.add(minCombo);
        return panel;
    }

    /**
     * "저장", "취소" 버튼 클릭 이벤트 처리
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == manageCategoryButton) {
            // [NEW] "관리" 버튼 클릭 시
            // 10주차 다이얼로그 예제처럼 새 다이얼로그 호출
            //
            CategoryManagerDialog manager = new CategoryManagerDialog(this, "카테고리 관리", categories);
            manager.setVisible(true); // 이 창이 닫힐 때까지 여기서 대기
            
            // [NEW] 관리 창이 닫힌 후, "마스터 목록" Vector가 변경되었으므로
            // JComboBox를 새로고침합니다.
            refreshCategoryCombo();

        } else if (source == saveButton) {
            // [MODIFIED] 저장 로직: 객체 생성 후 리스트에 추가
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "일정 내용을 입력해주세요.");
                return;
            }
            CategoryItem selectedCategory = (CategoryItem) categoryCombo.getSelectedItem();
            String categoryName = (selectedCategory != null) ? selectedCategory.getName() : "미지정";
            Color categoryColor = (selectedCategory != null) ? selectedCategory.getColor() : Color.LIGHT_GRAY;
            
            String start = startHourCombo.getSelectedItem() + " " + startMinCombo.getSelectedItem();
            String end = endHourCombo.getSelectedItem() + " " + endMinCombo.getSelectedItem();

            // ScheduleItem 객체 생성
            ScheduleItem newItem = new ScheduleItem(title, categoryName, start, end, categoryColor);

            // 데이터 원본(Vector)과 UI모델(DefaultListModel)에 둘 다 추가
            targetVector.add(newItem);
            targetUiModel.addElement(newItem);
            
            dispose();
        } else if (source == cancelButton) {
        	dispose();
        }
    }

    /**
     * [NEW] 카테고리 JComboBox를 "마스터 목록" Vector 기준으로 새로고침합니다.
     */
    private void refreshCategoryCombo() {
        // JComboBox의 모델을 "마스터 목록" Vector로 새로 만듭니다.
        // 11주차 JComboBox 생성자
        categoryCombo.setModel(new DefaultComboBoxModel<>(categories));
    }
}