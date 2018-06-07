package com.test.simplebank;

import com.test.simplebank.datasource.IAccountDataSource;
import com.test.simplebank.model.Account;
import com.test.simplebank.model.MoneyTransfer;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.SQLException;

@Singleton
@Path("/")
public class AccountServiceImpl implements IAccountService {

    private final IAccountDataSource db;

    @Inject
    public AccountServiceImpl(IAccountDataSource db) {
        this.db = db;
    }

    public Response create(final Account account) {
        try {
            return Response.created(URI.create("account/" + db.createAccount(account)))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account getAccount(final Long id) {
        try {
            return db.getAccount(id)
                    .orElseThrow(() -> new NotFoundException("Account with id=" + id + " not found"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account income(final Long id, final BigDecimal value) {
        checkValueIsPositive(value);
        final Account account = getAccount(id);
        try {
            db.increaseBalance(account.getId(), value);
            return getAccount(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account outcome(final Long id, final BigDecimal value) {
        checkValueIsPositive(value);
        final Account account = getAccount(id);
        try {
            if (db.decreaseBalance(account.getId(), value)) {
                return getAccount(id);
            } else {
                throw new ProcessingException("Insufficient account balance");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account transfer(MoneyTransfer moneyTransfer) {
        checkValueIsPositive(moneyTransfer.getValue());
        final Account senderAccount = getAccount(moneyTransfer.getSenderId());
        final Account recipientAccount = getAccount(moneyTransfer.getRecipientId());
        try {
            if (db.decreaseBalance(senderAccount.getId(), moneyTransfer.getValue())) {
                db.increaseBalance(recipientAccount.getId(), moneyTransfer.getValue());
                return getAccount(senderAccount.getId());
            } else {
                throw new ProcessingException("Insufficient sender account balance");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkValueIsPositive(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new RuntimeException("Non-positive money value");
        }
    }
}
