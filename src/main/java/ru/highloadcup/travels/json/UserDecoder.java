package ru.highloadcup.travels.json;

import com.jsoniter.JsonIterator;
import com.jsoniter.spi.Decoder;
import ru.highloadcup.travels.entity.User;

import java.io.IOException;

public class UserDecoder implements Decoder {
    @Override public User decode(JsonIterator jsonIterator) throws IOException {
        User user = new User();
        for (String field = jsonIterator.readObject(); field != null; field = jsonIterator.readObject()) {
            switch (field) {
            case "id":
                int id = jsonIterator.readInt();
                user.setId(id);
                break;
            case "email":
                String email = jsonIterator.readString();
                if (email == null) throw new RuntimeException();
                user.setEmail(email);
                break;
            case "first_name":
                String firstName = jsonIterator.readString();
                if (firstName == null) throw new RuntimeException();
                user.setFirstName(firstName);
                break;
            case "last_name":
                String lastName = jsonIterator.readString();
                if (lastName == null) throw new RuntimeException();
                user.setLastName(lastName);
                break;
            case "gender":
                String gender = jsonIterator.readString();
                if (gender.length() != 1 || (!gender.equals("m") && !gender.equals("f"))) throw new RuntimeException();
                user.setGender(gender);
                break;
            case "birth_date":
                long birthdate = jsonIterator.readLong();
                user.setBirthDate(birthdate);
                break;
            default:
                jsonIterator.skip();
            }
        }
        return user;
    }
}
