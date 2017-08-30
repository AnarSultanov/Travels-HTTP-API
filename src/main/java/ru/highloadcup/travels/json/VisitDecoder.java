package ru.highloadcup.travels.json;

import com.jsoniter.JsonIterator;
import com.jsoniter.spi.Decoder;
import ru.highloadcup.travels.entity.Visit;

import java.io.IOException;

public class VisitDecoder implements Decoder {
    @Override public Visit decode(JsonIterator jsonIterator) throws IOException {
        Visit visit = new Visit();
        for (String field = jsonIterator.readObject(); field != null; field = jsonIterator.readObject()) {
            switch (field) {
            case "id":
                int id = jsonIterator.readInt();
                visit.setId(id);
                break;
            case "location":
                int location = jsonIterator.readInt();
                visit.setLocation(location);
                break;
            case "user":
                int user = jsonIterator.readInt();
                visit.setUser(user);
                break;
            case "visited_at":
                long visitedAt = jsonIterator.readInt();
                visit.setVisitedAt(visitedAt);
                break;
            case "mark":
                int mark = jsonIterator.readInt();
                visit.setMark(mark);
                break;
            default:
                jsonIterator.skip();
            }
        }
        return visit;
    }
}