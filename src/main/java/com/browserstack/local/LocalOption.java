package com.browserstack.local;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LocalOption {

    public static final String[] DAEMON_START = new String[]{"-d", "start"};
    public static final String[] DAEMON_STOP = new String[]{"-d", "stop"};

    private static final Map<LocalFlag, String> argFlags = new HashMap<LocalFlag, String>();

    static {
        argFlags.put(LocalFlag.VERBOSE, "vvv");
        argFlags.put(LocalFlag.FOLDER_TESTING, "f");
        argFlags.put(LocalFlag.FORCE, "force");
        argFlags.put(LocalFlag.ONLY, "only");
        argFlags.put(LocalFlag.FORCELOCAL, "forcelocal");
        argFlags.put(LocalFlag.LOCAL_IDENTIFIER, "localIdentifier");
        argFlags.put(LocalFlag.ONLY_AUTOMATE, "onlyAutomate");
        argFlags.put(LocalFlag.PROXY_HOST, "proxyHost");
        argFlags.put(LocalFlag.PROXY_PORT, "proxyPort");
        argFlags.put(LocalFlag.PROXY_USER, "proxyUser");
        argFlags.put(LocalFlag.PROXY_PASS, "proxyPass");
        argFlags.put(LocalFlag.FORCE_PROXY, "forceproxy");
        argFlags.put(LocalFlag.HOSTS, "hosts");
    }

    public final String arg;
    public final String[] argList;
    private final String optName;
    private final String optValue;
    private LocalFlag localFlag;

    private LocalOption(String optName, String optValue, boolean requiresName, boolean requiresValue) {
        this.optName = (optName == null) ? "" : optName.trim();
        this.optValue = (optValue == null) ? "" : optValue.trim();
        this.localFlag = LocalFlag.UNDEFINED;
        this.argList = createCmdLineArg(this, requiresName, requiresValue);

        StringBuilder sb = new StringBuilder();
        for (String s : argList) {
            sb.append(s).append(" ");
        }

        this.arg = sb.toString().trim();
    }

    public LocalOption(LocalFlag localFlag) {
        this(argFlags.get(localFlag), null, true, false);
        this.localFlag = localFlag;
    }

    public LocalOption(LocalFlag localFlag, String optValue) {
        this(argFlags.get(localFlag), optValue, true, true);
        this.localFlag = localFlag;
    }

    public LocalOption(String optValue) {
        this(null, optValue, false, true);
        this.localFlag = LocalFlag.UNDEFINED;
    }

    public static void unsetOption(Set<LocalOption> options, LocalFlag localFlag) {
        Iterator<LocalOption> iterator = options.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getLocalFlag() == localFlag) {
                iterator.remove();
                break;
            }
        }
    }

    public static void toggleFlag(Set<LocalOption> options, LocalFlag localFlag, boolean enable) {
        LocalOption localOption = new LocalOption(localFlag);
        if (enable) {
            options.add(localOption);
        } else {
            unsetOption(options, localFlag);
        }
    }

    public static String[] createCmdLineArg(LocalOption option, boolean requiresName, boolean requiresValue) {
        if (requiresName && (option.optName.isEmpty())) {
            throw new IllegalArgumentException("Invalid option");
        }

        if (requiresValue && (option.optValue.isEmpty())) {
            throw new IllegalArgumentException("Required value for option: " + option.optName);
        }

        if (!option.optName.isEmpty() && option.optValue.isEmpty()) {
            // -vvv
            return new String[]{"-" + option.optName};
        } else if (option.optName.isEmpty() && !option.optValue.isEmpty()) {
            // <access-key>
            return new String[]{option.optValue};
        }

        // -f <path>
        return new String[]{"-" + option.optName, option.optValue};
    }

    public LocalFlag getLocalFlag() {
        return localFlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalOption that = (LocalOption) o;
        return !(arg != null ? !arg.equals(that.arg) : that.arg != null);

    }

    @Override
    public int hashCode() {
        return arg != null ? arg.hashCode() : 0;
    }

    @Override
    public String toString() {
        return arg;
    }
}
