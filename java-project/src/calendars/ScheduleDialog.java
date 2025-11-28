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
    
    private JCheckBox multiDayCheckBox;
    private JPanel endDatePanel;
    private JComboBox<Integer> endYearCombo, endMonthCombo, endDayCombo;

    private JComboBox<String> startHourCombo, startMinCombo;
    private JComboBox<String> endHourCombo, endMinCombo;
    private JButton manageCategoryButton, saveButton, cancelButton;

    // --- 데이터 ---
    private Vector<CategoryItem> categories; 
    private Calendar selectedDate; 
    private Map<String, Vector<ScheduleItem>> globalScheduleData;
    private Vector<ScheduleItem> currentDayVector;
    private DefaultListModel<ScheduleItem> currentDayUiModel;
    
    // [NEW] DAO 추가
    private CalendarDAO dao;

    /**
     * 생성자: CalendarDAO 파라미터 추가
     */
    public ScheduleDialog(Window parent, String title, Calendar selectedDate, 
                          Vector<CategoryItem> categories,
                          Map<String, Vector<ScheduleItem>> globalScheduleData,
                          Vector<ScheduleItem> currentDayVector, 
                          DefaultListModel<ScheduleItem> currentDayUiModel,
                          CalendarDAO dao) { // [NEW]
        
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.selectedDate = selectedDate;
        this.categories = categories;
        this.globalScheduleData = globalScheduleData;
        this.currentDayVector = currentDayVector;
        this.currentDayUiModel = currentDayUiModel;
        this.dao = dao; // [NEW]

        // --- 메인 패널 ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 입력 폼 패널 ---
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5)); 
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("일정 내용:"));
        titleField = new JTextField();
        formPanel.add(titleField);

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

        formPanel.add(new JLabel("기간 설정:"));
        multiDayCheckBox = new JCheckBox("여러 날 (Multi-day)");
        multiDayCheckBox.setBackground(Color.WHITE);
        multiDayCheckBox.addActionListener(this); 
        formPanel.add(multiDayCheckBox);

        formPanel.add(new JLabel("종료 날짜:"));
        createEndDatePanel(); 
        formPanel.add(endDatePanel);
        setEndDateEnabled(false);

        formPanel.add(new JLabel("시작 시간:"));
        JPanel startTimePanel = createTimePanel(); 
        startHourCombo = (JComboBox<String>) startTimePanel.getComponent(0);
        startMinCombo = (JComboBox<String>) startTimePanel.getComponent(1);
        formPanel.add(startTimePanel);

        formPanel.add(new JLabel("종료 시간:"));
        JPanel endTimePanel = createTimePanel(); 
        endHourCombo = (JComboBox<String>) endTimePanel.getComponent(0);
        endMinCombo = (JComboBox<String>) endTimePanel.getComponent(1);
        formPanel.add(endTimePanel);

        mainPanel.add(formPanel, BorderLayout.CENTER);

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
    
    private void createEndDatePanel() {
        endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        endDatePanel.setBackground(Color.WHITE);
        int currentYear = selectedDate.get(Calendar.YEAR);
        Vector<Integer> years = new Vector<>();
        for(int i = currentYear; i <= currentYear + 5; i++) years.add(i);
        endYearCombo = new JComboBox<>(years);
        Vector<Integer> months = new Vector<>();
        for(int i=1; i<=12; i++) months.add(i);
        endMonthCombo = new JComboBox<>(months);
        endMonthCombo.setSelectedItem(selectedDate.get(Calendar.MONTH) + 1);
        Vector<Integer> days = new Vector<>();
        for(int i=1; i<=31; i++) days.add(i);
        endDayCombo = new JComboBox<>(days);
        endDayCombo.setSelectedItem(selectedDate.get(Calendar.DAY_OF_MONTH));
        
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

    private void setEndDateEnabled(boolean enabled) {
        endYearCombo.setEnabled(enabled);
        endMonthCombo.setEnabled(enabled);
        endDayCombo.setEnabled(enabled);
        endDatePanel.setVisible(true);
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
            setEndDateEnabled(multiDayCheckBox.isSelected());

        } else if (source == manageCategoryButton) {
            // [MODIFIED] CategoryManagerDialog에도 DAO 전달
            CategoryManagerDialog manager = new CategoryManagerDialog(this, "카테고리 관리", categories, dao);
            manager.setVisible(true);
            refreshCategoryCombo();

        } else if (source == saveButton) {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "일정 내용을 입력해주세요.");
                return;
            }

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

            if (!multiDayCheckBox.isSelected() && endTimeVal <= startTimeVal) {
                JOptionPane.showMessageDialog(this, "종료 시간은 시작 시간보다 이후여야 합니다.");
                return;
            }

            CategoryItem selectedCategory = (CategoryItem) categoryCombo.getSelectedItem();
            String categoryName = (selectedCategory != null) ? selectedCategory.getName() : "미지정";
            Color categoryColor = (selectedCategory != null) ? selectedCategory.getColor() : Color.LIGHT_GRAY;
            
            String startStr = sHourStr + " " + sMinStr;
            String endStr = eHourStr + " " + eMinStr;

            long groupId = System.currentTimeMillis(); 

            Calendar startDate = (Calendar) selectedDate.clone();
            Calendar endDate = (Calendar) selectedDate.clone();

            if (multiDayCheckBox.isSelected()) {
                int y = (Integer) endYearCombo.getSelectedItem();
                int m = (Integer) endMonthCombo.getSelectedItem() - 1; 
                int d = (Integer) endDayCombo.getSelectedItem();
                endDate.set(y, m, d);
                if (endDate.before(startDate)) {
                    JOptionPane.showMessageDialog(this, "종료 날짜가 시작 날짜보다 이전입니다.");
                    return;
                }
            }

            Calendar tempCal = (Calendar) startDate.clone();
            clearTime(tempCal);
            clearTime(endDate);

            boolean isSuccess = true;

            while (!tempCal.after(endDate)) {
                String dateKey = String.format("%04d-%02d-%02d", 
                        tempCal.get(Calendar.YEAR), 
                        tempCal.get(Calendar.MONTH) + 1, 
                        tempCal.get(Calendar.DAY_OF_MONTH));

                if (!globalScheduleData.containsKey(dateKey)) {
                    globalScheduleData.put(dateKey, new Vector<>());
                }
                Vector<ScheduleItem> targetList = globalScheduleData.get(dateKey);

                ScheduleItem newItem = new ScheduleItem(groupId, title, categoryName, categoryColor, dateKey, startStr, endStr);
                
                // 1. 메모리에 추가
                targetList.add(newItem);

                // 2. [DB] DB에 추가
                if (!dao.addSchedule(newItem)) {
                    isSuccess = false;
                    System.err.println("DB 저장 실패: " + dateKey);
                }

                // 3. UI 업데이트
                String currentViewDateKey = String.format("%04d-%02d-%02d", 
                        selectedDate.get(Calendar.YEAR), 
                        selectedDate.get(Calendar.MONTH) + 1, 
                        selectedDate.get(Calendar.DAY_OF_MONTH));
                
                if (dateKey.equals(currentViewDateKey) && currentDayUiModel != null) {
                    currentDayUiModel.addElement(newItem);
                }

                tempCal.add(Calendar.DATE, 1);
            }

            if (!isSuccess) {
                JOptionPane.showMessageDialog(this, "일부 일정이 DB에 저장되지 않았습니다.");
            }

            dispose();
            
        } else if (source == cancelButton) {
            dispose();
        }
    }

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