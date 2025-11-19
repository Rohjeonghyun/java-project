package calendars;

import javax.swing.*;
import java.awt.*;

/**
 * 리스트나 콤보박스에서 카테고리를 보여줄 때,
 * 텍스트 길이에 맞춰서만 배경색을 입혀주는 렌더러입니다.
 */
public class CategoryRenderer implements ListCellRenderer<CategoryItem> {
    
    private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList<? extends CategoryItem> list, CategoryItem value, 
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        
        // 1. 기본 렌더러 설정을 가져옴 (폰트, 선택색상 등 처리 위임)
        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value != null) {
            // 2. 텍스트 설정
            renderer.setText(value.getName());
            
            // 3. "텍스트 길이만큼만" 배경색을 칠하기 위해 라벨 자체에 색상 적용
            renderer.setOpaque(true); // 배경색이 보이도록 불투명 설정
            renderer.setBackground(value.getColor()); // 카테고리 고유 색상
            
            // 선택되었을 때 글자색 처리 (가독성 확보)
            if (isSelected) {
                // 선택된 상태면 테두리 등을 표현하거나, 여기선 심플하게 유지
            }
        }

        // 4. 라벨(renderer)을 감싸는 패널 생성
        // FlowLayout.LEFT를 쓰면 라벨이 텍스트 길이만큼만 줄어들고 왼쪽에 붙음
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        panel.add(renderer);

        // 5. 패널의 배경색 처리 (리스트의 선택 상태에 따라)
        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
        } else {
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }
        
        return panel;
    }
}
