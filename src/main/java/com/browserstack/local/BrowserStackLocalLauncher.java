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

    public BrowserStackLocal start() throws BrowserStackLocalException {
        return instance.start();
    }

    public BrowserStackLocalLauncher setExecutionTimeout(long executionTimeout) {
        instance.setExecutionTimeout(executionTimeout);
        return this;
    }

    public BrowserStackLocalLauncher setVerbose(boolean enable) {
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.VERBOSE, enable);
        return this;
    }

    public BrowserStackLocalLauncher setLocalIdentifier(String localIdentifier) {
        instance.getOptions().add(new LocalOption(LocalFlag.LOCAL_IDENTIFIER, localIdentifier));
        return this;
    }

    public BrowserStackLocalLauncher setForce(boolean enable) {
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.FORCE, enable);
        return this;
    }

    public BrowserStackLocalLauncher setForceLocal(boolean enable) {
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.FORCELOCAL, enable);
        return this;
    }

    public BrowserStackLocalLauncher setOnly(String target) {
        instance.getOptions().add(new LocalOption(LocalFlag.ONLY, target));
        return this;
    }

    public BrowserStackLocalLauncher setOnlyAutomate(boolean enable) {
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.ONLY_AUTOMATE, enable);
        return this;
    }

    public BrowserStackLocalLauncher setProxy(String host, int port) {
        instance.getOptions().add(new LocalOption(LocalFlag.PROXY_HOST, host));
        instance.getOptions().add(new LocalOption(LocalFlag.PROXY_PORT, port + ""));
        return this;
    }

    public BrowserStackLocalLauncher setProxy(String host, int port, boolean forceProxy) {
        setProxy(host, port);
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.FORCE_PROXY, forceProxy);
        return this;
    }

    public BrowserStackLocalLauncher setProxy(String host, int port, String username, String password) {
        setProxy(host, port);
        instance.getOptions().add(new LocalOption(LocalFlag.PROXY_USER, username));
        instance.getOptions().add(new LocalOption(LocalFlag.PROXY_PASS, password));
        return this;
    }

    public BrowserStackLocalLauncher setProxy(String host, int port, String username, String password, boolean forceProxy) {
        setProxy(host, port, username, password);
        LocalOption.toggleFlag(instance.getOptions(), LocalFlag.FORCE_PROXY, forceProxy);
        return this;
    }

    public BrowserStackLocalLauncher setFolderTestingPath(String folderPath) {
        instance.getOptions().add(new LocalOption(LocalFlag.FOLDER_TESTING, folderPath));
        return this;
    }

    public BrowserStackLocalLauncher setHosts(String hosts) {
        instance.getOptions().add(new LocalOption(LocalFlag.HOSTS, hosts));
        return this;
    }

    public BrowserStackLocalLauncher appendArgument(String argument) {
        instance.appendArgument(argument);
        return this;
    }
}
