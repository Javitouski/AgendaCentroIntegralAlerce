package prog.android.centroalr.model;


public class Notification {
    private int id;
    private String title;
    private String message;
    private String time;
    private boolean isRead;

    public Notification(int id, String title, String message, String time, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.isRead = isRead;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}