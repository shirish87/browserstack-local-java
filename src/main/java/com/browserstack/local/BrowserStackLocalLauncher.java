package com.browserstack.local;

import com.browserstack.local.util.FileUtil;
import com.browserstack.local.util.PlatformUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

class BrowserStackLocalLauncher {
    private static final String BIN_URL = "https://s3.amazonaws.com/browserStack/browserstack-local/";
    private static final String BIN_BASENAME = "BrowserStackLocal";

    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean IS_OS_WINDOWS = OS_NAME.contains("windows");

    private final static String[] DEST_PATHS = {
            System.getProperty("user.home") + "/.browserstack",
            System.getProperty("user.dir"),
            System.getProperty("java.io.tmpdir")
    };

    private static final int DEFAULT_START_EXEC_TIMEOUT = 300;
    private static final int DEFAULT_STOP_EXEC_TIMEOUT = 20;

    private final Set<LocalOption> options = new HashSet<LocalOption>();

    private final String accessKey;

    private final String downloadUrl;
    private final String binFilename;
    private final File binHome;
    private final File binFile;

    private long startExecutionTimeout;

    BrowserStackLocalLauncher(String accessKey, String binaryHome) throws BrowserStackLocalException {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            throw new BrowserStackLocalException("Access key required.");
        }

        this.accessKey = accessKey;
        this.binFilename = IS_OS_WINDOWS ? BIN_BASENAME + ".exe" : BIN_BASENAME;


        if (binaryHome != null && FileUtil.setupPaths(binaryHome, binFilename)) {
            binHome = new File(binaryHome, binFilename);
        } else {
            binHome = FileUtil.getDownloadDestination(DEST_PATHS, binFilename);
        }

        this.downloadUrl = BIN_URL + getBinaryFilename();
        this.binFile = new File(binHome, binFilename);
        this.startExecutionTimeout = DEFAULT_START_EXEC_TIMEOUT;
    }

    BrowserStackLocalLauncher(String accessKey) throws BrowserStackLocalException {
        this(accessKey, null);
    }

    private static String getBinaryFilename() throws BrowserStackLocalException {
        if (IS_OS_WINDOWS) {
            return BIN_BASENAME + ".exe";
        } else if (OS_NAME.contains("mac") || OS_NAME.contains("darwin")) {
            return BIN_BASENAME + "-darwin-x64";
        } else if (OS_NAME.contains("linux")) {
            String arch = System.getProperty("os.arch");
            return BIN_BASENAME + "-linux-" + (arch.contains("64") ? "x64" : "ia32");
        } else {
            throw new BrowserStackLocalException("Unsupported operating system: " + OS_NAME);
        }
    }

    BrowserStackLocalLauncher start() throws BrowserStackLocalException {
        if (!binExists()) {
            FileUtil.downloadFile(downloadUrl, binHome.getAbsolutePath(), binFilename);
        }

        final BrowserStackLocalCmdResult result;
        try {
            result = PlatformUtil.execCommand(buildCommand(LocalOption.DAEMON_START), startExecutionTimeout);
        } catch (IOException e) {
            throw new BrowserStackLocalException(e.getMessage());
        }

        if (result != null && !result.checkConnected()) {
            throw new BrowserStackLocalException(result.message);
        }

        return this;
    }

    public BrowserStackLocalCmdResult stop() throws BrowserStackLocalException {
        try {
            return PlatformUtil.execCommand(buildCommand(LocalOption.DAEMON_STOP), DEFAULT_STOP_EXEC_TIMEOUT);
        } catch (IOException e) {
            throw new BrowserStackLocalException(e.getMessage());
        }
    }

    private boolean binExists() {
        return (binFile.exists() && binFile.isFile());
    }

    void setExecutionTimeout(long executionTimeout) {
        this.startExecutionTimeout = executionTimeout;
    }

    Set<LocalOption> getOptions() {
        return options;
    }

    protected String[] buildCommand(String[] daemonCommand) {
        List<String> commandParts = new ArrayList<String>();
        commandParts.add(binFile.getAbsolutePath());
        commandParts.add(accessKey);
        Collections.addAll(commandParts, daemonCommand);

        for (LocalOption lo : options) {
            Collections.addAll(commandParts, lo.argList);
        }

        return commandParts.toArray(new String[commandParts.size()]);
    }
}
