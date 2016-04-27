package com.browserstack.local;

import java.io.IOException;

public class BrowserStackLocalException extends IOException {

    private String message;

    public BrowserStackLocalException(String message) {
        super(message);

        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
