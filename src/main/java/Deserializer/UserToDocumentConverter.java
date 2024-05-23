package Deserializer;

import org.bson.Document;

import Dto.User;

import java.io.Serializable;
import java.util.function.Function;

public class UserToDocumentConverter implements Function<User, Document>, Serializable {
    @Override
    public Document apply(User user) {
        Document doc = new Document();
        doc.append("id", user.id);
        doc.append("name", user.name);
        doc.append("email", user.email);
        doc.append("genre", user.genre);
        doc.append("register_date", user.registerDate.toString());
        return doc;
    }
}
