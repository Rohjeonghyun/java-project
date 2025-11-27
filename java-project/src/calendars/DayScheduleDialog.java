package calendars;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * 특정 날짜의 모든 일정을 리스트로 보여주는 다이얼로그입니다.
 */
public class DayScheduleDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 ---
    private JList<ScheduleItem> scheduleList;
    private DefaultListModel<ScheduleItem> listModel;
    private JButton addButton, deleteButton, closeButton;

    // --- 데이터 ---
    private Calendar selectedDate;
    private Vector<ScheduleItem> dailySchedules; // 해당 날짜의 일정 목록
    private Vector<CategoryItem> categories; // 카테고리 목록 (ScheduleDialog에 전달용)
 // [NEW] 전체 맵 저장 변수
    private Map<String, Vector<ScheduleItem>> globalScheduleData;

    /**
     * 생성자
     */
    public DayScheduleDialog(Window parent, String title, Calendar selectedDate, 
                             Vector<ScheduleItem> dailySchedules, Vector<CategoryItem> categories,
                             Map<String, Vector<ScheduleItem>> globalScheduleData) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.selectedDate = selectedDate;
        this.dailySchedules = dailySchedules;
        this.categories = categories;
        this.globalScheduleData = globalScheduleData;
        
        Collections.sort(this.dailySchedules, new Comparator<ScheduleItem>() {
        	@Override
            public int compare(ScheduleItem o1, ScheduleItem o2) {
                // 문자열 비교 (예: "09시 00분" vs "10시 30분")
                return o1.getStartTime().compareTo(o2.getStartTime());
            }
        });

        // --- 1. 메인 패널 ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 2. 일정 목록 (CENTER) ---
        listModel = new DefaultListModel<>();
        listModel.addAll(dailySchedules); // 기존 일정 불러오기
        
        scheduleList = new JList<>(listModel);
        scheduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleList.setCellRenderer(new ScheduleRenderer());
        
        // 스크롤판에 리스트 추가
        JScrollPane scrollPane = new JScrollPane(scheduleList);
        // 목록이 없을 때 안내 메시지 표시용 패널은 생략하고 리스트만 배치
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- 3. 버튼 패널 (SOUTH) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("일정 추가");
        deleteButton = new JButton("선택 삭제");
        closeButton = new JButton("닫기");

        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        closeButton.addActionListener(this);

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- 다이얼로그 설정 ---
        setContentPane(mainPanel);
        setSize(400, 500);
        setLocationRelativeTo(null); // 화면 중앙 배치
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == addButton) {
            // 목록(Vector)을 넘겨줘서 저장을 처리
            Window parent = SwingUtilities.getWindowAncestor(this);
            String title = String.format("%d년 %d월 %d일 일정 추가", 
                    selectedDate.get(Calendar.YEAR), 
                    selectedDate.get(Calendar.MONTH) + 1, 
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            
            ScheduleDialog dialog = new ScheduleDialog(parent, title, selectedDate, categories, globalScheduleData, dailySchedules, listModel);
            dialog.setVisible(true);
            
        } else if (source == deleteButton) {
            // [선택 삭제] 버튼 클릭 시
            ScheduleItem selectedItem = scheduleList.getSelectedValue();
            
            if (selectedItem != null) {
                // 사용자 확인 (선택 사항)
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "선택한 일정을 삭제하시겠습니까?\n(연결된 날짜의 일정도 모두 삭제됩니다.)", 
                        "일정 삭제", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // 1. 삭제할 타겟 ID 확인
                    long targetId = selectedItem.getId();
                    
                    // 2. [핵심] 전체 데이터 맵을 순회하며 해당 ID를 가진 모든 일정 삭제
                    // globalScheduleData.values()는 각 날짜별 일정 리스트(Vector)들의 모음입니다.
                    for (Vector<ScheduleItem> dayList : globalScheduleData.values()) {
                        // 해당 날짜의 리스트에서 targetId와 같은 항목 제거 (Java 8 removeIf 사용)
                        dayList.removeIf(item -> item.getId() == targetId);
                    }
                    
                    // 3. 현재 보고 있는 목록(UI) 새로고침
                    listModel.clear();
                    
                    // dailySchedules는 globalScheduleData의 value를 참조하므로 이미 내용이 삭제되었음.
                    // 정렬 후 다시 UI에 추가
                    Collections.sort(dailySchedules, new Comparator<ScheduleItem>() {
                        @Override
                        public int compare(ScheduleItem o1, ScheduleItem o2) {
                            return o1.getStartTime().compareTo(o2.getStartTime());
                        }
                    });
                    
                    listModel.addAll(dailySchedules);
                }
            } else {
                JOptionPane.showMessageDialog(this, "삭제할 일정을 선택해주세요.");
            }
            
        } else if (source == closeButton) {
            SwingUtilities.invokeLater(() -> dispose());
        }
    }
}