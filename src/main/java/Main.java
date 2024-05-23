import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.bson.Document;

import Deserializer.UserDeserializationSchema;
import Deserializer.UserToDocumentConverter;
import Dto.GenreCount;
import Dto.User;
import Sink.MongoSink;

public class Main {

    private static final String BROKERS = "localhost:9092";
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String MONGO_USERNAME = "root";
    private static final String MONGO_PASSWORD = "123456";
    private static final String MONGO_URI = "mongodb://" + MONGO_USERNAME + ":" + MONGO_PASSWORD + "@localhost:27017";
    private static final String MONGO_DATABASE = "users";
    private static final String MONGO_COLLECTION = "users";

    public static void main(String[] args) throws Exception {

        // Step 1: Create the StreamExecutionEnvironment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Step 2: Define Kafka source
        KafkaSource<User> source = KafkaSource.<User>builder()
                .setBootstrapServers(BROKERS)
                .setProperty("partition.discovery.interval.ms", "1000")
                .setTopics("user")
                .setGroupId("groupId-919292")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new UserDeserializationSchema())
                .build();

        // Step 3: Create a DataStreamSource from Kafka
        DataStreamSource<User> usersStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        JdbcExecutionOptions execOptions = new JdbcExecutionOptions.Builder()
            .withBatchSize(1000)
            .withBatchIntervalMs(200)
            .withMaxRetries(5)
            .build();
        
        JdbcConnectionOptions connOptions = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
            .withUrl(JDBC_URL)
            .withDriverName("org.postgresql.Driver")
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .build();

        SingleOutputStreamOperator<GenreCount> genderCounts = usersStream
                .map(user -> new GenreCount(user.genre, 1))
                .returns(GenreCount.class)
                .keyBy(GenreCount::getGenre)
                .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
                .sum("count");

        usersStream.addSink(JdbcSink.sink(
            "CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(255) PRIMARY KEY, " + 
                "name VARCHAR(255)  NOT NULL, " +
                "email VARCHAR(255)  NOT NULL, " +
                "genre VARCHAR(255)  NOT NULL, " +
                "register_date TIMESTAMP " +
                ")", 
        (JdbcStatementBuilder<User>) (preparedStatement, users) -> {}, 
        execOptions,
        connOptions
        )).name("Create User Table Sink");

        usersStream.addSink(JdbcSink.sink(
            "CREATE TABLE IF NOT EXISTS genre_counts (" +
                "genre VARCHAR(50) PRIMARY KEY, " + 
                "count INT " +
                ")", 
        (JdbcStatementBuilder<User>) (preparedStatement, users) -> {}, 
        execOptions,
        connOptions
        )).name("Create genre_counts Table Sink");

        // Step 4: Add JDBC sink to the Kafka DataStream
        usersStream.addSink(JdbcSink.sink(
                "INSERT INTO users (id, name, email, genre, register_date) VALUES (?, ?, ?, ?, ?)",
                (statement, user) -> {
                    statement.setString(1, user.id);
                    statement.setString(2, user.name);
                    statement.setString(3, user.email);
                    statement.setString(4, user.genre);
                    statement.setTimestamp(5, user.registerDate);
                },
                execOptions,
                connOptions
        )).name("PostgreSQL Sink");

        genderCounts.addSink(JdbcSink.sink(
                "INSERT INTO genre_counts (genre, count) VALUES (?, ?) ON CONFLICT (genre) DO UPDATE SET count = ?",
                (statement, genderCount) -> {
                    statement.setString(1, genderCount.genre);
                    statement.setInt(2, genderCount.count);
                    statement.setInt(3, genderCount.count);
                },
                execOptions,
                connOptions
        )).name("Genre Count PostgreSQL Sink");

         // MongoDB sink for users
         usersStream.addSink(new MongoSink<>(MONGO_URI, MONGO_DATABASE, MONGO_COLLECTION, new UserToDocumentConverter()))
         .name("MongoDB Sink for Users");

        // Step 5: Execute the Flink job
        env.execute("Kafka-flink-postgres-mongo");
    }
    
}