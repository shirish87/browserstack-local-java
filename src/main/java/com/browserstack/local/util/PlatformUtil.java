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

    private static final int THREAD_JOIN_TIMEOUT = 500;

    public interface ProcessLauncher {
        boolean isAlive() throws IOException;

        void start(String[] command) throws IOException;

        void join(long executionTimeout, TimeUnit timeUnit) throws IOException;

        void kill() throws IOException;

        InputStream stdout();

        InputStream stderr();
    }

    public synchronized static BrowserStackLocalCmdResult execCommand(final ProcessLauncher process,
                                                                      final String[] command,
                                                                      final long executionTimeout) throws IOException {
        final BrowserStackLocalException[] exceptions = new BrowserStackLocalException[1];
        final String[] results = new String[1];

        process.start(command);

        final Thread streamOutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    results[0] = readStreamUntilEnd(process, process.stdout());
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
                    String output = readStreamUntilEnd(process, process.stderr());
                    if (DEBUG) {
                        System.out.println("STDERR: " + output);
                    }

                    if (output.contains(STATUS_SUCCESS)) {
                        results[0] = output;
                    } else if (exceptions[0] == null && !output.isEmpty()) {
                        exceptions[0] = new BrowserStackLocalException(output, true);
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        System.out.println("STDERR: e: " + e.getMessage());
                    }

                    exceptions[0] = new BrowserStackLocalException(e.getMessage());
                }
            }
        });

        streamOutThread.start();
        streamErrThread.start();

        try {
            process.join(executionTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            process.kill();
        }

        try {
            streamOutThread.join(THREAD_JOIN_TIMEOUT);
            streamErrThread.join(THREAD_JOIN_TIMEOUT);
        } catch (InterruptedException e) {
            if (DEBUG) {
                System.err.println(e.getMessage());
            }
        }

        streamOutThread.interrupt();
        streamErrThread.interrupt();

        if (results[0] != null && results[0].trim().length() > 0) {
            return new BrowserStackLocalCmdResult(results[0]);
        } else if (exceptions[0] != null) {
            throw exceptions[0];
        } else {
            throw new BrowserStackLocalException("An unexpected error occurred.");
        }
    }


    public synchronized static BrowserStackLocalCmdResult execCommand(final String[] command,
                                                                      final long executionTimeout) throws IOException {
        return execCommand(new ProcessLauncher() {
            private Process process;

            @Override
            public void start(String[] command) throws IOException {
                process = new ProcessBuilder(command).start();
            }

            @Override
            public boolean isAlive() throws IOException {
                if (process != null) {
                    try {
                        process.exitValue();
                        return false;
                    } catch (IllegalThreadStateException e) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public void join(long executionTimeout, TimeUnit timeUnit) throws IOException {
                if (process != null) {
                    try {
                        waitFor(process, executionTimeout, timeUnit);
                    } catch (InterruptedException e) {
                        kill();
                    }
                }
            }

            @Override
            public void kill() throws IOException {
                if (process != null) {
                    process.destroy();
                }
            }

            @Override
            public InputStream stdout() {
                return (process != null) ? process.getInputStream() : null;
            }

            @Override
            public InputStream stderr() {
                return (process != null) ? process.getErrorStream() : null;
            }
        }, command, executionTimeout);
    }


    public static String readStreamUntilEnd(final ProcessLauncher processLauncher, final InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;

        while (processLauncher.isAlive() && !Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
            builder.append(line).append(System.getProperty("line.separator"));
        }

        return builder.toString();
    }

    public static boolean waitFor(Process process, long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);

        do {
            try {
                process.exitValue();
                return true;
            } catch (IllegalThreadStateException ex) {
                if (rem > 0) {
                    Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
                }
            }

            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);
        return false;
    }
}
