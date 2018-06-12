package com.test.simplebank.dao;

import com.test.simplebank.model.Account;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public interface IAccountDAO {
    Long createAccount(final Account account) throws SQLException;

    Optional<Account> getAccount(final Long accountId) throws SQLException;

    // returns true if balance changed, otherwise false
    boolean raiseBalance(final Long accountId, final BigDecimal value, final boolean keepPositive) throws SQLException;

    // returns true if transfer completed successfully (sender's balance was decreased and recipients's was increased),
    //      otherwise false
    boolean transfer(final Long senderAccountId, final Long recipientAccountId, final BigDecimal value) throws SQLException;
}