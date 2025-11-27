package todo;

public class TodoItem {
    String text;
    boolean done;
    boolean over;
    String time;
    String endTime;   

    public TodoItem(String t, boolean done, boolean over, String time, String endTime) {
        this.text = t;
        this.done = done;
        this.over = over;
        this.time = time;
        this.endTime = endTime;
    }

    public TodoItem(String text) {
        this(text, false, false, null, null);
    }

    @Override
    public String toString() {
        return text;
    }
}
