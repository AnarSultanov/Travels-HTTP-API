package ru.highloadcup.travels.wrapper;

import ru.highloadcup.travels.entity.UsersVisit;

import java.util.List;

public class UsersVisitsWrapper {
    private List<UsersVisit> visits;

    public List<UsersVisit> getVisits() {
        return visits;
    }

    public void setVisits(List<UsersVisit> visits) {
        this.visits = visits;
    }
}
