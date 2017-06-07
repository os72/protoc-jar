package com.github.os72.protocjar;

import java.io.*;
import java.util.Arrays;

public class LinuxProtocRecoverer implements ProtocRunnerRecoverer {
    private static final int FAILED_RECOVERY = -1;
    private final File protocJarFile;
    private final String[] protocArguments;
    private final IOException ioException;

    public LinuxProtocRecoverer(File protocJarFile, String[] protocArguments, IOException ioException) {
        this.protocJarFile = protocJarFile;
        this.protocArguments = protocArguments;
        this.ioException = ioException;
    }

    public int attemptToRecover() {
        if (isCausedByPermissionDenied() && isPlatformLinux() && isTmpMountWithNoexecOption()) {
            return recover();
        }
        Protoc.log("Recovery it not supported from the current IOException.");
        return FAILED_RECOVERY;
    }

    private int recover() {
        try {
            File alternativeProtocTmpFile = createProtocFileInAlternativeTmpDir();
            Protoc.log("Try to run protoc from alternative temp directory: " +
                    alternativeProtocTmpFile.getAbsolutePath());
            return Protoc.runProtoc(alternativeProtocTmpFile.getAbsolutePath(), Arrays.asList(this.protocArguments));
        } catch (Exception e) {
            Protoc.log("Exception happened while recovery: " + e.toString());
            return FAILED_RECOVERY;
        }
    }

    private boolean isPlatformLinux() {
        return System.getProperty("os.name").toLowerCase().startsWith("linux");
    }

    private boolean isCausedByPermissionDenied() {
        return this.ioException.getCause().toString().toLowerCase().contains("permission denied");
    }

    private boolean isTmpMountWithNoexecOption() {
        ProcessBuilder processBuilder = new ProcessBuilder("mount");
        try {
            Process process = processBuilder.start();
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("/tmp") && line.contains("noexec")) {
                    return true;
                }
            }
        } catch (Exception e) {
            Protoc.log("Exception while checking if /tmp is mounted with noexec option: " + e.toString());
        }
        return false;
    }

    private File createProtocFileInAlternativeTmpDir() throws IOException {
        File protocTmpFile = File.createTempFile("protocjar", "", createTmpDir());
        Protoc.populateFile(this.protocJarFile.getAbsolutePath(), protocTmpFile);
        protocTmpFile.setExecutable(true);
        protocTmpFile.deleteOnExit();
        return protocTmpFile;
    }

    private File createTmpDir() {
        String userHomeDir = System.getProperty("user.home");
        String alternativeTmpDir = userHomeDir + File.separator + "tmp";
        Protoc.log("Create alternative temporary directory at " + alternativeTmpDir);
        File tmpDir = new File(alternativeTmpDir);
        tmpDir.mkdirs();
        tmpDir.deleteOnExit();
        return tmpDir;
    }
}
