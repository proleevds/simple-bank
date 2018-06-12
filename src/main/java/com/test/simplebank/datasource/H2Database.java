package com.test.simplebank.datasource;

import org.h2.jdbcx.JdbcConnectionPool;

import javax.inject.Singleton;

@Singleton
public class H2Database {
    private static final String DB_CONNECTION = "jdbc:h2:mem:simplebank;" +
            "DB_CLOSE_DELAY=-1;" +
            "DATABASE_TO_UPPER=false";

    public static JdbcConnectionPool createDataSource() {
        return JdbcConnectionPool.create(DB_CONNECTION, "sa", "");
    }
}