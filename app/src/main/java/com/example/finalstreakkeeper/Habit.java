package com.example.finalstreakkeeper;

public class Habit {

    // 🔹 Firestore document ID
    private String id;

    // 🔹 Firestore fields
    private String title;
    private String type;
    private int streak;
    private boolean completedToday;
    private long lastCompletedTime;

    // ✅ REQUIRED empty constructor (Firestore needs this)
    public Habit() {}

    // Optional constructors (for manual creation)
    public Habit(String title) {
        this.title = title;
        this.type = "default";
        this.streak = 0;
        this.completedToday = false;
        this.lastCompletedTime = 0;
    }

    public Habit(String title, String type) {
        this.title = title;
        this.type = type;
        this.streak = 0;
        this.completedToday = false;
        this.lastCompletedTime = 0;
    }

    // 🔹 GETTERS
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public int getStreak() { return streak; }
    public boolean isCompletedToday() { return completedToday; }
    public long getLastCompletedTime() { return lastCompletedTime; }

    // 🔹 SETTERS (Firestore uses these)
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setType(String type) { this.type = type; }
    public void setStreak(int streak) { this.streak = streak; }
    public void setCompletedToday(boolean completedToday) { this.completedToday = completedToday; }
    public void setLastCompletedTime(long lastCompletedTime) { this.lastCompletedTime = lastCompletedTime; }

    // 🔹 Helper logic (kept from your code)
    public void markCompleted() {
        this.completedToday = true;
        this.lastCompletedTime = System.currentTimeMillis();
    }

    public boolean wasCompletedYesterday() {
        if (lastCompletedTime == 0) return false;

        java.util.Calendar last = java.util.Calendar.getInstance();
        java.util.Calendar yesterday = java.util.Calendar.getInstance();

        last.setTimeInMillis(lastCompletedTime);
        yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1);

        return last.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR)
                && last.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR);
    }
}
