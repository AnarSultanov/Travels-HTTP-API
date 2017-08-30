package ru.highloadcup.travels.entity;

import java.util.HashSet;
import java.util.Set;

public class Location {
    private int id;
    private String place;
    private String country;
    private String city;
    private int distance;
    private Set<Visit> visits;

    public Location() {
        this.visits = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Set<Visit> getVisits() {
        return visits;
    }

    public void setVisits(Set<Visit> visits) {
        this.visits = visits;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Location location = (Location) o;

        if (id != location.id)
            return false;
        if (distance != location.distance)
            return false;
        if (place != null ? !place.equals(location.place) : location.place != null)
            return false;
        if (country != null ? !country.equals(location.country) : location.country != null)
            return false;
        if (city != null ? !city.equals(location.city) : location.city != null)
            return false;
        return visits != null ? visits.equals(location.visits) : location.visits == null;
    }

    @Override public int hashCode() {
        int result = id;
        result = 31 * result + (place != null ? place.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + distance;
        result = 31 * result + (visits != null ? visits.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "Location{" +
                "id=" + id +
                ", place='" + place + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", distance=" + distance +
                ", visits=" + visits +
                '}';
    }
}
