package ru.highloadcup.travels.json;

import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Encoder;
import ru.highloadcup.travels.entity.UsersVisit;

import java.io.IOException;

public class UsersVisitEncoder implements Encoder {
    @Override public void encode(Object obj, JsonStream jsonStream) throws IOException {
        UsersVisit usersVisit = (UsersVisit) obj;
        jsonStream.writeObjectStart();
        jsonStream.writeObjectField("mark");
        jsonStream.writeVal(usersVisit.getMark());
        jsonStream.writeMore();
        jsonStream.writeObjectField("visited_at");
        jsonStream.writeVal(usersVisit.getVisitedAt());
        jsonStream.writeMore();
        jsonStream.writeObjectField("place");
        jsonStream.writeVal(usersVisit.getPlace());
        jsonStream.writeObjectEnd();

    }
}
