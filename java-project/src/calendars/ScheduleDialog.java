package calendars;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Map;
import java.util.Vector;

public class ScheduleDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 ---
    private JTextField titleField;
    private JComboBox<CategoryItem> categoryCombo;
    
    // [NEW] 여러 날 설정 체크박스 & 종료 날짜 패널
    private JCheckBox multiDayCheckBox;
    private JPanel endDatePanel;
    private JComboBox<Integer> endYearCombo, endMonthCombo, endDayCombo;

    private JComboBox<String> startHourCombo, startMinCombo;
    private JComboBox<String> endHourCombo, endMinCombo;
    private JButton manageCategoryButton, saveButton, cancelButton;

    // --- 데이터 ---
    private Vector<CategoryItem> categories; 
    private Calendar selectedDate; // 시작 날짜
    
    // [NEW] 전체 데이터 맵 (여러 날짜에 저장하기 위해 필요)
    private Map<String, Vector<ScheduleItem>> globalScheduleData;
    
    // 현재 보고 있는 날짜의 리스트 (즉시 갱신용)
    private Vector<ScheduleItem> currentDayVector;
    private DefaultListModel<ScheduleItem> currentDayUiModel;

    public ScheduleDialog(Window parent, String title, Calendar selectedDate, 
                          Vector<CategoryItem> categories,
                          Map<String, Vector<ScheduleItem>> globalScheduleData, // [NEW] 전체 맵 받기
                          Vector<ScheduleItem> currentDayVector, 
                          DefaultListModel<ScheduleItem> currentDayUiModel) {
        
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.selectedDate = selectedDate;
        this.categories = categories;
        this.globalScheduleData = globalScheduleData;
        this.currentDayVector = currentDayVector;
        this.currentDayUiModel = currentDayUiModel;

        // --- 메인 패널 ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 입력 폼 패널 (GridLayout 행 수 증가) ---
        // 기존 4행 -> 6행으로 변경 (체크박스, 날짜선택 추가)
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5)); 
        formPanel.setBackground(Color.WHITE);

        // 1. 일정 내용
        formPanel.add(new JLabel("일정 내용:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        // 2. 카테고리
        formPanel.add(new JLabel("카테고리:"));
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0)); 
        categoryPanel.setBackground(Color.WHITE);
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setRenderer(new CategoryRenderer());
        categoryCombo.setLightWeightPopupEnabled(false); 
        categoryPanel.add(categoryCombo, BorderLayout.CENTER);
        manageCategoryButton = new JButton("관리");
        manageCategoryButton.addActionListener(this); 
        categoryPanel.add(manageCategoryButton, BorderLayout.EAST);
        formPanel.add(categoryPanel); 

        // 3. [NEW] 여러 날 설정 (체크박스)
        formPanel.add(new JLabel("기간 설정:"));
        multiDayCheckBox = new JCheckBox("여러 날 (Multi-day)");
        multiDayCheckBox.setBackground(Color.WHITE);
        multiDayCheckBox.addActionListener(this); // 체크 이벤트 연결
        formPanel.add(multiDayCheckBox);

        // 4. [NEW] 종료 날짜 설정 (평소엔 숨김/비활성)
        formPanel.add(new JLabel("종료 날짜:"));
        createEndDatePanel(); // 메소드로 패널 생성
        formPanel.add(endDatePanel);
        
        // 초기 상태: 체크박스 해제 -> 종료 날짜 비활성화
        setEndDateEnabled(false);

        // 5. 시작 시간
        formPanel.add(new JLabel("시작 시간:"));
        JPanel startTimePanel = createTimePanel(); 
        startHourCombo = (JComboBox<String>) startTimePanel.getComponent(0);
        startMinCombo = (JComboBox<String>) startTimePanel.getComponent(1);
        formPanel.add(startTimePanel);

        // 6. 종료 시간
        formPanel.add(new JLabel("종료 시간:"));
        JPanel endTimePanel = createTimePanel(); 
        endHourCombo = (JComboBox<String>) endTimePanel.getComponent(0);
        endMinCombo = (JComboBox<String>) endTimePanel.getComponent(1);
        formPanel.add(endTimePanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // --- 버튼 패널 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("저장");
        cancelButton = new JButton("취소");
        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack(); 
        setLocationRelativeTo(null); 
    }
    
    // [NEW] 종료 날짜 선택 패널 생성 (년/월/일 콤보박스)
    private void createEndDatePanel() {
        endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        endDatePanel.setBackground(Color.WHITE);

        // 년도 (현재 년도 -1 ~ +5)
        int currentYear = selectedDate.get(Calendar.YEAR);
        Vector<Integer> years = new Vector<>();
        for(int i = currentYear; i <= currentYear + 5; i++) years.add(i);
        endYearCombo = new JComboBox<>(years);
        
        // 월 (1~12)
        Vector<Integer> months = new Vector<>();
        for(int i=1; i<=12; i++) months.add(i);
        endMonthCombo = new JComboBox<>(months);
        endMonthCombo.setSelectedItem(selectedDate.get(Calendar.MONTH) + 1); // 현재 월 선택

        // 일 (1~31) - 간단하게 31로 고정 (실제론 월별 계산 필요하지만 약식 구현)
        Vector<Integer> days = new Vector<>();
        for(int i=1; i<=31; i++) days.add(i);
        endDayCombo = new JComboBox<>(days);
        endDayCombo.setSelectedItem(selectedDate.get(Calendar.DAY_OF_MONTH)); // 현재 일 선택

        // MacOS 버그 방지
        endYearCombo.setLightWeightPopupEnabled(false);
        endMonthCombo.setLightWeightPopupEnabled(false);
        endDayCombo.setLightWeightPopupEnabled(false);

        endDatePanel.add(endYearCombo);
        endDatePanel.add(new JLabel("년"));
        endDatePanel.add(endMonthCombo);
        endDatePanel.add(new JLabel("월"));
        endDatePanel.add(endDayCombo);
        endDatePanel.add(new JLabel("일"));
    }

    // [NEW] 종료 날짜 컴포넌트 활성/비활성 처리
    private void setEndDateEnabled(boolean enabled) {
        endYearCombo.setEnabled(enabled);
        endMonthCombo.setEnabled(enabled);
        endDayCombo.setEnabled(enabled);
        // 시각적 효과 (비활성 시 흐리게)
        endDatePanel.setVisible(true); // 공간은 차지하되 비활성
    }

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

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == multiDayCheckBox) {
            // [NEW] 체크박스 상태에 따라 날짜 패널 활성/비활성
            setEndDateEnabled(multiDayCheckBox.isSelected());

        } else if (source == manageCategoryButton) {
            CategoryManagerDialog manager = new CategoryManagerDialog(this, "카테고리 관리", categories);
            manager.setVisible(true);
            refreshCategoryCombo();

        } else if (source == saveButton) {
            // --- 저장 로직 시작 ---
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "일정 내용을 입력해주세요.");
                return;
            }

            // 시간 처리
            String sHourStr = (String) startHourCombo.getSelectedItem();
            String sMinStr = (String) startMinCombo.getSelectedItem();
            String eHourStr = (String) endHourCombo.getSelectedItem();
            String eMinStr = (String) endMinCombo.getSelectedItem();
            
            int sHour = Integer.parseInt(sHourStr.replace("시", ""));
            int sMin = Integer.parseInt(sMinStr.replace("분", ""));
            int eHour = Integer.parseInt(eHourStr.replace("시", ""));
            int eMin = Integer.parseInt(eMinStr.replace("분", ""));
            int startTimeVal = sHour * 60 + sMin;
            int endTimeVal = eHour * 60 + eMin;

            // 종료 시간이 시작 시간보다 빠르면 경고 (하루 일정일 때만 체크하거나, 날짜가 다르면 통과)
            if (!multiDayCheckBox.isSelected() && endTimeVal <= startTimeVal) {
                JOptionPane.showMessageDialog(this, "종료 시간은 시작 시간보다 이후여야 합니다.");
                return;
            }

            // 카테고리 정보
            CategoryItem selectedCategory = (CategoryItem) categoryCombo.getSelectedItem();
            String categoryName = (selectedCategory != null) ? selectedCategory.getName() : "미지정";
            Color categoryColor = (selectedCategory != null) ? selectedCategory.getColor() : Color.LIGHT_GRAY;
            
            String startStr = sHourStr + " " + sMinStr;
            String endStr = eHourStr + " " + eMinStr;
            long groupId = System.currentTimeMillis();

            // --- [NEW] 날짜 루프 저장 로직 ---
            Calendar startDate = (Calendar) selectedDate.clone();
            Calendar endDate = (Calendar) selectedDate.clone();

            if (multiDayCheckBox.isSelected()) {
                // 종료 날짜 설정
                int y = (Integer) endYearCombo.getSelectedItem();
                int m = (Integer) endMonthCombo.getSelectedItem() - 1; // Calendar 월은 0부터
                int d = (Integer) endDayCombo.getSelectedItem();
                endDate.set(y, m, d);
                
                // 날짜 역전 체크
                if (endDate.before(startDate)) {
                    JOptionPane.showMessageDialog(this, "종료 날짜가 시작 날짜보다 이전입니다.");
                    return;
                }
            }
            // (체크 안 하면 startDate == endDate)

            // 시작 날짜부터 종료 날짜까지 하루씩 증가하며 저장
            Calendar tempCal = (Calendar) startDate.clone();
            
            // 날짜 비교를 위해 시간 초기화 (날짜만 비교)
            clearTime(tempCal);
            clearTime(endDate);

            while (!tempCal.after(endDate)) {
                String dateKey = String.format("%04d-%02d-%02d", 
                        tempCal.get(Calendar.YEAR), 
                        tempCal.get(Calendar.MONTH) + 1, 
                        tempCal.get(Calendar.DAY_OF_MONTH));

                // 해당 날짜의 Vector 가져오기 (없으면 생성)
                if (!globalScheduleData.containsKey(dateKey)) {
                    globalScheduleData.put(dateKey, new Vector<>());
                }
                Vector<ScheduleItem> targetList = globalScheduleData.get(dateKey);

                // 아이템 생성 및 추가
                ScheduleItem newItem = new ScheduleItem(groupId, title, categoryName, startStr, endStr, categoryColor, dateKey);
                targetList.add(newItem);

                // 만약 지금 추가하는 날짜가 '현재 보고 있는 목록(currentDayVector)'과 같은 날짜라면 UI 즉시 갱신
                // (참조 비교 대신 날짜 문자열 비교가 안전)
                String currentViewDateKey = String.format("%04d-%02d-%02d", 
                        selectedDate.get(Calendar.YEAR), 
                        selectedDate.get(Calendar.MONTH) + 1, 
                        selectedDate.get(Calendar.DAY_OF_MONTH));
                
                if (dateKey.equals(currentViewDateKey) && currentDayUiModel != null) {
                    currentDayUiModel.addElement(newItem);
                }

                // 하루 증가
                tempCal.add(Calendar.DATE, 1);
            }

            dispose(); // 창 닫기
            
        } else if (source == cancelButton) {
            dispose();
        }
    }

    // Calendar 시간 정보 초기화 (날짜 비교용)
    private void clearTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private void refreshCategoryCombo() {
        categoryCombo.setModel(new DefaultComboBoxModel<>(categories));
    }
}