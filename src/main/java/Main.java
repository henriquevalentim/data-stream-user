import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Main {

    static final String BROKERS = "localhost:9092";

    public static void main(String[] args) throws Exception {
        // Step 1: Create the StreamExecutionEnvironment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        System.out.println("Environment created");

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
        DataStreamSource<User> kafka = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        System.out.println("Kafka source created");

        // Step 4: Add JDBC sink to the Kafka DataStream
        kafka.addSink(JdbcSink.sink(
                "INSERT INTO users (id, name, email, genre, register_date) VALUES (?, ?, ?, ?, ?)",
                (statement, user) -> {
                    statement.setString(1, user.id);
                    statement.setString(2, user.name);
                    statement.setString(3, user.email);
                    statement.setString(4, user.genre);
                    statement.setTimestamp(5, user.registerDate);
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                        .withUrl("jdbc:postgresql://localhost:5432/postgres")
                        .withDriverName("org.postgresql.Driver")
                        .withUsername("postgres")
                        .withPassword("postgres")
                        .build()
        )).name("PostgreSQL Sink");

        // Step 5: Execute the Flink job
        env.execute("Kafka-flink-postgres");
    }
}
