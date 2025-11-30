package todo;

public class RoutineItem {
    long id;             
    String text;          // 루틴 내용
    RoutineState state;   // 현재 상태
    String startTime;

    public RoutineItem(long id, String t, RoutineState state) {
        this.id = id;
        this.text = t;
        this.state = state;
    }

    
    public RoutineItem(String t) {
        this(0L, t, RoutineState.TODO); 
        this.text = text;
        this.state = RoutineState.TODO;
        this.startTime = null;
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
