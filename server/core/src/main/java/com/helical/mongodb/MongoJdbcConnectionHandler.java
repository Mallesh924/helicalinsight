package com.helical.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

public class MongoJdbcConnectionHandler implements InvocationHandler {

    private final MongoClient client;
    private final MongoDatabase database;
    private boolean closed = false;

    public MongoJdbcConnectionHandler(MongoClient client, MongoDatabase database) {
        this.client = client;
        this.database = database;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String name = method.getName();

        if ("close".equals(name)) {
            if (!closed) {
                client.close();
                closed = true;
            }
            return null;
        }

        if ("isClosed".equals(name)) {
            return closed;
        }

        if ("isValid".equals(name)) {
            return !closed;
        }

        if ("getCatalog".equals(name)) {
            return database.getName();
        }

        if ("toString".equals(name)) {
            return "MongoDB JDBC Connection [" + database.getName() + "]";
        }

        if ("unwrap".equals(name)) {
            throw new SQLException("MongoDB connection does not support unwrap");
        }

        if ("isWrapperFor".equals(name)) {
            return false;
        }

        if ("getAutoCommit".equals(name)) {
            return true;
        }

        if ("setAutoCommit".equals(name) ||
            "commit".equals(name) ||
            "rollback".equals(name)) {
            return null;
        }

        if ("getTransactionIsolation".equals(name)) {
            return java.sql.Connection.TRANSACTION_NONE;
        }

        if ("getWarnings".equals(name)) {
            return null;
        }

        if ("clearWarnings".equals(name)) {
            return null;
        }

        throw new SQLException(
                "JDBC operation not supported by MongoDB driver: " + name
        );
    }
}
