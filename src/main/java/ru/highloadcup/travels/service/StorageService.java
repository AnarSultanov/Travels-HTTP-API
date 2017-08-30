package ru.highloadcup.travels.service;

import com.jsoniter.JsonIterator;
import ru.highloadcup.travels.entity.Location;
import ru.highloadcup.travels.entity.User;
import ru.highloadcup.travels.entity.UsersVisit;
import ru.highloadcup.travels.entity.Visit;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class StorageService {

    public static final StorageService INSTANCE = new StorageService();

    private StorageService(){}

    private static User[] users = new User[1200000];
    private static Location[] locations = new Location[1200000];
    private static Visit[] visits = new Visit[12000000];

    public void readInitData(String dataPath) {
        try (Stream<Path> paths = Files.walk(Paths.get(dataPath))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    if (path.getFileName().toString().contains("users")) {
                        JsonIterator parse = JsonIterator.parse(Files.readAllBytes(path));
                        parse.readObject();
                        while (parse.readArray()) {
                            insertUser(parse.read(User.class));
                        }
                        parse.close();
                    } else if (path.getFileName().toString().contains("locations")) {
                        JsonIterator parse = JsonIterator.parse(Files.readAllBytes(path));
                        parse.readObject();
                        while (parse.readArray()) {
                            insertLocation(parse.read(Location.class));
                        }
                        parse.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.gc();
        try (Stream<Path> paths = Files.walk(Paths.get(dataPath))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    if (path.getFileName().toString().contains("visits")) {
                        JsonIterator parse = JsonIterator.parse(Files.readAllBytes(path));
                        parse.readObject();
                        while (parse.readArray()) {
                            insertVisit(parse.read(Visit.class));
                        }
                        parse.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.gc();
    }

    public boolean userIsPresent(int id) {
        return id < 1200000 && users[id] != null;
    }

    public boolean locationIsPresent(int id) {
        return id < 1200000 && locations[id] != null;
    }

    public boolean visitIsPresent(int id) {
        return id < 1200000 && visits[id] != null;
    }

    public void insertUser(User user) {
        if (users[user.getId()] != null) throw new RuntimeException();
        users[user.getId()] = user;
    }

    public void insertLocation(Location location) {
        if (locations[location.getId()] != null) throw new RuntimeException();
        locations[location.getId()] = location;
    }

    public void insertVisit(Visit visit) {
        if (visits[visit.getId()] != null) throw new RuntimeException();
        visits[visit.getId()] = visit;
        users[visit.getUser()].getVisits().add(visit);
        locations[visit.getLocation()].getVisits().add(visit);
    }

    public User findUserById(int userId) {
        if(userId >= 1200000) return null;
        return users[userId];
    }

    public Location findLocationById(int locationId) {
        if(locationId >= 1200000) return null;
        return locations[locationId];
    }

    public Visit findVisitById(int visitId) {
        if(visitId >= 12000000) return null;
        return visits[visitId];
    }

    public void updateUser(int id, String firstName, String lastName,
            String gender, Long birthDate, String email) {
        User existingUser = users[id];
        if(firstName != null ) existingUser.setFirstName(firstName);
        if(lastName != null ) existingUser.setLastName(lastName);
        if(gender != null ) existingUser.setGender(gender);
        if(birthDate != null ) existingUser.setBirthDate(birthDate);
        if(email != null ) existingUser.setEmail(email);
    }

    public void updateLocation(int id, String country, String city,
            String place, Integer distance) {
        Location existingLocation = locations[id];
        if(country != null ) existingLocation.setCountry(country);
        if(city != null ) existingLocation.setCity(city);
        if(place != null ) existingLocation.setPlace(place);
        if(distance != null ) existingLocation.setDistance(distance);
    }

    public void updateVisit(int id, Integer location, Integer user,
            Long visitedAt, Integer mark) {

        Visit existingVisit = visits[id];

        if(user != null && !user.equals(existingVisit.getUser())) {
            users[existingVisit.getUser()].getVisits().remove(existingVisit);
            users[user].getVisits().add(existingVisit);
            existingVisit.setUser(user);
        }

        if(location != null && !location.equals(existingVisit.getLocation())) {
            locations[existingVisit.getLocation()].getVisits().remove(existingVisit);
            locations[location].getVisits().add(existingVisit);
            existingVisit.setLocation(location);
        }

        if(visitedAt != null ) existingVisit.setVisitedAt(visitedAt);
        if(mark != null ) existingVisit.setMark(mark);
    }

    public List<UsersVisit> findUsersVisits(int userId, Long fromDate, Long toDate,
            String country, Integer toDistance) {
        List<UsersVisit> usersVisits = new ArrayList<>();

        for (Visit visit: users[userId].getVisits()) {
            if(fromDate != null && visit.getVisitedAt() < fromDate) continue;
            if(toDate != null && visit.getVisitedAt() > toDate) continue;
            if(country != null && !locations[visit.getLocation()].getCountry().equals(country)) continue;
            if(toDistance != null && locations[visit.getLocation()].getDistance() >= toDistance) continue;
            UsersVisit uv = new UsersVisit();
            uv.setMark(visit.getMark());
            uv.setVisitedAt(visit.getVisitedAt());
            uv.setPlace(locations[visit.getLocation()].getPlace());
            usersVisits.add(uv);
        }
        usersVisits.sort(Comparator.comparing(UsersVisit::getVisitedAt));
        return usersVisits;
    }

    public double getAverageMarkForLocation(Integer locationId, Long fromDate, Long toDate,
            Long maxBirthDate, Long minBirthDate, String gender) {
        List<Integer> marks = new ArrayList<>();
        for (Visit visit: locations[locationId].getVisits()) {
            if(fromDate != null && visit.getVisitedAt() < fromDate) continue;
            if(toDate != null && visit.getVisitedAt() > toDate) continue;
            if(minBirthDate != null && users[visit.getUser()].getBirthDate() < minBirthDate) continue;
            if(maxBirthDate != null && users[visit.getUser()].getBirthDate() > maxBirthDate) continue;
            if(gender != null && !(users[visit.getUser()].getGender().equals(gender))) continue;
            marks.add(visit.getMark());
        }
        return calcAverage(marks);
    }

    private double calcAverage(List<Integer> marks) {
        if (marks.isEmpty()) return 0.0;
        double sum = 0;
        for(double mark: marks) {
            sum += mark;
        }
        double averageMark = sum/(marks.size());
        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.HALF_UP);
        String formattedDouble = df.format(averageMark);

        return Double.parseDouble(formattedDouble);
    }
}

