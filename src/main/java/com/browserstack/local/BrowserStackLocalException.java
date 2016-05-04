package com.browserstack.local;

import java.io.IOException;

public class BrowserStackLocalException extends IOException {

    private String message;
    private BrowserStackLocalCmdResult result;

    public BrowserStackLocalException(String message) {
        super(message);
        this.message = message;
    }

    public BrowserStackLocalException(String message, boolean attemptParse) {
        this(message);

        if (attemptParse) {
            try {
                result = new BrowserStackLocalCmdResult(message);
            } catch (BrowserStackLocalException e) {
                // ignore
            }
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    public boolean hasResult() {
        return (result != null);
    }

    public BrowserStackLocalCmdResult getResult() {
        return result;
    }
}
