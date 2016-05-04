package com.browserstack.local.util;

import com.browserstack.local.BrowserStackLocalException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;


public class FileUtil {

    public static File getDownloadDestination(final List<String> destPaths, final String destFilename) {
        for (String path : destPaths) {
            if (setupPaths(path, destFilename)) {
                return new File(path, destFilename);
            }
        }

        return null;
    }

    public static boolean setupPaths(final String destPath, final String destFilename) {
        try {
            final File destDir = new File(destPath);
            if (!destDir.exists() || !destDir.isDirectory()) {
                FileUtils.forceMkdir(destDir);
            }

            final File destFile = new File(destDir, destFilename);
            if (destFile.exists() && destFile.isFile()) {
                FileUtils.forceDelete(destFile);
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }


    public static void downloadFile(final String downloadUrl, final String destPath, final String destFilename)
            throws BrowserStackLocalException {
        if (!setupPaths(destPath, destFilename)) {
            throw new BrowserStackLocalException("Failed to set up download destination (" + destPath + ").");
        }

        try {
            File destFile = new File(destPath, destFilename);
            FileUtils.copyURLToFile(new URL(downloadUrl), destFile);
            changePermissions(destFile);
        } catch (IOException e) {
            throw new BrowserStackLocalException("Error downloading binary: " + e.getMessage());
        }
    }

    public static boolean changePermissions(final File file) {
        return file.setExecutable(true, true) &&
                file.setReadable(true, true) &&
                file.setWritable(true, true);
    }
}
