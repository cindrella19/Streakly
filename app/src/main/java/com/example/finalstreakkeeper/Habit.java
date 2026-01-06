package com.example.finalstreakkeeper;

public class Habit {

    public String name;
    public String type;
    public int streak;
    public long lastCompletedTime;

    public Habit(String name) {
        this.name = name;
        this.type = "default";
        this.streak = 0;
        this.lastCompletedTime = 0;
    }

    public Habit(String name, String type) {
        this.name = name;
        this.type = type;
        this.streak = 0;
        this.lastCompletedTime = 0;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void setCompleted(boolean completed) {
        if (completed) {
            this.lastCompletedTime = System.currentTimeMillis();
        } else {
            this.lastCompletedTime = 0;
        }
    }

    public boolean isCompletedToday() {
        if (lastCompletedTime == 0) return false;

        java.util.Calendar last = java.util.Calendar.getInstance();
        java.util.Calendar now = java.util.Calendar.getInstance();

        last.setTimeInMillis(lastCompletedTime);
        now.setTimeInMillis(System.currentTimeMillis());

        return last.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR)
                && last.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR);
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