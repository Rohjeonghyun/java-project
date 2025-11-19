package calendars;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class CalendarPanel extends JPanel implements ActionListener {

    // --- UI 컴포넌트 ---
    private JLabel yearMonthLabel; // "YYYY년 MM월" 표시 레이블
    private JButton prevButton;    // 이전 달 버튼
    private JButton nextButton;    // 다음 달 버튼
    private JPanel daysGridPanel;  // 날짜(1~31) 버튼들을 담을 그리드 패널
    private JButton[] dayButtons = new JButton[42]; // 6주(42일)치 버튼 배열

    // --- 달력 로직 ---
    private Calendar cal; // 현재 달력 정보를 가진 Calendar 객체
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월");
    private Vector<String> categories;
    
    // 날짜별 일정 목록을 저장할 Map
    // Key: "yyyy-MM-dd" (날짜 문자열), Value: 그 날의 일정 목록(Vector)
    private Map<String, Vector<ScheduleItem>> scheduleData = new HashMap<>();
    
    public CalendarPanel(Vector<String> categories) {
    	this.categories = categories;
        // 1. 메인 패널 레이아웃 설정 (BorderLayout)
        setLayout(new BorderLayout(10, 10)); // 상하좌우 10픽셀 간격
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10)); // 패널 전체의 패딩

        cal = Calendar.getInstance(); // 현재 날짜로 Calendar 객체 생성

        // 2. 상단 네비게이션 패널 (BorderLayout.NORTH)
        JPanel navPanel = buildNavigationPanel();
        add(navPanel, BorderLayout.NORTH);

        // 3. 중앙 캘린더 패널 (BorderLayout.CENTER)
        JPanel centerPanel = buildCalendarGridPanel();
        add(centerPanel, BorderLayout.CENTER);

        updateCalendar();
    }

    /*
     상단의 '이전', '년/월', '다음' 버튼이 있는 네비게이션 패널을 생성
     */
    private JPanel buildNavigationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        prevButton = new JButton("<");
        nextButton = new JButton(">");
        
        // 버튼에 액션 리스너 추가
        prevButton.addActionListener(this);
        nextButton.addActionListener(this);

        yearMonthLabel = new JLabel("", SwingConstants.CENTER);
        yearMonthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        panel.add(prevButton, BorderLayout.WEST);
        panel.add(yearMonthLabel, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.EAST);

        return panel;
    }

    /*
      중앙의 '요일'과 '날짜' 그리드를 포함하는 메인 캘린더 패널을 생성
     */
    private JPanel buildCalendarGridPanel() {
        // 요일과 날짜 그리드를 담을 전체 컨테이너
        JPanel panel = new JPanel(new BorderLayout(0, 5)); // 요일-날짜 그리드 사이 5픽셀 간격
        panel.setBackground(Color.WHITE);

        // 3a. 요일 패널 (GridLayout 1x7)
        JPanel daysOfWeekPanel = new JPanel(new GridLayout(1, 7, 5, 5)); // 컴포넌트간 5픽셀 간격
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

     // 날짜 그리드 패널 (GridLayout 6x7)
        daysGridPanel = new JPanel(new GridLayout(6, 7, 5, 5)); // 6주, 7일
        daysGridPanel.setBackground(Color.WHITE);
        
        for (int i = 0; i < 42; i++) {
            dayButtons[i] = new JButton("");
            dayButtons[i].setFont(new Font("맑은 고딕", Font.PLAIN, 16));
            
            dayButtons[i].setHorizontalAlignment(SwingConstants.LEFT); // 수평 정렬을 왼쪽으로
            dayButtons[i].setVerticalAlignment(SwingConstants.TOP);   // 수직 정렬을 위쪽으로

            // 버튼의 여백을 줄이
            dayButtons[i].setMargin(new Insets(2, 2, 2, 2)); 
            dayButtons[i].setFocusable(false); // 포커스 테두리 제거
            dayButtons[i].setBackground(Color.WHITE);
            dayButtons[i].addActionListener(this);
            daysGridPanel.add(dayButtons[i]);
        }
        panel.add(daysGridPanel, BorderLayout.CENTER);

        return panel;
    }

    /*
     '이전', '다음' 버튼 클릭 시 호출되는 이벤트 처리 메소드
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == prevButton) {
            cal.add(Calendar.MONTH, -1); // 현재 날짜에서 1달을 뺀다
            updateCalendar(); // 달력 UI를 새로고침
        } else if (source == nextButton) {
            cal.add(Calendar.MONTH, +1); // 현재 날짜에서 1달을 더한다
            updateCalendar(); // 달력 UI를 새로고침
        } else {
            // [이 부분이 날짜 버튼을 처리합니다]
            for (int i = 0; i < 42; i++) {
                if (source == dayButtons[i]) {
                    // 버튼에 텍스트가 있는지(활성화된 날짜인지) 확인
                    String dateText = dayButtons[i].getText();
                    if (!dateText.isEmpty()) {
                        // 클릭된 날짜(년, 월, 일) 정보로 새 창을 엽니다.
                        int day = Integer.parseInt(dateText);
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH는 0부터 시작
                        
                        openSchedule(year, month, day);
                    }
                    break; // 해당 버튼을 찾았으므로 루프 중단
                }
            }
        }
    }

    private void openSchedule(int year, int month, int day) {
    	Window parentFrame = SwingUtilities.getWindowAncestor(this);
    	// 1. 맵에서 사용할 Key 생성 (예: "2025-11-18")
        String dateKey = String.format("%04d-%02d-%02d", year, month, day);
        
        // 2. 해당 날짜의 일정 리스트 가져오기 (없으면 새로 생성)
        if (!scheduleData.containsKey(dateKey)) {
            scheduleData.put(dateKey, new Vector<>());
        }
        Vector<ScheduleItem> dailySchedules = scheduleData.get(dateKey);
        // 3. 다이얼로그 제목
    	String title = String.format("%d년 %d월 %d일 일정", year, month, day);
    	// 4. Calendar 객체 생성 (날짜 정보 전달용)
    	Calendar selecteDate = (Calendar) cal.clone();
    	selecteDate.set(Calendar.DAY_OF_MONTH, day);
    	
    	SwingUtilities.invokeLater(() -> {
    		DayScheduleDialog dialog = new DayScheduleDialog(parentFrame, title, selecteDate, dailySchedules, categories);
            dialog.setVisible(true);
        });
    }
    
    /*
     cal 객체(현재 년/월)를 기준으로 달력의 숫자와 UI를 업데이트합니다.
     */
    private void updateCalendar() {
        // "YYYY년 MM월" 레이블 업데이트
        yearMonthLabel.setText(sdf.format(cal.getTime()));

        // 모든 날짜 버튼 초기화
        for (int i = 0; i < 42; i++) {
            dayButtons[i].setText("");
            dayButtons[i].setEnabled(false); // 비활성화
            dayButtons[i].setBackground(Color.WHITE);
            dayButtons[i].setForeground(Color.BLACK);
        }

        // 이번 달의 시작 요일과 마지막 날 계산
        Calendar tempCal = (Calendar) cal.clone(); // 계산용 임시 복사본
        tempCal.set(Calendar.DAY_OF_MONTH, 1); // 날짜를 1일로 설정
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK); // 1일의 요일 (1:일, 2:월, ...)
        int totalDaysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH); // 이 달의 마지막 날

        // 날짜 버튼에 숫자
        int startIndex = firstDayOfWeek - 1; // 1일이 시작될 버튼 인덱스 (0부터 시작)
        for (int i = 0; i < totalDaysInMonth; i++) {
            int buttonIndex = startIndex + i;
            dayButtons[buttonIndex].setText(String.valueOf(i + 1));
            dayButtons[buttonIndex].setEnabled(true); // 버튼 활성화

            // (일, 토요일 색상 변경)
            int dayOfWeek = (buttonIndex % 7); // 0:일, 1:월 2:화, 3:수, 4:목, 5:금, 6:토
            if (dayOfWeek == 0) dayButtons[buttonIndex].setForeground(Color.RED);
            else if (dayOfWeek == 6) dayButtons[buttonIndex].setForeground(Color.BLUE);
            else dayButtons[buttonIndex].setForeground(Color.BLACK);
        }
        
        Calendar today = Calendar.getInstance();
        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            
            int todayDate = today.get(Calendar.DAY_OF_MONTH); // 오늘 날짜
            dayButtons[startIndex + todayDate - 1].setBackground(new Color(255, 255, 204)); // 연한 노란색
        }
    }
}