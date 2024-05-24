import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import Deserializer.AddressDeserializationSchema;
import Deserializer.AddressToDocumentConverter;
import Deserializer.UserDeserializationSchema;
import Deserializer.UserToDocumentConverter;
import Dto.Address;
import Dto.User;
import Sink.MongoSink;

public class Main {

    private static final String BROKERS = "localhost:9092";
    private static final String MONGO_USERNAME = "root";
    private static final String MONGO_PASSWORD = "123456";
    private static final String MONGO_URI = "mongodb://" + MONGO_USERNAME + ":" + MONGO_PASSWORD + "@localhost:27017";
    private static final String MONGO_DATABASE_USER = "users";
    private static final String MONGO_COLLECTION_USER = "users";

    private static final String MONGO_DATABASE_ADDRESS = "address";
    private static final String MONGO_COLLECTION_ADDRESS = "address";

    public static void main(String[] args) throws Exception {

        // Step 1: Create the StreamExecutionEnvironment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Step 2: Define Kafka source
        KafkaSource<User> sourceUser = KafkaSource.<User>builder()
                .setBootstrapServers(BROKERS)
                .setProperty("partition.discovery.interval.ms", "1000")
                .setTopics("user")
                .setGroupId("groupId-919292")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new UserDeserializationSchema())
                .build();

            KafkaSource<Address> sourceAddress = KafkaSource.<Address>builder()
                .setBootstrapServers(BROKERS)
                .setProperty("partition.discovery.interval.ms", "1000")
                .setTopics("address")
                .setGroupId("groupId-919293")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new AddressDeserializationSchema())
                .build();

        // Step 3: Create a DataStreamSource from Kafka
        DataStreamSource<User> usersStream = env.fromSource(sourceUser, WatermarkStrategy.noWatermarks(), "Kafka Source user");
        DataStreamSource<Address> addressStream = env.fromSource(sourceAddress, WatermarkStrategy.noWatermarks(), "Kafka Source address");

         // MongoDB sink for users
         usersStream.addSink(new MongoSink<>(MONGO_URI, MONGO_DATABASE_USER, MONGO_COLLECTION_USER, new UserToDocumentConverter()))
         .name("MongoDB Sink for Users");

         addressStream.addSink(new MongoSink<>(MONGO_URI, MONGO_DATABASE_ADDRESS, MONGO_COLLECTION_ADDRESS, new AddressToDocumentConverter()))
         .name("MongoDB Sink for address");

        // Step 5: Execute the Flink job
        env.execute("Kafka-flink-postgres-mongo");
    }
    
}