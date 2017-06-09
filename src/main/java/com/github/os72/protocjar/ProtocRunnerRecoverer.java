package com.github.os72.protocjar;

import java.io.*;
import java.util.Arrays;

public class ProtocRunnerRecoverer {
    private static final int FAILED_RECOVERY = -1;
    private final File protocJarFile;
    private final String[] protocArguments;

    public ProtocRunnerRecoverer(File protocJarFile, String[] protocArguments) {
        this.protocJarFile = protocJarFile;
        this.protocArguments = protocArguments;
    }

    public int attemptToRecover() {
        try {
            File alternativeProtocTmpFile = createProtocFileInAlternativeTmpDir();
            Protoc.log("Try to run protoc from alternative temp directory: " +
                    alternativeProtocTmpFile.getAbsolutePath());
            return Protoc.runProtoc(alternativeProtocTmpFile.getAbsolutePath(), Arrays.asList(this.protocArguments));
        } catch (Exception e) {
            Protoc.log("Exception happened while recovery: " + e.toString());
            return FAILED_RECOVERY;
        }    }

    private File createProtocFileInAlternativeTmpDir() throws IOException {
        File protocTmpFile = File.createTempFile("protocjar", ".exe", createTmpDir());
        Protoc.populateFile(this.protocJarFile.getAbsolutePath(), protocTmpFile);
        Protoc.log(String.valueOf(protocTmpFile.setExecutable(true)));

        File f = File.createTempFile("prefix", "suffix"); f.deleteOnExit();
        System.out.println(f.setExecutable(true));
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
