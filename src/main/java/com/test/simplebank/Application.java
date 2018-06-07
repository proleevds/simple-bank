package com.test.simplebank;

import com.test.simplebank.datasource.IAccountDataSource;
import com.test.simplebank.datasource.InMemoryAccountDataSource;
import com.test.simplebank.model.ServiceException;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.Servlet;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

public class Application {
    public static void main(String[] args) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        Servlet servlet = new ServletContainer(createResourceConfig());
        ServletHolder servletHolder = new ServletHolder("simplebank", servlet);
        servletHolder.setInitOrder(0);
        context.addServlet(servletHolder, "/*");
        Server jettyServer = new Server(8181);
        jettyServer.setHandler(context);
        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

    public static ResourceConfig createResourceConfig() {
        return new ResourceConfig()
                .packages("com.test.simplebank")
                .register(MoxyJsonFeature.class)
                .register(new MoxyJsonConfig().resolver())
                .register(JacksonJaxbJsonProvider.class)
                .register(new AppBinder())
                .register(IAccountService.class);
    }

    public static class AppBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(new InMemoryAccountDataSource()).to(IAccountDataSource.class);
            bind(AccountServiceImpl.class).to(IAccountService.class);
        }
    }

    @Provider
    public static class RuntimeExceptionExceptionMapper implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(
                            new ServiceException(e.getMessage()))
                    .build();
        }
    }

    @Provider
    public static class NotFoundExceptionExceptionMapper implements ExceptionMapper<NotFoundException> {
        @Override
        public Response toResponse(NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(
                            new ServiceException(e.getMessage()))
                    .build();
        }
    }
}
