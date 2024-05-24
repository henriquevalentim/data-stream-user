package Deserializer;

import org.bson.Document;
import Dto.Address;

import java.io.Serializable;
import java.util.function.Function;

public class AddressToDocumentConverter implements Function<Address, Document>, Serializable {
    @Override
    public Document apply(Address address) {
        Document doc = new Document();
        doc.append("userId", address.userId);
        doc.append("address", address.address);
        doc.append("city", address.city);
        doc.append("state", address.state);
        doc.append("zip_code", address.zipCode);
        doc.append("country", address.country);
        return doc;
    }
}
