package com.test.simplebank.model;

public class ServiceException {
    private String error;

    public ServiceException(String error) {
        this.error = error;
    }

    public ServiceException() {
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
