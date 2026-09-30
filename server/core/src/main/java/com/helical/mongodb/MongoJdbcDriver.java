package com.helical.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class MongoJdbcDriver implements Driver {

    private static final String PREFIX = "mongodb://";
    private static final MongoJdbcDriver INSTANCE = new MongoJdbcDriver();

    static {
        try {
            java.sql.DriverManager.registerDriver(INSTANCE);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to register MongoDB JDBC driver", e);
        }
    }

    public static MongoJdbcDriver getInstance() {
        return INSTANCE;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (url == null || !acceptsURL(url)) {
            return null;
        }

        try {
            MongoClientURI mongoUri = new MongoClientURI(url);
            MongoClient client = new MongoClient(mongoUri);

            String databaseName = mongoUri.getDatabase();

            if (databaseName == null || databaseName.isEmpty()) {
                client.close();
                throw new SQLException("MongoDB database name is required");
            }

            MongoDatabase database = client.getDatabase(databaseName);

            return (Connection) Proxy.newProxyInstance(
                    MongoJdbcDriver.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    new MongoJdbcConnectionHandler(client, database)
            );

        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Unable to connect to MongoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean acceptsURL(String url) {
        return url != null &&
                (url.startsWith("mongodb://") || url.startsWith("mongodb+srv://"));
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
