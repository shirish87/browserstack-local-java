package com.browserstack.local;

import com.browserstack.local.util.FileUtil;
import com.browserstack.local.util.PlatformUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BrowserStackLocal {
    public static final String BIN_BASENAME = "BrowserStackLocal";
    private static final String BIN_URL = "https://s3.amazonaws.com/browserStack/browserstack-local/";

    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean IS_OS_WINDOWS = OS_NAME.contains("windows");

    private final static List<String> DEST_PATHS = new ArrayList<String>(Arrays.asList(
            System.getProperty("user.home") + "/.browserstack",
            System.getProperty("user.dir"),
            System.getProperty("java.io.tmpdir")
    ));

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


    protected BrowserStackLocal(String accessKey, String binaryHome) throws BrowserStackLocalException {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            throw new BrowserStackLocalException("Access key required.");
        }

        this.accessKey = accessKey;
        this.binFilename = IS_OS_WINDOWS ? BIN_BASENAME + ".exe" : BIN_BASENAME;


        if (binaryHome != null && FileUtil.setupPaths(binaryHome, binFilename)) {
            binHome = new File(binaryHome, binFilename);
        } else {
            List<String> destPaths = getBinaryHomePaths();
            if (destPaths != null) {
                destPaths.addAll(DEST_PATHS);
            } else {
                destPaths = DEST_PATHS;
            }

            binHome = FileUtil.getDownloadDestination(destPaths, binFilename);
        }

        this.downloadUrl = BIN_URL + getBinaryFilename();
        this.binFile = new File(binHome, binFilename);
        this.startExecutionTimeout = DEFAULT_START_EXEC_TIMEOUT;
    }

    protected BrowserStackLocal(String accessKey) throws BrowserStackLocalException {
        this(accessKey, null);
    }

    protected BrowserStackLocal start() throws BrowserStackLocalException {
        if (!binExists()) {
            FileUtil.downloadFile(downloadUrl, binHome.getAbsolutePath(), binFilename);
        }

        try {
            startResult = this.run(buildCommand(LocalOption.DAEMON_START), startExecutionTimeout);
        } catch (IOException e) {
            throw wrapException(e);
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
            throw wrapException(e);
        }
    }

    /**
     * Kills the launched BrowserStackLocal tunnel instance
     *
     * @return an instance of {@link BrowserStackLocalCmdResult} containing result of command execution.
     * @throws BrowserStackLocalException
     */
    public BrowserStackLocalCmdResult stop() throws BrowserStackLocalException {
        checkState();

        try {
            BrowserStackLocalCmdResult result = run(buildCommand(LocalOption.DAEMON_STOP), DEFAULT_STOP_EXEC_TIMEOUT);
            isStopped = result.checkSuccessful();
            return result;
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    /**
     * Returns the result of the launch command {@link BrowserStackLocalLauncher#start}.
     *
     * @return an instance of {@link BrowserStackLocalCmdResult} containing result of {@link BrowserStackLocalLauncher#start} execution.
     */
    public BrowserStackLocalCmdResult getLaunchResult() {
        checkState();
        return startResult;
    }

    /**
     * Returns true if BrowserStackLocal was successfully stopped by the last call to {@link BrowserStackLocal#stop}.
     *
     * @return true or false based on successful {@link BrowserStackLocal#stop}.
     */
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
        return (argument != null && !extraOptions.contains(argument) && extraOptions.add(argument));
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

    protected List<String> getBinaryHomePaths() {
        // Use default
        return null;
    }

    private static BrowserStackLocalException wrapException(IOException e) {
        if (e instanceof BrowserStackLocalException) {
            return (BrowserStackLocalException) e;
        } else {
            return new BrowserStackLocalException(e.getMessage());
        }
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
