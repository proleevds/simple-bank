import com.test.simplebank.model.Account;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.junit.Assert;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;

public class BaseJerseyTest extends JerseyTest {
    public BaseJerseyTest() throws TestContainerException {
        super();
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
    }

    @Override
    protected Application configure() {
        return com.test.simplebank.Application.createResourceConfig();
    }

    Response sendPost(String path, Object entity) {
        return target(path)
                .request()
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(
                        entity,
                        MediaType.APPLICATION_JSON_TYPE));
    }

    URI createAccount(String name, BigDecimal balance) {
        final Response response = sendPost(
                "account/create",
                new Account(name, balance));
        return response.getLocation();
    }

    void check_200_OK(final Response response) {
        Assert.assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
    }

    void check_400_BAD_REQUEST(final Response response) {
        Assert.assertTrue(response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
    }
}
