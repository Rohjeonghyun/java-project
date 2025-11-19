package calendars;

/**
 * 일정 하나의 정보를 담는 데이터 클래스입니다.
 * (일정명, 카테고리, 시작/종료 시간 등)
 */
public class ScheduleItem {
    private String title;
    private String category;
    private String startTime;
    private String endTime;

    public ScheduleItem(String title, String category, String startTime, String endTime) {
        this.title = title;
        this.category = category;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    
    // 리스트에 보여질 텍스트 형식 정의 (예: "[학교] 10:00 ~ 12:00 강의")
    @Override
    public String toString() {
        return String.format("[%s] %s ~ %s  %s", category, startTime, endTime, title);
    }
}