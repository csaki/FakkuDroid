package com.fakkudroid.asynctask;

/**
 * Created by neko on 07/07/2014.
 */
public class ResultAsyncTask <T> {

    private T object;
    private boolean result;
    private Exception exception;

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
