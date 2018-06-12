package com.test.simplebank.dao;

import com.test.simplebank.model.Account;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;

@Singleton
public class AccountDAO implements IAccountDAO {
    private final DataSource dataSource;

    @Inject
    public AccountDAO(final DataSource dataSource) {
        Objects.requireNonNull(dataSource);
        this.dataSource = dataSource;
        try {
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        final Connection connection = dataSource.getConnection();
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
        final String createAccountQuery = "INSERT INTO Account (Name, Balance) VALUES (?, ?)";
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
        final String getAccountQuery = "SELECT Name, Balance FROM Account WHERE AccountID = ?";
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

    private boolean raiseBalance(final Long accountId,
                                 final BigDecimal value,
                                 final boolean keepPositive,
                                 final Connection connection) throws SQLException {
        String updateBalanceQuery = "UPDATE Account " +
                "SET Balance = Balance + ? " +
                "WHERE AccountID = ?";
        if (keepPositive) {
            updateBalanceQuery += " AND Balance >= ?";
        }
        try (final PreparedStatement ps = connection.prepareStatement(updateBalanceQuery)) {
            ps.setBigDecimal(1, value);
            ps.setLong(2, accountId);
            if (keepPositive) {
                ps.setBigDecimal(3, value.negate());
            }
            // balance changed
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean raiseBalance(final Long accountId, final BigDecimal value, final boolean keepPositive) throws SQLException {
        try (final Connection c = getConnection()) {
            try {
                if (raiseBalance(accountId, value, keepPositive, c)) {
                    c.commit();
                    return true;
                }
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
        return false;
    }

    @Override
    public boolean transfer(final Long senderAccountId, final Long recipientAccountId,
                            final BigDecimal value) throws SQLException {
        try (final Connection c = getConnection()) {
            try {
                if (raiseBalance(senderAccountId, value.negate(), true, c)
                        && raiseBalance(recipientAccountId, value, false, c)) {
                    c.commit();
                    return true;
                }
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
        return false;
    }
}