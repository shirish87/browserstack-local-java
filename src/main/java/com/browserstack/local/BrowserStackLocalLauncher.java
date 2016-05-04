package com.browserstack.local;

import com.browserstack.local.util.FileUtil;
import com.browserstack.local.util.PlatformUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BrowserStackLocalLauncher {
    public static final String BIN_BASENAME = "BrowserStackLocal";
    private static final String BIN_URL = "https://s3.amazonaws.com/browserStack/browserstack-local/";

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
    private final List<String> extraOptions = new ArrayList<String>();

    private final String accessKey;

    private final String downloadUrl;
    private final String binFilename;
    private final File binHome;
    private final File binFile;

    private long startExecutionTimeout;

    private BrowserStackLocalCmdResult startResult;
    private boolean isStopped;


    protected BrowserStackLocalLauncher(String accessKey, String binaryHome) throws BrowserStackLocalException {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            throw new BrowserStackLocalException("Access key required.");
        }

        this.accessKey = accessKey;
        this.binFilename = IS_OS_WINDOWS ? BIN_BASENAME + ".exe" : BIN_BASENAME;


        if (binaryHome != null && FileUtil.setupPaths(binaryHome, binFilename)) {
            binHome = new File(binaryHome, binFilename);
        } else {
            String[] destPaths = getBinaryHomePaths();
            destPaths = (destPaths != null) ? destPaths : DEST_PATHS;
            binHome = FileUtil.getDownloadDestination(destPaths, binFilename);
        }

        this.downloadUrl = BIN_URL + getBinaryFilename();
        this.binFile = new File(binHome, binFilename);
        this.startExecutionTimeout = DEFAULT_START_EXEC_TIMEOUT;
    }

    protected BrowserStackLocalLauncher(String accessKey) throws BrowserStackLocalException {
        this(accessKey, null);
    }

    protected BrowserStackLocalLauncher start() throws BrowserStackLocalException {
        if (!binExists()) {
            FileUtil.downloadFile(downloadUrl, binHome.getAbsolutePath(), binFilename);
        }

        try {
            startResult = this.run(buildCommand(LocalOption.DAEMON_START), startExecutionTimeout);
        } catch (IOException e) {
            if (e instanceof BrowserStackLocalException) {
                throw (BrowserStackLocalException) e;
            } else {
                throw new BrowserStackLocalException(e.getMessage());
            }
        }

        if (startResult != null && !startResult.checkConnected()) {
            throw new BrowserStackLocalException(startResult.message);
        }

        return this;
    }

    protected BrowserStackLocalCmdResult run(String[] commandLine, long executionTimeout) throws IOException {
        try {
            return PlatformUtil.execCommand(commandLine, executionTimeout);
        } catch (IOException e) {
            if (e instanceof BrowserStackLocalException) {
                throw e;
            } else {
                throw new BrowserStackLocalException(e.getMessage());
            }
        }
    }

    public BrowserStackLocalCmdResult stop() throws BrowserStackLocalException {
        checkState();

        try {
            BrowserStackLocalCmdResult result = run(buildCommand(LocalOption.DAEMON_STOP), DEFAULT_STOP_EXEC_TIMEOUT);
            isStopped = result.checkSuccessful();
            return result;
        } catch (IOException e) {
            if (e instanceof BrowserStackLocalException) {
                throw (BrowserStackLocalException) e;
            } else {
                throw new BrowserStackLocalException(e.getMessage());
            }
        }
    }

    public BrowserStackLocalCmdResult getLaunchResult() {
        checkState();
        return startResult;
    }

    public boolean isStopped() {
        return (startResult == null || isStopped);
    }

    protected void checkState() {
        if (isStopped()) {
            throw new IllegalStateException("BrowserStackLocal not running");
        }
    }

    protected boolean binExists() {
        return (binFile.exists() && binFile.isFile());
    }

    protected long getStartExecutionTimeout() {
        return startExecutionTimeout;
    }

    protected void setExecutionTimeout(long executionTimeout) {
        this.startExecutionTimeout = executionTimeout;
    }

    protected Set<LocalOption> getOptions() {
        return options;
    }

    protected boolean appendArgument(String argument) {
        if (argument != null && !extraOptions.contains(argument)) {
            return extraOptions.add(argument);
        }

        return false;
    }

    protected String[] buildCommand(String[] daemonCommand) {
        List<String> commandParts = new ArrayList<String>();
        commandParts.add(binFile.getAbsolutePath());
        commandParts.add(accessKey);
        Collections.addAll(commandParts, daemonCommand);

        for (LocalOption lo : options) {
            Collections.addAll(commandParts, lo.argList);
        }

        commandParts.addAll(extraOptions);
        return commandParts.toArray(new String[commandParts.size()]);
    }

    protected String[] getBinaryHomePaths() {
        // Use default
        return null;
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
}
