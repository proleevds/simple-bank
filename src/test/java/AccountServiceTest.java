import com.test.simplebank.model.Account;
import com.test.simplebank.model.MoneyTransfer;
import com.test.simplebank.model.ServiceException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;

import static org.junit.Assert.assertTrue;

public class AccountServiceTest extends BaseJerseyTest {
    @Test
    public void createAccount() {
        final String name = "test";
        final BigDecimal balance = BigDecimal.TEN;

        final Response createdResponse = sendPost(
                "account/create",
                new Account(name, balance));
        Assert.assertTrue(createdResponse.getStatus() == Response.Status.CREATED.getStatusCode());
        Assert.assertTrue(createdResponse.getLocation() != null);

        final Response requestedResponse = target(createdResponse.getLocation().getPath()).request().get();
        check_200_OK(requestedResponse);
        final Account account = requestedResponse.readEntity(Account.class);
        assertTrue(name.equals(account.getName()));
        assertTrue(balance.compareTo(account.getBalance()) == 0);
    }

    @Test
    public void testZeroAndNegativeIncome() {
        final String name = "test";
        final BigDecimal balance = BigDecimal.TEN;
        final URI accountURI = createAccount(name, balance);

        final Response successfulResponse = sendPost(
                accountURI.getPath() + "/income",
                "{\"value\":" + BigDecimal.ZERO + "}");
        check_400_BAD_REQUEST(successfulResponse);
        assertTrue("Non-positive money value"
                .equals(successfulResponse.readEntity(ServiceException.class).getError()));

        final Response failedResponse = sendPost(
                accountURI.getPath() + "/income",
                "{\"value\":" + BigDecimal.TEN.negate() + "}");
        check_400_BAD_REQUEST(failedResponse);
        assertTrue("Non-positive money value"
                .equals(failedResponse.readEntity(ServiceException.class).getError()));
    }

    @Test
    public void testIncome() {
        final String name = "test";
        final BigDecimal balance = BigDecimal.TEN;
        final BigDecimal income = BigDecimal.ONE;
        final URI accountURI = createAccount(name, balance);

        final Response incomeResponse = sendPost(
                accountURI.getPath() + "/income",
                "{\"value\":" + income + "}");
        check_200_OK(incomeResponse);
        final Account account = incomeResponse.readEntity(Account.class);
        assertTrue(name.equals(account.getName()));
        assertTrue(balance.add(income)
                .compareTo(account.getBalance()) == 0);
    }

    @Test
    public void testOutcome() {
        final String name = "test";
        final BigDecimal balance = BigDecimal.ONE;
        final BigDecimal outcome = BigDecimal.ONE;
        final URI accountURI = createAccount(name, balance);

        final Response successfulOutcome = sendPost(
                accountURI.getPath() + "/outcome",
                "{\"value\":" + outcome + "}");
        check_200_OK(successfulOutcome);
        final Account account = successfulOutcome.readEntity(Account.class);
        assertTrue(name.equals(account.getName()));
        assertTrue(balance.subtract(outcome)
                .compareTo(account.getBalance()) == 0);

        final Response failedOutcome = sendPost(
                accountURI.getPath() + "/outcome",
                "{\"value\":" + outcome + "}");
        check_400_BAD_REQUEST(failedOutcome);
        assertTrue("Insufficient account balance"
                .equals(failedOutcome.readEntity(ServiceException.class).getError()));
    }

    @Test
    public void testTransfer() {
        final String senderAccountName = "test1";
        final BigDecimal senderAccountBalance = new BigDecimal("50.11");
        final String recipientAccountName = "test2";
        final BigDecimal recipientAccountBalance = new BigDecimal("66.33");
        final BigDecimal transferValue = new BigDecimal("35.19");

        final Account createdSenderAccount = target(createAccount(senderAccountName, senderAccountBalance).getPath())
                .request().get().readEntity(Account.class);
        final Account createdRecipientAccount = target(createAccount(recipientAccountName, recipientAccountBalance).getPath())
                .request().get().readEntity(Account.class);

        final Response successfulTransfer = sendPost(
                "moneyTransfer",
                new MoneyTransfer(
                        createdSenderAccount.getId(),
                        createdRecipientAccount.getId(),
                        transferValue));
        check_200_OK(successfulTransfer);
        final Account returnedSenderAccount = successfulTransfer.readEntity(Account.class);

        final Account senderAccountAfterSucceededTransfer = target("account/" + createdSenderAccount.getId())
                .request().get().readEntity(Account.class);
        final Account recipientAccountAfterSucceededTransfer = target("account/" + createdRecipientAccount.getId())
                .request().get().readEntity(Account.class);

        assertTrue(returnedSenderAccount.equals(senderAccountAfterSucceededTransfer));

        assertTrue(createdSenderAccount.getBalance().subtract(transferValue)
                .compareTo(senderAccountAfterSucceededTransfer.getBalance()) == 0);
        assertTrue(createdRecipientAccount.getBalance().add(transferValue)
                .compareTo(recipientAccountAfterSucceededTransfer.getBalance()) == 0);

        final Response failedTransfer = sendPost(
                "moneyTransfer",
                new MoneyTransfer(
                        createdSenderAccount.getId(),
                        createdRecipientAccount.getId(),
                        transferValue));
        check_400_BAD_REQUEST(failedTransfer);
        assertTrue("Insufficient sender account balance"
                .equals(failedTransfer.readEntity(ServiceException.class).getError()));
    }

    @Test
    public void nonexistentAccount() {
        final long id = 11L;
        final Response response = target("account/" + id).request().get();
        assertTrue(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
        assertTrue(
                response.readEntity(ServiceException.class).getError()
                        .equals(String.format("Account with id=%s not found", id)));
    }


}
