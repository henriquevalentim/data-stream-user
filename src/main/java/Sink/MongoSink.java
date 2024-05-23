package Sink;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.bson.Document;

import java.util.function.Function;

public class MongoSink<T> implements SinkFunction<T> {
    private final String uri;
    private final String database;
    private final String collection;
    private final Function<T, Document> converter;

    public MongoSink(String uri, String database, String collection, Function<T, Document> converter) {
        this.uri = uri;
        this.database = database;
        this.collection = collection;
        this.converter = converter;
    }

    @Override
    public void invoke(T value, Context context) {
        try (var mongoClient = MongoClients.create(uri)) {
            MongoDatabase db = mongoClient.getDatabase(database);
            MongoCollection<Document> col = db.getCollection(collection);
            col.insertOne(converter.apply(value));
        }
    }
}
