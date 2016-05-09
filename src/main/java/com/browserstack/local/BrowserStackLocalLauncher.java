package com.browserstack.local;


public class BrowserStackLocalLauncher {

    private final BrowserStackLocal instance;

    protected BrowserStackLocalLauncher(BrowserStackLocal localInstance) throws BrowserStackLocalException {
        if (localInstance == null) {
            throw new IllegalArgumentException("Missing instance");
        }

        this.instance = localInstance;
    }

    public BrowserStackLocalLauncher(String accessKey, String binaryHome) throws BrowserStackLocalException {
        this(new BrowserStackLocal(accessKey, binaryHome));
    }

    public BrowserStackLocalLauncher(String accessKey) throws BrowserStackLocalException {
        this(new BrowserStackLocal(accessKey));
    }

    /**
     * Starts a BrowserStackLocal tunnel instance with the configured options.
     *
     * @return an instance of {@link BrowserStackLocal}.
     * @throws BrowserStackLocalException
     */
    public BrowserStackLocal start() throws BrowserStackLocalException {
        return instance.start();
    }

    /**
     * Maximum number of seconds to wait for execution of a command to complete.
     *
     * @param executionTimeout Number of seconds to wait for execution of a command.
     * @return current instance of {@link BrowserStackLocalLauncher} for chaining.
     */
    public BrowserStackLocalLauncher setExecutionTimeout(long executionTimeout) {
        instance.setExecutionTimeout(executionTimeout);
        return this;
    }

    protected BrowserStackLocalLauncher setVerbose(boolean enable) {
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.VERBOSE, enable);
        return this;
    }

    /**
     * Specify a unique identifier for each connection, while creating multiple Local Testing connections.
     *
     * @param localIdentifier Unique identifier for this BrowserStackLocal instance.
     * @return current instance of {@link BrowserStackLocalLauncher} for chaining.
     */
    public BrowserStackLocalLauncher setLocalIdentifier(String localIdentifier) {
        instance.getOptions().add(new LocalOption(LocalFlag.LOCAL_IDENTIFIER, localIdentifier));
        return this;
    }

    /**
     * Kill other running instances of BrowserStack Local.
     *
     * @param enable Set to true for enabling or false for disabling this option.
     * @return current instance of {@link BrowserStackLocalLauncher} for chaining.
     */
    public BrowserStackLocalLauncher setForce(boolean enable) {
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.FORCE, enable);
        return this;
    }

    /**
     * Route all traffic via local machine.
     *
     * @param enable Set to true for enabling or false for disabling this option.
     * @return an instance of {@link BrowserStackLocalLauncher}.
     */
    public BrowserStackLocalLauncher setForceLocal(boolean enable) {
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.FORCELOCAL, enable);
        return this;
    }

    /**
     * Restricts Local Testing access to specified local servers and/or folders.
     *
     * @param target String containing local servers and/or paths to folders. e.g. localhost,3000,0
     * @return an instance of {@link BrowserStackLocalLauncher}.
     */
    public BrowserStackLocalLauncher setOnly(String target) {
        instance.getOptions().add(new LocalOption(LocalFlag.ONLY, target));
        return this;
    }

    /**
     * Setup Local Testing only for Automate. When this flag is set,
     * Local Testing connections set up with a binary cannot be used for Live, Screenshots or Responsive.
     *
     * @param enable Set to true for enabling or false for disabling this option.
     * @return an instance of {@link BrowserStackLocalLauncher}.
     */
    public BrowserStackLocalLauncher setOnlyAutomate(boolean enable) {
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.ONLY_AUTOMATE, enable);
        return this;
    }

    /**
     * Proxy to be used to connect to BrowserStack servers.
     *
     * @param host Hostname for the proxy.
     * @param port Port for the proxy.
     * @return an instance of {@link BrowserStackLocalLauncher}.
     */
    public BrowserStackLocalLauncher setProxy(String host, int port) {
        instance.getOptions().add(new LocalOption(LocalFlag.PROXY_HOST, host));
        instance.getOptions().add(new LocalOption(LocalFlag.PROXY_PORT, port + ""));
        return this;
    }

    /**
     * Proxy to be used to connect to BrowserStack servers.
     *
     * @param host Hostname for the proxy.
     * @param port Port for the proxy.
     * @param forceProxy Set or unset forceProxy flag to always use proxy.
     * @return an instance of {@link BrowserStackLocalLauncher}.
     */
    public BrowserStackLocalLauncher setProxy(String host, int port, boolean forceProxy) {
        setProxy(host, port);
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.FORCE_PROXY, forceProxy);
        return this;
    }

    /**
     * Proxy to be used to connect to BrowserStack servers.
     *
     * @param host Hostname for the proxy.
     * @param port Port for the proxy.
     * @param username Username for authentication with the proxy.
     * @param password Password for authentication with the proxy.
     * @return an instance of {@link BrowserStackLocalLauncher}.
     */
    public BrowserStackLocalLauncher setProxy(String host, int port, String username, String password) {
        setProxy(host, port);
        instance.getOptions().add(new LocalOption(LocalFlag.PROXY_USER, username));
        instance.getOptions().add(new LocalOption(LocalFlag.PROXY_PASS, password));
        return this;
    }

    /**
     * Proxy to be used to connect to BrowserStack servers.
     *
     * @param host Hostname for the proxy.
     * @param port Port for the proxy.
     * @param username Username for authentication with the proxy.
     * @param password Password for authentication with the proxy.
     * @param forceProxy Set or unset forceProxy flag to always use proxy.
     * @return current instance of {@link BrowserStackLocalLauncher} for chaining.
     */
    public BrowserStackLocalLauncher setProxy(String host, int port, String username, String password, boolean forceProxy) {
        setProxy(host, port, username, password);
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.FORCE_PROXY, forceProxy);
        return this;
    }

    /**
     * Sets the path to the local testing folder
     *
     * @param folderPath Path to the folder containing static asset files.
     * @return current instance of {@link BrowserStackLocalLauncher} for chaining.
     */
    public BrowserStackLocalLauncher setFolderTestingPath(String folderPath) {
        instance.getOptions().add(new LocalOption(LocalFlag.FOLDER_TESTING, folderPath));
        return this;
    }

    /**
     * Passes additional arguments to the BrowserStackLocal binary.
     *
     * @param argument Any additional arguments to be passed to BrowserStackLocal binary.
     * @return current instance of {@link BrowserStackLocalLauncher} for chaining.
     */
    public BrowserStackLocalLauncher appendArgument(String argument) {
        instance.appendArgument(argument);
        return this;
    }
}
