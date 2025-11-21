package calendars;

import java.awt.Color;

/*
 * 카테고리 데이터 클래스 (이름 + 색상)
 */
public class CategoryItem {
    private String name;
    private Color color;

    public CategoryItem(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    // JComboBox 등에서 기본적으로 텍스트를 보여주기 위해 toString 오버라이드
    @Override
    public String toString() {
        return name;
    }
}
