package calendars;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
    private Map<String, Vector<ScheduleItem>> scheduleData = new HashMap<>();
    
    // [NEW] DAO
    private CalendarDAO dao;

    /**
     * [MODIFIED] 생성자: DAO 추가
     */
    public CalendarPanel(Vector<CategoryItem> categories, CalendarDAO dao) {
        this.categories = categories;
        this.dao = dao; // [NEW]

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

    // (buildNavigationPanel, buildCalendarGridPanel 생략 - 동일)
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
            // [MODIFIED] dao 전달
            DayScheduleDialog dialog = new DayScheduleDialog(parentFrame, title, selectedDate, dailySchedules, categories, this.scheduleData, dao);
            dialog.setVisible(true);
            updateCalendar(); 
        });
    }

    private void updateCalendar() {
        yearMonthLabel.setText(sdf.format(cal.getTime()));

        // [NEW] DB에서 이번 달의 모든 일정 불러오기
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        
        // 매번 DB에서 새로고침하여 최신 상태 유지
        scheduleData.clear(); 
        Vector<ScheduleItem> dbSchedules = dao.getSchedulesByMonth(currentYear, currentMonth);
        
        // DB 데이터를 Map 형태로 변환 (Key: "yyyy-MM-dd")
        for (ScheduleItem item : dbSchedules) {
            String dateKey = item.getDate();
            if (!scheduleData.containsKey(dateKey)) {
                scheduleData.put(dateKey, new Vector<>());
            }
            scheduleData.get(dateKey).add(item);
        }

        // 버튼 초기화 및 데이터 적용
        for (int i = 0; i < 42; i++) {
            dayButtons[i].setText("");
            dayButtons[i].setEnabled(false);
            dayButtons[i].setBackground(Color.WHITE);
            dayButtons[i].setForeground(Color.BLACK);
            dayButtons[i].setScheduleColors(null); 
        }

        Calendar tempCal = (Calendar) cal.clone(); 
        tempCal.set(Calendar.DAY_OF_MONTH, 1); 
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK); 
        int totalDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH); 

        int startIndex = firstDayOfWeek - 1; 
        for (int i = 0; i < totalDaysInMonth; i++) {
            int buttonIndex = startIndex + i;
            int day = i + 1;
            
            dayButtons[buttonIndex].setText(String.valueOf(day));
            dayButtons[buttonIndex].setEnabled(true); 

            int dayOfWeek = (buttonIndex % 7); 
            if (dayOfWeek == 0) dayButtons[buttonIndex].setForeground(Color.RED);
            else if (dayOfWeek == 6) dayButtons[buttonIndex].setForeground(Color.BLUE);
            else dayButtons[buttonIndex].setForeground(Color.BLACK);

            String dateKey = String.format("%04d-%02d-%02d", currentYear, currentMonth, day);
            if (scheduleData.containsKey(dateKey)) {
                Vector<ScheduleItem> schedules = scheduleData.get(dateKey);
                if (!schedules.isEmpty()) {
                    Collections.sort(schedules, new Comparator<ScheduleItem>() {
                        @Override
                        public int compare(ScheduleItem o1, ScheduleItem o2) {
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
        
        Calendar today = Calendar.getInstance();
        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            int todayDate = today.get(Calendar.DAY_OF_MONTH);
            dayButtons[startIndex + todayDate - 1].setBackground(new Color(255, 255, 204)); 
        }
        
        repaint(); 
    }
    
    private static class DayButton extends JButton {
        private List<Color> scheduleColors = null;

        public void setScheduleColors(List<Color> colors) {
            this.scheduleColors = colors;
            repaint(); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (scheduleColors != null && !scheduleColors.isEmpty()) {
                int x = 5; 
                int width = getWidth() - 9; 
                int height = 3; 
                int gap = 3; 
                FontMetrics fm = g.getFontMetrics();
                int y =fm.getHeight() + 5; 

                for (Color color : scheduleColors) {
                    if (y + height > getHeight() - 2) break;
                    g.setColor(color);
                    g.fillRect(x, y, width, height); 
                    y += (height + gap);
                }
            }
        }
    }
    public Map<String, Vector<ScheduleItem>> getScheduleData() {
        return scheduleData;
    }
}