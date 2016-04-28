package com.browserstack.local;

import org.json.JSONException;
import org.json.JSONObject;

public class BrowserStackLocalCmdResult {

    public final String rawOutput;

    public final int pid;
    public final String status;
    public final String state;
    public final String message;

    public BrowserStackLocalCmdResult(String rawOutput) throws BrowserStackLocalException {
        this.rawOutput = rawOutput;

        try {
            JSONObject result = new JSONObject(rawOutput);
            pid = result.optInt("pid");
            status = result.optString("status", "");
            state = result.optString("state", "");
            message = result.optString("message", "");
        } catch (JSONException e) {
            throw new BrowserStackLocalException(e.getMessage());
        }
    }

    public boolean checkConnected() {
        return (state != null && state.equals("connected"));
    }

    @Override
    public String toString() {
        return rawOutput;
    }
}
