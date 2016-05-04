package com.browserstack.local;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BrowserStackLocalTest {
    private Local l;
    private Map<String, String> options;

    @Before
    public void setUp() throws Exception {
        l = new Local();
        options = new HashMap<String, String>();
        options.put("key", System.getenv("BROWSERSTACK_ACCESS_KEY"));
    }

    @Test
    public void testIsRunning() throws Exception {
        assertFalse(l.isRunning());
        l.start(options);
        assertTrue(l.isRunning());
    }

    private static String accessKey;

    @BeforeClass
    public static void setUp() {
        accessKey = System.getenv("BROWSERSTACK_KEY");
    }

    @Before
    @After
    public void killBrowserStackLocal() {
        Runtime rt = Runtime.getRuntime();
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                rt.exec("taskkill /F /IM " + BrowserStackLocal.BIN_BASENAME + ".exe");
            } else {
                rt.exec("killall -9 " + BrowserStackLocal.BIN_BASENAME);
            }

            Thread.sleep(300);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testStartStop() {
        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey).start();
            assertFalse(browserstackLocal.isStopped());
            assertTrue(browserstackLocal.getLaunchResult().pid > 0);
            assertTrue(browserstackLocal.getLaunchResult().checkConnected());

            BrowserStackLocalCmdResult stopResult = browserstackLocal.stop();
            assertTrue(stopResult.checkSuccessful());
            assertTrue(browserstackLocal.isStopped());

            try {
                browserstackLocal.stop();
            } catch (IllegalStateException e) {
                assertTrue(true);
            }

        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionForce() {
        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey)
                    .setForce(true)
                    .start();

            assertOption(browserstackLocal, "-force", true);
            browserstackLocal.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionForceLocal() {
        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey)
                    .setForceLocal(true)
                    .start();

            assertOption(browserstackLocal, "-forcelocal", true);
            browserstackLocal.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionLocalIdentifier() {
        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey)
                    .setLocalIdentifier("123")
                    .start();

            assertOption(browserstackLocal, "-localIdentifier 123", true);
            browserstackLocal.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionOnlyAutomate() {
        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey)
                    .setOnlyAutomate(true)
                    .start();

            assertOption(browserstackLocal, "-onlyAutomate", true);
            browserstackLocal.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionOnly() {
        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey)
                    .setOnly("localhost,3000,0")
                    .start();

            assertOption(browserstackLocal, "-only localhost,3000,0", true);
            browserstackLocal.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionFolderTestingPath() {
        String localFolder = System.getProperty("user.home");

        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey)
                    .setFolderTestingPath(localFolder)
                    .start();

            assertOption(browserstackLocal, "-f " + localFolder, true);
            browserstackLocal.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionExtra() {
        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey)
                    .appendArgument("-vvv")
                    .start();

            assertOption(browserstackLocal, "-vvv", true);
            browserstackLocal.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionProxy() {
        try {
            BrowserStackLocal browserstackLocal = new BrowserStackLocalLauncher(accessKey)
                    .setProxy("localhost", 3128, "username", "password")
                    .start();

            String commandLine = Arrays.toString(browserstackLocal.buildCommand(LocalOption.DAEMON_START));
            assertTrue(commandLine.contains("-proxyHost localhost"));
            assertTrue(commandLine.contains("-proxyPort 3128"));
            assertTrue(commandLine.contains("-proxyUser username"));
            assertTrue(commandLine.contains("-proxyPass password"));

            browserstackLocal.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(e.hasResult());
            assertTrue(e.getResult().message.contains("Could not connect"));
        }
    }

    private static void assertOption(BrowserStackLocal browserstackLocal, String matchOption, boolean checkStarted) {
        StringBuilder sb = new StringBuilder();
        for (String arg : browserstackLocal.buildCommand(LocalOption.DAEMON_START)) {
            sb.append(" ").append(arg);
        }

        String commandLine = sb.toString().trim();
        assertTrue(commandLine.contains(" " + accessKey));
        assertTrue(commandLine.contains(" " + matchOption));

        assertTrue(browserstackLocal.getLaunchResult().pid > 0);

        if (checkStarted) {
            assertTrue(browserstackLocal.getLaunchResult().checkConnected());
        }
    }
}
