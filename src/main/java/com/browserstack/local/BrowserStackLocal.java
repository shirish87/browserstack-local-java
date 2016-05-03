package com.browserstack.local;


public class BrowserStackLocal {

    private final BrowserStackLocalLauncher launcher;

    protected BrowserStackLocal(BrowserStackLocalLauncher localLauncher) throws BrowserStackLocalException {
        if (localLauncher == null) {
            throw new IllegalArgumentException("Missing launcher");
        }

        this.launcher = localLauncher;
    }

    public BrowserStackLocal(String accessKey, String binaryHome) throws BrowserStackLocalException {
        this(new BrowserStackLocalLauncher(accessKey, binaryHome));
    }

    public BrowserStackLocal(String accessKey) throws BrowserStackLocalException {
        this(new BrowserStackLocalLauncher(accessKey));
    }

    public BrowserStackLocalLauncher start() throws BrowserStackLocalException {
        return launcher.start();
    }

    public BrowserStackLocal setExecutionTimeout(long executionTimeout) {
        launcher.setExecutionTimeout(executionTimeout);
        return this;
    }

    public BrowserStackLocal setVerbose(boolean enable) {
        LocalOption.toggleFlag(launcher.getOptions(), LocalFlag.VERBOSE, enable);
        return this;
    }

    public BrowserStackLocal setLocalIdentifier(String localIdentifier) {
        launcher.getOptions().add(new LocalOption(LocalFlag.LOCAL_IDENTIFIER, localIdentifier));
        return this;
    }

    public BrowserStackLocal setForce(boolean enable) {
        LocalOption.toggleFlag(launcher.getOptions(), LocalFlag.FORCE, enable);
        return this;
    }

    public BrowserStackLocal setForceLocal(boolean enable) {
        LocalOption.toggleFlag(launcher.getOptions(), LocalFlag.FORCELOCAL, enable);
        return this;
    }

    public BrowserStackLocal setOnlyAutomate(boolean enable) {
        LocalOption.toggleFlag(launcher.getOptions(), LocalFlag.ONLY_AUTOMATE, enable);
        return this;
    }

    public BrowserStackLocal setProxy(String host, int port) {
        launcher.getOptions().add(new LocalOption(LocalFlag.PROXY_HOST, host));
        launcher.getOptions().add(new LocalOption(LocalFlag.PROXY_PORT, port + ""));
        return this;
    }

    public BrowserStackLocal setProxy(String host, int port, boolean forceProxy) {
        setProxy(host, port);
        LocalOption.toggleFlag(launcher.getOptions(), LocalFlag.FORCE_PROXY, forceProxy);
        return this;
    }

    public BrowserStackLocal setProxy(String host, int port, String username, String password) {
        setProxy(host, port);
        launcher.getOptions().add(new LocalOption(LocalFlag.PROXY_USER, username));
        launcher.getOptions().add(new LocalOption(LocalFlag.PROXY_PASS, password));
        return this;
    }

    public BrowserStackLocal setProxy(String host, int port, String username, String password, boolean forceProxy) {
        setProxy(host, port, username, password);
        LocalOption.toggleFlag(launcher.getOptions(), LocalFlag.FORCE_PROXY, forceProxy);
        return this;
    }

    public BrowserStackLocal setFolderTestingPath(String folderPath) {
        launcher.getOptions().add(new LocalOption(LocalFlag.FOLDER_TESTING, folderPath));
        return this;
    }

    public BrowserStackLocal setHosts(String hosts) {
        launcher.getOptions().add(new LocalOption(LocalFlag.HOSTS, hosts));
        return this;
    }

    public BrowserStackLocal appendArgument(String argument) {
        launcher.appendArgument(argument);
        return this;
    }
}
