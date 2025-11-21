package todo;

public class RoutineItem {
    String text;                 // 루틴 내용
    RoutineState state;          // 현재 상태

    public RoutineItem(String t) {
        this.text = t;
        this.state = RoutineState.TODO;
    }

    public void nextState() {
        switch (state) {
            case TODO:
                state = RoutineState.DONE;
                break;
            case DONE:
                state = RoutineState.SKIP;
                break;
            case SKIP:
            default:
                state = RoutineState.TODO;
                
                break;
        }
    }

    @Override
    public String toString() {
        return text;
    }
}
