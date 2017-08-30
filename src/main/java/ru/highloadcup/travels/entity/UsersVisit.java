package ru.highloadcup.travels.entity;


public class UsersVisit {
    private int mark;
    private long visitedAt;
    private String place;

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public long getVisitedAt() {
        return visitedAt;
    }

    public void setVisitedAt(long visitedAt) {
        this.visitedAt = visitedAt;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UsersVisit that = (UsersVisit) o;

        if (mark != that.mark)
            return false;
        if (visitedAt != that.visitedAt)
            return false;
        return place != null ? place.equals(that.place) : that.place == null;
    }

    @Override public int hashCode() {
        int result = mark;
        result = 31 * result + (int) (visitedAt ^ (visitedAt >>> 32));
        result = 31 * result + (place != null ? place.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "UsersVisit{" +
                "mark=" + mark +
                ", visitedAt=" + visitedAt +
                ", place='" + place + '\'' +
                '}';
    }
}
