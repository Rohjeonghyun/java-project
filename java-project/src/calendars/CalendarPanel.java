package calendars;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections; // [추가] 정렬을 위해 추가
import java.util.Comparator;  // [추가] 정렬 기준을 위해 추가
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CalendarPanel extends JPanel implements ActionListener {

    // --- UI 컴포넌트 ---
    private JLabel yearMonthLabel;
    private JButton prevButton, nextButton;
    private JPanel daysGridPanel;
    private DayButton[] dayButtons = new DayButton[42];

    // --- 달력 로직 ---
    private Calendar cal;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월");
    
    // --- 데이터 ---
    private Vector<CategoryItem> categories;
    
    // 날짜별 일정 목록 (Key: "yyyy-MM-dd", Value: 일정 리스트)
    private Map<String, Vector<ScheduleItem>> scheduleData = new HashMap<>();

    public CalendarPanel(Vector<CategoryItem> categories) {
        this.categories = categories;

        setLayout(new BorderLayout(10, 10)); 
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10)); 

        cal = Calendar.getInstance(); 

        JPanel navPanel = buildNavigationPanel();
        add(navPanel, BorderLayout.NORTH);

        JPanel centerPanel = buildCalendarGridPanel();
        add(centerPanel, BorderLayout.CENTER);

        updateCalendar();
    }

    private JPanel buildNavigationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        prevButton = new JButton("<");
        nextButton = new JButton(">");
        
        prevButton.addActionListener(this);
        nextButton.addActionListener(this);

        yearMonthLabel = new JLabel("", SwingConstants.CENTER);
        yearMonthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        panel.add(prevButton, BorderLayout.WEST);
        panel.add(yearMonthLabel, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildCalendarGridPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5)); 
        panel.setBackground(Color.WHITE);

        // 요일 패널
        JPanel daysOfWeekPanel = new JPanel(new GridLayout(1, 7, 5, 5)); 
        daysOfWeekPanel.setBackground(Color.WHITE);
        String[] daysOfWeek = {"일", "월", "화", "수", "목", "금", "토"};
        
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            if (day.equals("일")) dayLabel.setForeground(Color.RED);
            if (day.equals("토")) dayLabel.setForeground(Color.BLUE);
            daysOfWeekPanel.add(dayLabel);
        }
        panel.add(daysOfWeekPanel, BorderLayout.NORTH);

        // 날짜 그리드 패널
        daysGridPanel = new JPanel(new GridLayout(6, 7, 5, 5)); 
        daysGridPanel.setBackground(Color.WHITE);
        
        for (int i = 0; i < 42; i++) {
            dayButtons[i] = new DayButton();
            dayButtons[i].setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            
            dayButtons[i].setHorizontalAlignment(SwingConstants.LEFT); 
            dayButtons[i].setVerticalAlignment(SwingConstants.TOP);   

            dayButtons[i].setMargin(new Insets(2, 2, 2, 2)); 
            dayButtons[i].setFocusable(false); 
            dayButtons[i].setBackground(Color.WHITE);
            
            dayButtons[i].addActionListener(this);
            
            daysGridPanel.add(dayButtons[i]);
        }
        panel.add(daysGridPanel, BorderLayout.CENTER);

        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == prevButton) {
            cal.add(Calendar.MONTH, -1);
            updateCalendar(); 
        } else if (source == nextButton) {
            cal.add(Calendar.MONTH, +1);
            updateCalendar(); 
        } else {
            for (int i = 0; i < 42; i++) {
                if (source == dayButtons[i]) {
                    String dateText = dayButtons[i].getText();
                    if (!dateText.isEmpty()) {
                        int day = Integer.parseInt(dateText);
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH) + 1;
                        
                        openDayScheduleDialog(year, month, day);
                    }
                    break;
                }
            }
        }
    }

    private void openDayScheduleDialog(int year, int month, int day) {
        Window parentFrame = SwingUtilities.getWindowAncestor(this);
        
        String dateKey = String.format("%04d-%02d-%02d", year, month, day);
        
        if (!scheduleData.containsKey(dateKey)) {
            scheduleData.put(dateKey, new Vector<>());
        }
        Vector<ScheduleItem> dailySchedules = scheduleData.get(dateKey);
        
        String title = String.format("%d년 %d월 %d일 일정", year, month, day);
        
        Calendar selectedDate = (Calendar) cal.clone();
        selectedDate.set(Calendar.DAY_OF_MONTH, day);

        SwingUtilities.invokeLater(() -> {
            DayScheduleDialog dialog = new DayScheduleDialog(parentFrame, title, selectedDate, dailySchedules, categories);
            dialog.setVisible(true);
            
            // 창이 닫힌 후 달력 화면(라인 표시) 갱신
            updateCalendar(); 
        });
    }

    /**
     * 달력 UI 업데이트 (날짜 및 일정 라인 표시)
     */
    private void updateCalendar() {
        yearMonthLabel.setText(sdf.format(cal.getTime()));

        // 1. 버튼 초기화
        for (int i = 0; i < 42; i++) {
            dayButtons[i].setText("");
            dayButtons[i].setEnabled(false);
            dayButtons[i].setBackground(Color.WHITE);
            dayButtons[i].setForeground(Color.BLACK);
            dayButtons[i].setScheduleColors(null); // 색상 초기화
        }

        Calendar tempCal = (Calendar) cal.clone(); 
        tempCal.set(Calendar.DAY_OF_MONTH, 1); 
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK); 
        int totalDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH); 
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;

        int startIndex = firstDayOfWeek - 1; 
        for (int i = 0; i < totalDaysInMonth; i++) {
            int buttonIndex = startIndex + i;
            int day = i + 1;
            
            dayButtons[buttonIndex].setText(String.valueOf(day));
            dayButtons[buttonIndex].setEnabled(true); 

            // 요일 색상
            int dayOfWeek = (buttonIndex % 7); 
            if (dayOfWeek == 0) dayButtons[buttonIndex].setForeground(Color.RED);
            else if (dayOfWeek == 6) dayButtons[buttonIndex].setForeground(Color.BLUE);
            else dayButtons[buttonIndex].setForeground(Color.BLACK);

            // 해당 날짜의 일정 색상 정보 가져와서 버튼에 설정
            String dateKey = String.format("%04d-%02d-%02d", currentYear, currentMonth, day);
            if (scheduleData.containsKey(dateKey)) {
                Vector<ScheduleItem> schedules = scheduleData.get(dateKey);
                if (!schedules.isEmpty()) {
                    
                    // 일정을 시작 시간(startTime) 순으로 정렬
                    Collections.sort(schedules, new Comparator<ScheduleItem>() {
                        @Override
                        public int compare(ScheduleItem o1, ScheduleItem o2) {
                            // 문자열 비교 (예: "09시 00분" vs "14시 30분")
                            return o1.getStartTime().compareTo(o2.getStartTime());
                        }
                    });

                    List<Color> colors = new ArrayList<>();
                    for (ScheduleItem item : schedules) {
                        colors.add(item.getCategoryColor());
                    }
                    dayButtons[buttonIndex].setScheduleColors(colors);
                }
            }
        }
        
        // "오늘" 날짜 하이라이트
        Calendar today = Calendar.getInstance();
        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            int todayDate = today.get(Calendar.DAY_OF_MONTH);
            dayButtons[startIndex + todayDate - 1].setBackground(new Color(255, 255, 204)); 
        }
        
        repaint();
    }
    
    /**
     * 일정 라인을 그릴 수 있는 커스텀 버튼
     */
    private static class DayButton extends JButton {
        private List<Color> scheduleColors = null;

        public void setScheduleColors(List<Color> colors) {
            this.scheduleColors = colors;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 라인 그리기
            if (scheduleColors != null && !scheduleColors.isEmpty()) {
                int x = 5; // 왼쪽 여백
                int width = getWidth() - 9; // 전체 너비 - 여백
                int height = 3; // 라인 높이
                int gap = 3; // 라인 간격
               
                // 버튼의 아래쪽부터 위로 쌓아 올림
                FontMetrics fm = g.getFontMetrics();
                int y =fm.getHeight() + 5; 
               

                for (Color color : scheduleColors) {
                    if (y + height > getHeight() - 2) break;
                    
                    g.setColor(color);
                    g.fillRect(x, y, width, height); // 라인 그리기
                    
                    y += (height + gap);
                }
            }
        }
    }
}