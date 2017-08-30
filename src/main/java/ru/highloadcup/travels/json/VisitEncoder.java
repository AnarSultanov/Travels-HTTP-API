package ru.highloadcup.travels.json;

import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Encoder;
import ru.highloadcup.travels.entity.Visit;

import java.io.IOException;

public class VisitEncoder implements Encoder {
    @Override public void encode(Object obj, JsonStream jsonStream) throws IOException {
        Visit visit = (Visit) obj;
        jsonStream.writeObjectStart();
        jsonStream.writeObjectField("id");
        jsonStream.writeVal(visit.getId());
        jsonStream.writeMore();
        jsonStream.writeObjectField("location");
        jsonStream.writeVal(visit.getLocation());
        jsonStream.writeMore();
        jsonStream.writeObjectField("user");
        jsonStream.writeVal(visit.getUser());
        jsonStream.writeMore();
        jsonStream.writeObjectField("visited_at");
        jsonStream.writeVal(visit.getVisitedAt());
        jsonStream.writeMore();
        jsonStream.writeObjectField("mark");
        jsonStream.writeVal(visit.getMark());
        jsonStream.writeObjectEnd();

    }
}