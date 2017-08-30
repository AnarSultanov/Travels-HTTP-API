package ru.highloadcup.travels.json;

import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Encoder;
import ru.highloadcup.travels.entity.User;

import java.io.IOException;

public class UserEncoder implements Encoder {
    @Override public void encode(Object obj, JsonStream jsonStream) throws IOException {
        User user = (User) obj;
        jsonStream.writeObjectStart();
        jsonStream.writeObjectField("id");
        jsonStream.writeVal(user.getId());
        jsonStream.writeMore();
        jsonStream.writeObjectField("email");
        jsonStream.writeVal(user.getEmail());
        jsonStream.writeMore();
        jsonStream.writeObjectField("first_name");
        jsonStream.writeVal(user.getFirstName());
        jsonStream.writeMore();
        jsonStream.writeObjectField("last_name");
        jsonStream.writeVal(user.getLastName());
        jsonStream.writeMore();
        jsonStream.writeObjectField("gender");
        jsonStream.writeVal(user.getGender());
        jsonStream.writeMore();
        jsonStream.writeObjectField("birth_date");
        jsonStream.writeVal(user.getBirthDate());
        jsonStream.writeObjectEnd();
    }
}
