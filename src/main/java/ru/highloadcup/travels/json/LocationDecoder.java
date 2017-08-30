package ru.highloadcup.travels.json;

import com.jsoniter.JsonIterator;
import com.jsoniter.spi.Decoder;
import ru.highloadcup.travels.entity.Location;

import java.io.IOException;

public class LocationDecoder implements Decoder {
    @Override public Location decode(JsonIterator jsonIterator) throws IOException {
        Location location = new Location();
        for (String field = jsonIterator.readObject(); field != null; field = jsonIterator.readObject()) {
            switch (field) {
            case "id":
                int id = jsonIterator.readInt();
                location.setId(id);
                break;
            case "place":
                String place = jsonIterator.readString();
                if (place == null) throw new RuntimeException();
                location.setPlace(place);
                break;
            case "country":
                String country = jsonIterator.readString();
                if (country == null) throw new RuntimeException();
                location.setCountry(country);
                break;
            case "city":
                String city = jsonIterator.readString();
                if (city == null) throw new RuntimeException();
                location.setCity(city);
                break;
            case "distance":
                int dist = jsonIterator.readInt();
                location.setDistance(dist);
                break;
            default:
                jsonIterator.skip();
            }
        }
        return location;
    }
}