package com.test.simplebank.datasource;

import com.test.simplebank.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class InMemoryAccountDataSource implements IAccountDataSource {
    private static final String DB_CONNECTION = "jdbc:h2:mem:simplebank;" +
            "DB_CLOSE_DELAY=-1;" +
            "DATABASE_TO_UPPER=false";

    public InMemoryAccountDataSource() {
        try {
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection getConnection() throws SQLException {
        final Connection connection = DriverManager.getConnection(DB_CONNECTION);
        Objects.requireNonNull(connection);
        connection.setAutoCommit(false);
        return connection;
    }

    private void createTables() throws SQLException {
        try (final Connection c = getConnection(); final Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS Account (" +
                    "AccountID LONG NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "Name VARCHAR(255), " +
                    "Balance DECIMAL(10, 2)" +
                    ")");
            c.commit();
        }
    }

    @Override
    public Long createAccount(final Account account) throws SQLException {
        String createAccountQuery = "INSERT INTO Account (Name, Balance) VALUES (?, ?)";
        try (final Connection c = getConnection();
             final PreparedStatement ps = c.prepareStatement(createAccountQuery, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getName());
            ps.setBigDecimal(2, account.getBalance());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Failed to create account, account was not inserted");
            }
            c.commit();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("Failed to determine created account id");
                }
            }
        }
    }

    @Override
    public Optional<Account> getAccount(final Long accountId) throws SQLException {
        String getAccountQuery = "SELECT Name, Balance FROM Account WHERE AccountID = ?";
        try (final Connection c = getConnection();
             final PreparedStatement ps = c.prepareStatement(getAccountQuery)) {
            ps.setLong(1, accountId);
            if (ps.execute()) {
                try (final ResultSet rs = ps.getResultSet()) {
                    if (rs != null && rs.next()) {
                        return Optional.of(
                                new Account(accountId,
                                        rs.getString(1),
                                        rs.getBigDecimal(2)));
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void increaseBalance(final Long accountId, final BigDecimal value) throws SQLException {
        String getAccountQuery = "UPDATE Account SET Balance = Balance + ? WHERE AccountID = ?";
        try (final Connection c = getConnection(); final PreparedStatement ps = c.prepareStatement(getAccountQuery)) {
            ps.setBigDecimal(1, value);
            ps.setLong(2, accountId);
            ps.executeUpdate();
            c.commit();
        }
    }

    @Override
    public boolean decreaseBalance(final Long accountId, final BigDecimal value) throws SQLException {
        String getAccountQuery = "UPDATE Account " +
                "SET Balance = Balance - ? " +
                "WHERE AccountID = ? " +
                "   AND Balance >= ?";
        try (final Connection c = getConnection(); final PreparedStatement ps = c.prepareStatement(getAccountQuery)) {
            ps.setBigDecimal(1, value);
            ps.setLong(2, accountId);
            ps.setBigDecimal(3, value);
            if (ps.executeUpdate() > 0) {
                // balance changed
                c.commit();
                return true;
            } else return false;
        }
    }
}