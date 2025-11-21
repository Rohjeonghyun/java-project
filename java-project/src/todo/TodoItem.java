package todo;

public class TodoItem {
    String text;    // 할일 내용
    boolean done;   // 완료 여부
    boolean over;  // 밀렸을때
    public TodoItem(String t) {
        this.text = t;
        this.done = false;
        this.over=false;
    }

    @Override
    public String toString() {
        return text;
    }
}
