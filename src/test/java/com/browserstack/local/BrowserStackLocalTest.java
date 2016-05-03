package com.browserstack.local;

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
    public void killBrowserStackLocal() {
        Runtime rt = Runtime.getRuntime();
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                rt.exec("taskkill /F /IM " + BrowserStackLocalLauncher.BIN_BASENAME + ".exe");
            } else {
                rt.exec("kill -9 " + BrowserStackLocalLauncher.BIN_BASENAME);
            }

            Thread.sleep(1000);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    public void testStartStop() {
        try {
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey).start();
            assertFalse(launcher.isStopped());
            assertTrue(launcher.getLaunchResult().pid > 0);
            assertTrue(launcher.getLaunchResult().checkConnected());

            BrowserStackLocalCmdResult stopResult = launcher.stop();
            assertTrue(stopResult.checkSuccessful());
            assertTrue(launcher.isStopped());

            try {
                launcher.stop();
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
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey)
                    .setForce(true)
                    .start();

            assertOption(launcher, "-force", true);
            launcher.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionForceLocal() {
        try {
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey)
                    .setLocalIdentifier("server-1")
                    .setForce(true)
                    .setOnlyAutomate(true)
                    .setForceLocal(true)
                    .setOnly("localhost,3000")
                    .setProxy("proxy.example.com", 3128, "username", "password")
                    .setHosts("localhost,3000,0")
                    .start();

            assertOption(launcher, "-forcelocal", true);
            launcher.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionLocalIdentifier() {
        try {
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey)
                    .setLocalIdentifier("123")
                    .start();

            assertOption(launcher, "-localIdentifier 123", true);
            launcher.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionOnlyAutomate() {
        try {
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey)
                    .setOnlyAutomate(true)
                    .start();

            assertOption(launcher, "-onlyAutomate", true);
            launcher.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionOnly() {
        try {
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey)
                    .setOnly("localhost,3000,0")
                    .start();

            assertOption(launcher, "-only localhost,3000,0", true);
            launcher.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionFolderTestingPath() {
        String localFolder = System.getProperty("user.home");

        try {
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey)
                    .setFolderTestingPath(localFolder)
                    .start();

            assertOption(launcher, "-f " + localFolder, true);
            launcher.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionExtra() {
        try {
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey)
                    .appendArgument("-vvv")
                    .start();

            assertOption(launcher, "-vvv", true);
            launcher.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testOptionProxy() {
        try {
            BrowserStackLocalLauncher launcher = new BrowserStackLocal(accessKey)
                    .setProxy("localhost", 3128, "username", "password")
                    .start();

            String commandLine = Arrays.toString(launcher.buildCommand(LocalOption.DAEMON_START));
            assertTrue(commandLine.contains("-proxyHost localhost"));
            assertTrue(commandLine.contains("-proxyPort 3128"));
            assertTrue(commandLine.contains("-proxyUser username"));
            assertTrue(commandLine.contains("-proxyPass password"));

            launcher.stop();
        } catch (BrowserStackLocalException e) {
            assertTrue(e.hasResult());
            assertTrue(e.getResult().message.contains("Could not connect"));
        }
    }

    private static void assertOption(BrowserStackLocalLauncher launcher, String matchOption, boolean checkStarted) {
        StringBuilder sb = new StringBuilder();
        for (String arg : launcher.buildCommand(LocalOption.DAEMON_START)) {
            sb.append(" ").append(arg);
        }

        String commandLine = sb.toString().trim();
        assertTrue(commandLine.contains(" " + accessKey));
        assertTrue(commandLine.contains(" " + matchOption));

        assertTrue(launcher.getLaunchResult().pid > 0);

        if (checkStarted) {
            assertTrue(launcher.getLaunchResult().checkConnected());
        }
    }
}
