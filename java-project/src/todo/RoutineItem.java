package todo;

public class RoutineItem {
    long id;              // 루틴 PK (DB 사용 시)
    String text;          // 루틴 내용
    RoutineState state;   // 현재 상태
    String startTime;     // 시작 시간 (예: "01시 00분")

    // 풀 생성자
    public RoutineItem(long id, String text, RoutineState state, String startTime) {
        this.id = id;
        this.text = text;
        this.state = state;
        this.startTime = startTime;
    }

    // id 없이: 텍스트 + 시작 시간
    public RoutineItem(String text, String startTime) {
        this(0L, text, RoutineState.TODO, startTime);
    }

    // 시작 시간 없이: 텍스트만
    public RoutineItem(String text) {
        this(0L, text, RoutineState.TODO, null);
    }

    public void nextState() {
        switch (state) {
            case TODO: state = RoutineState.DONE; break;
            case DONE: state = RoutineState.SKIP; break;
            case SKIP:
            default : state = RoutineState.TODO; break;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
