package todo;

public class TodoItem {
	long groupId;
    String text;
    boolean done;
    boolean over;
    String time;
    String endTime;   
    String category;
    
    
    public TodoItem(long groupId,String t, boolean done, boolean over, String time,
    		String endTime,String category) {
    	
    //[FIX] 여러 날 일정 확인을 위해 groupId 추가.
    	this.groupId=groupId;
        this.text = t;
        this.done = done;
        this.over = over;
        this.time = time;
        this.endTime = endTime;
        this.category = category;
    }
    public TodoItem(String text, boolean done, boolean over, String time, String endTime,String category) {
        this(0L,text, done, over, time, endTime, category);
    }

    public TodoItem(String text) {
        this(0L,text, false, false, null, null,null);
    }

    @Override
    public String toString() {
        return text;
    }
}
