package com.fakkudroid.exception;

/**
 * Created by neko on 07/07/2014.
 */
public class FakkuParserException extends Exception{

    private Exception exception;
    private String html;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
