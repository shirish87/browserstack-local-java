package com.browserstack.local;

import com.browserstack.local.util.FileUtil;
import com.browserstack.local.util.PlatformUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by shirish on 28/04/16.
 */
public class BrowserStackLocal {

    private static final String BIN_URL = "https://s3.amazonaws.com/browserStack/browserstack-local/";
    private static final String BIN_BASENAME = "BrowserStackLocal";

    private static final String OS_NAME = System.getProperty("os.name", "").toLowerCase();
    private static final boolean IS_OS_WINDOWS = OS_NAME.contains("windows");

    private final static String[] DEST_PATHS = {
            System.getProperty("user.home") + "/.browserstack",
            System.getProperty("user.dir"),
            System.getProperty("java.io.tmpdir")
    };

    private static final Object monitor = new Object();

    private static final int DEFAULT_START_EXEC_TIMEOUT = 300;
    private static final int DEFAULT_STOP_EXEC_TIMEOUT = 20;

    private final Set<LocalOption> options = new HashSet<LocalOption>();

    private final String accessKey;

    private final String binFilename;

    private final File binHome;

    private final File binFile;

    private long startExecutionTimeout;

    protected BrowserStackLocal(String accessKey, String binaryHome) throws BrowserStackLocalException {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            throw new BrowserStackLocalException("Access key required.");
        }

        this.accessKey = accessKey;
        this.binFilename = IS_OS_WINDOWS ? BIN_BASENAME + ".exe" : BIN_BASENAME;

        if (binaryHome != null && FileUtil.setupPaths(binaryHome, binFilename)) {
            this.binHome = new File(binaryHome, binFilename);
        } else {
            this.binHome = FileUtil.getDownloadDestination(DEST_PATHS, binFilename);
        }

        this.binFile = new File(this.binHome, binFilename);
        this.startExecutionTimeout = DEFAULT_START_EXEC_TIMEOUT;
    }

    public BrowserStackLocal(String accessKey) throws BrowserStackLocalException {
        this(accessKey, null);
    }

    public String start() throws BrowserStackLocalException {
        try {
            return PlatformUtil.execCommand(buildCommand(LocalOption.DAEMON_START), startExecutionTimeout);
        } catch (IOException e) {
            throw new BrowserStackLocalException(e.getMessage());
        }
    }


    public String stop() throws BrowserStackLocalException {
        try {
            return PlatformUtil.execCommand(buildCommand(LocalOption.DAEMON_STOP), DEFAULT_STOP_EXEC_TIMEOUT);
        } catch (IOException e) {
            throw new BrowserStackLocalException(e.getMessage());
        }
    }

    private boolean binExists() {
        return (binFile.exists() && binFile.isFile());
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


    public long getExecutionTimeout() {
        return startExecutionTimeout;
    }

    public void setExecutionTimeout(long executionTimeout) {
        this.startExecutionTimeout = executionTimeout;
    }

    public void setVerbose(boolean enable) {
        LocalOption.toggleFlag(options, LocalFlag.VERBOSE, enable);
    }

    public void setLocalIdentifier(String localIdentifier) {
        options.add(new LocalOption(LocalFlag.LOCAL_IDENTIFIER, localIdentifier));
    }

    public void setForce(boolean enable) {
        LocalOption.toggleFlag(options, LocalFlag.FORCE, enable);
    }

    public void setForceLocal(boolean enable) {
        LocalOption.toggleFlag(options, LocalFlag.FORCELOCAL, enable);
    }

    public void setOnlyAutomate(boolean enable) {
        LocalOption.toggleFlag(options, LocalFlag.ONLY_AUTOMATE, enable);
    }

    public void setProxy(String host, String port) {
        options.add(new LocalOption(LocalFlag.PROXY_HOST, host));
        options.add(new LocalOption(LocalFlag.PROXY_PORT, port));
    }

    public void setProxy(String host, String port, boolean forceProxy) {
        setProxy(host, port);
        LocalOption.toggleFlag(options, LocalFlag.FORCE_PROXY, forceProxy);
    }

    public void setProxy(String host, String port, String username, String password) {
        setProxy(host, port);
        options.add(new LocalOption(LocalFlag.PROXY_USER, username));
        options.add(new LocalOption(LocalFlag.PROXY_PASS, password));
    }

    public void setProxy(String host, String port, String username, String password, boolean forceProxy) {
        setProxy(host, port, username, password);
        LocalOption.toggleFlag(options, LocalFlag.FORCE_PROXY, forceProxy);
    }

    public void setFolderTestingPath(String folderPath) {
        options.add(new LocalOption(LocalFlag.FOLDER_TESTING, folderPath));
    }

    public void setHosts(String hosts) {
        options.add(new LocalOption(LocalFlag.HOSTS, hosts));
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
