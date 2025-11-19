package calendars;

import javax.swing.*;
import java.awt.*;

/**
 * 일정 목록(List)의 각 항목을 그려주는 렌더러입니다.
 * 카테고리 텍스트에만 배경색을 적용하여 보여줍니다.
 */
public class ScheduleRenderer implements ListCellRenderer<ScheduleItem> {
    
    private JPanel panel;
    private JLabel categoryLabel;
    private JLabel timeLabel;
    private JLabel titleLabel;

    public ScheduleRenderer() {
        // 항목들을 가로로 나열 (왼쪽 정렬)
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        
        // 1. 카테고리 라벨 (배경색 적용 대상)
        categoryLabel = new JLabel();
        categoryLabel.setOpaque(true); // 배경색이 보이도록 불투명 설정
        categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 2. 시간, 제목 라벨
        timeLabel = new JLabel();
        titleLabel = new JLabel();
        
        panel.add(categoryLabel);
        panel.add(timeLabel);
        panel.add(titleLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ScheduleItem> list, ScheduleItem value, 
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        
        if (value != null) {
            // [카테고리] 설정
            categoryLabel.setText(" " + value.getCategory() + " "); // 여백 살짝 추가
            categoryLabel.setBackground(value.getCategoryColor()); // 저장된 색상 적용
            
            // [시간] 설정
            timeLabel.setText(value.getStartTime() + " ~ " + value.getEndTime());
            
            // [제목] 설정
            titleLabel.setText(value.getTitle());
        }

        // 리스트 선택 상태에 따른 배경/글자색 처리
        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
            timeLabel.setForeground(list.getSelectionForeground());
            titleLabel.setForeground(list.getSelectionForeground());
            // 카테고리는 배경색이 있으므로 글자색은 검정으로 고정 (가독성)
            categoryLabel.setForeground(Color.BLACK); 
        } else {
            panel.setBackground(list.getBackground());
            timeLabel.setForeground(list.getForeground());
            titleLabel.setForeground(list.getForeground());
            categoryLabel.setForeground(Color.BLACK);
        }
        
        return panel;
    }
}