package ru.highloadcup.travels.json;

import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Encoder;
import ru.highloadcup.travels.entity.Location;

import java.io.IOException;

public class LocationEncoder implements Encoder {
    @Override public void encode(Object obj, JsonStream jsonStream) throws IOException {
        Location location = (Location) obj;
        jsonStream.writeObjectStart();
        jsonStream.writeObjectField("id");
        jsonStream.writeVal(location.getId());
        jsonStream.writeMore();
        jsonStream.writeObjectField("place");
        jsonStream.writeVal(location.getPlace());
        jsonStream.writeMore();
        jsonStream.writeObjectField("country");
        jsonStream.writeVal(location.getCountry());
        jsonStream.writeMore();
        jsonStream.writeObjectField("city");
        jsonStream.writeVal(location.getCity());
        jsonStream.writeMore();
        jsonStream.writeObjectField("distance");
        jsonStream.writeVal(location.getDistance());
        jsonStream.writeObjectEnd();
    }
}
