package com.test.simplebank;

import com.test.simplebank.model.Account;
import com.test.simplebank.model.MoneyTransfer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface IAccountService {
    @POST
    @Path("account/create")
    Response create(final Account account);

    @GET
    @Path("account/{id}")
    Account getAccount(@PathParam("id") final Long id);

    @POST
    @Path("account/{id}/income")
    Account income(@PathParam("id") final Long id, final BigDecimal value);

    @POST
    @Path("account/{id}/outcome")
    Account outcome(@PathParam("id") final Long id, final BigDecimal value);

    @POST
    @Path("moneyTransfer")
    Account transfer(MoneyTransfer moneyTransfer);
}
