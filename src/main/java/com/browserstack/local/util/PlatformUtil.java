package com.browserstack.local.util;

import com.browserstack.local.BrowserStackLocalCmdResult;
import com.browserstack.local.BrowserStackLocalException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;


public class PlatformUtil {

    private static final String STATUS_SUCCESS = "success";
    private static final boolean DEBUG = false;

    public synchronized static BrowserStackLocalCmdResult execCommand(final String[] command,
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

                    if (DEBUG) {
                        System.out.println("STDOUT: " + results[0]);
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        System.out.println("STDOUT: e: " + e.getMessage());
                    }

                    exceptions[0] = new BrowserStackLocalException(e.getMessage());
                }
            }
        });

        final Thread streamErrThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String output = readStreamUntilEnd(process, process.getErrorStream());
                    if (DEBUG) {
                        System.out.println("STDERR: " + output);
                    }

                    if (output.contains(STATUS_SUCCESS)) {
                        results[0] = output;
                    } else if (exceptions[0] == null && !output.isEmpty()) {
                        throw new BrowserStackLocalException(output);
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        System.out.println("STDERR: e: " + e.getMessage());
                    }

                    exceptions[0] = new BrowserStackLocalException(e.getMessage());
                }
            }
        });

        streamInpThread.start();
        streamErrThread.start();

        try {
            process.waitFor(executionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            process.destroyForcibly();
        }

        try {
            streamInpThread.interrupt();
            streamInpThread.join(500);

            streamErrThread.interrupt();
            streamErrThread.join(500);
        } catch (InterruptedException e) {
            // ignore

            if (DEBUG) {
                System.err.println(e.getMessage());
            }
        }

        if (results[0] != null) {
            return new BrowserStackLocalCmdResult(results[0]);
        } else if (exceptions[0] != null) {
            throw exceptions[0];
        } else {
            throw new BrowserStackLocalException("An unexpected error occurred.");
        }
    }


    private static String readStreamUntilEnd(final Process process, final InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;

        while (process.isAlive() && !Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
            builder.append(line).append(System.getProperty("line.separator"));
        }

        return builder.toString();
    }
}
