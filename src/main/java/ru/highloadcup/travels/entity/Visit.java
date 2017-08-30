package ru.highloadcup.travels.entity;

public class Visit {
    private int id;
    private int location;
    private int user;
    private long visitedAt;
    private int mark;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public long getVisitedAt() {
        return visitedAt;
    }

    public void setVisitedAt(long visitedAt) {
        this.visitedAt = visitedAt;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Visit visit = (Visit) o;

        if (id != visit.id)
            return false;
        if (location != visit.location)
            return false;
        if (user != visit.user)
            return false;
        if (visitedAt != visit.visitedAt)
            return false;
        return mark == visit.mark;
    }

    @Override public int hashCode() {
        int result = id;
        result = 31 * result + location;
        result = 31 * result + user;
        result = 31 * result + (int) (visitedAt ^ (visitedAt >>> 32));
        result = 31 * result + mark;
        return result;
    }

    @Override public String toString() {
        return "Visit{" +
                "id=" + id +
                ", location=" + location +
                ", user=" + user +
                ", visitedAt=" + visitedAt +
                ", mark=" + mark +
                '}';
    }
}
