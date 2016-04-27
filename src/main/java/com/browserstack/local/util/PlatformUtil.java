package com.browserstack.local.util;

import com.browserstack.local.BrowserStackLocalException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by shirish on 28/04/16.
 */
public class PlatformUtil {

    private static final Object execMonitor = new Object();

    public static String execCommand(final String[] command,
                                     final long executionTimeout) throws IOException {

        final BrowserStackLocalException[] exceptions = new BrowserStackLocalException[1];
        final String[] results = new String[1];

        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        final Process process = processBuilder.start();
        final Thread streamInpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    results[0] = readStreamUntilEnd(process, process.getInputStream());
                } catch (IOException e) {
                    exceptions[0] = new BrowserStackLocalException(e.getMessage());
                }
            }
        });

        final Thread streamErrThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    exceptions[0] = new BrowserStackLocalException(readStreamUntilEnd(process, process.getErrorStream()));
                } catch (IOException e) {
                    exceptions[0] = new BrowserStackLocalException(e.getMessage());
                }
            }
        });

        try {
            streamInpThread.start();
            streamErrThread.start();
            process.waitFor(executionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            process.destroyForcibly();
            streamInpThread.interrupt();
            streamErrThread.interrupt();
        }

        if (exceptions[0] != null) {
            throw exceptions[0];
        } else {
            return results[0];
        }
    }

    private static String readStreamUntilEnd(final Process process, final InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;

        while (process.isAlive() && (line = reader.readLine()) != null) {
            builder.append(line).append(System.getProperty("line.separator"));
        }

        return builder.toString();
    }
}
