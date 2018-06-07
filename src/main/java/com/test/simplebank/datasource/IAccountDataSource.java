package com.test.simplebank.datasource;

import com.test.simplebank.model.Account;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public interface IAccountDataSource {
    Long createAccount(Account account) throws SQLException;

    Optional<Account> getAccount(Long accountId) throws SQLException;

    void increaseBalance(Long accountId, BigDecimal value) throws SQLException;

    boolean decreaseBalance(Long accountId, BigDecimal value) throws SQLException;
}