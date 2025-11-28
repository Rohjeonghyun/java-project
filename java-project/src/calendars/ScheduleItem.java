package calendars;

import java.awt.Color;

public class ScheduleItem {
    // 일정 그룹 ID (여러 날에 걸친 일정 삭제 시 식별용)
    private long id;
    
    private String title;       // 일정 내용
    private String category;    // 카테고리 이름
    private Color categoryColor;// 카테고리 색상
    private String date;        // 날짜 (yyyy-MM-dd)
    private String startTime;   // 시작 시간
    private String endTime;     // 종료 시간

    // [중요] CalendarDAO에서 호출하는 생성자 (파라미터 순서 중요합니다.)
    public ScheduleItem(long id, String title, String category, Color categoryColor, String date, String startTime, String endTime) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.categoryColor = categoryColor;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public Color getCategoryColor() { return categoryColor; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    @Override
    public String toString() {
        return String.format("[%s] %s ~ %s  %s", category, startTime, endTime, title);
    }
}