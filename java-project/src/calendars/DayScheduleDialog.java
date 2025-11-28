package calendars;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;

public class DayScheduleDialog extends JDialog implements ActionListener {

    // --- UI 컴포넌트 ---
    private JList<ScheduleItem> scheduleList;
    private DefaultListModel<ScheduleItem> listModel;
    private JButton addButton, deleteButton, closeButton;

    // --- 데이터 ---
    private Calendar selectedDate;
    private Vector<ScheduleItem> dailySchedules; 
    private Vector<CategoryItem> categories; 
    private Map<String, Vector<ScheduleItem>> globalScheduleData;
    
    // [NEW] DAO
    private CalendarDAO dao;

    /**
     * 생성자: CalendarDAO 파라미터 추가
     */
    public DayScheduleDialog(Window parent, String title, Calendar selectedDate, 
                             Vector<ScheduleItem> dailySchedules, Vector<CategoryItem> categories,
                             Map<String, Vector<ScheduleItem>> globalScheduleData,
                             CalendarDAO dao) { // [NEW]
        
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.selectedDate = selectedDate;
        this.dailySchedules = dailySchedules;
        this.categories = categories;
        this.globalScheduleData = globalScheduleData;
        this.dao = dao; // [NEW]

        // 정렬
        Collections.sort(this.dailySchedules, new Comparator<ScheduleItem>() {
            @Override
            public int compare(ScheduleItem o1, ScheduleItem o2) {
                return o1.getStartTime().compareTo(o2.getStartTime());
            }
        });

        // --- 1. 메인 패널 ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 2. 일정 목록 ---
        listModel = new DefaultListModel<>();
        listModel.addAll(this.dailySchedules);
        
        scheduleList = new JList<>(listModel);
        scheduleList.setCellRenderer(new ScheduleRenderer()); // 렌더러 적용
        scheduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(scheduleList);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- 3. 버튼 패널 ---
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

        // --- 4. 다이얼로그 설정 ---
        setContentPane(mainPanel);
        setSize(400, 500); 
        setLocationRelativeTo(null); 
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == addButton) {
            Window parent = SwingUtilities.getWindowAncestor(this);
            String title = String.format("%d년 %d월 %d일 일정 추가", 
                    selectedDate.get(Calendar.YEAR), 
                    selectedDate.get(Calendar.MONTH) + 1, 
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            
            SwingUtilities.invokeLater(() -> {
                // [MODIFIED] dao 전달
                ScheduleDialog dialog = new ScheduleDialog(
                    parent, title, selectedDate, categories, 
                    globalScheduleData, dailySchedules, listModel, dao
                );
                dialog.setVisible(true);
            });
            
        } else if (source == deleteButton) {
            ScheduleItem selectedItem = scheduleList.getSelectedValue();
            
            if (selectedItem != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "선택한 일정을 삭제하시겠습니까?\n(연결된 날짜의 일정도 모두 삭제됩니다.)", 
                        "일정 삭제", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    long targetId = selectedItem.getId();
                    
                    // 1. [DB] 삭제
                    if (dao.deleteScheduleByGroupId(targetId)) {
                        
                        // 2. 메모리(전체 맵)에서 삭제
                        for (Vector<ScheduleItem> dayList : globalScheduleData.values()) {
                            dayList.removeIf(item -> item.getId() == targetId);
                        }
                        
                        // 3. UI 새로고침
                        listModel.clear();
                        Collections.sort(dailySchedules, new Comparator<ScheduleItem>() {
                            @Override
                            public int compare(ScheduleItem o1, ScheduleItem o2) {
                                return o1.getStartTime().compareTo(o2.getStartTime());
                            }
                        });
                        listModel.addAll(dailySchedules);
                    } else {
                        JOptionPane.showMessageDialog(this, "DB 삭제 실패");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "삭제할 일정을 선택해주세요.");
            }
            
        } else if (source == closeButton) {
            SwingUtilities.invokeLater(() -> dispose());
        }
    }
}