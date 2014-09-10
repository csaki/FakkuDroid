package com.fakkudroid.exception;

/**
 * Created by neko on 04/07/2014.
 */
public class ConnectionException extends Exception {

    private int errorHttpCode;
    private Exception exception;

    public ConnectionException(Exception exception) {
        this(-1);
        this.exception = exception;
    }

    public ConnectionException(int errorHttpCode) {
        super();
        this.errorHttpCode = errorHttpCode;
    }

    public int getErrorHttpCode() {
        return errorHttpCode;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
