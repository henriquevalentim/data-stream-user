package TableCreator;

// TableCreator.java
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.jdbc.JdbcStatementBuilder;
import org.apache.flink.streaming.api.datastream.DataStreamSource;

import Dto.User;

public class TableCreatorUser {

    private final JdbcExecutionOptions execOptions;
    private final JdbcConnectionOptions connOptions;

    public TableCreatorUser(JdbcExecutionOptions execOptions, JdbcConnectionOptions connOptions) {
        this.execOptions = execOptions;
        this.connOptions = connOptions;
    }

    public void createTables(DataStreamSource<User> usersStream) {
        // Create User Table
        usersStream.addSink(JdbcSink.sink(
            "CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(255) PRIMARY KEY, " + 
                "name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL, " +
                "genre VARCHAR(255) NOT NULL, " +
                "register_date TIMESTAMP " +
                ")", 
        (JdbcStatementBuilder<User>) (preparedStatement, users) -> {}, 
        execOptions,
        connOptions
        )).name("Create User Table Sink");

        // Create Genre Counts Table
        usersStream.addSink(JdbcSink.sink(
            "CREATE TABLE IF NOT EXISTS genre_counts (" +
                "genre VARCHAR(50) PRIMARY KEY, " + 
                "count INT " +
                ")", 
        (JdbcStatementBuilder<User>) (preparedStatement, users) -> {}, 
        execOptions,
        connOptions
        )).name("Create Genre Counts Table Sink");
    }
}
