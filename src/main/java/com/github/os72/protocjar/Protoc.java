/*
 * Copyright 2014 protoc-jar developers
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.os72.protocjar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Protoc
{
	public static void main(String[] args) {
		try {
			int exitCode = runProtoc(args);
			System.exit(exitCode);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int runProtoc(String[] args) throws IOException, InterruptedException {
		ProtocVersion protocVersion = ProtocVersion.PROTOC_VERSION;
		boolean includeStdTypes = false;
		for (String arg : args) {
			ProtocVersion v = getVersion(arg);
			if (v != null) protocVersion = v;
			if (arg.equals("--include_std_types")) includeStdTypes = true;
		}
		
		File protocTemp = extractProtoc(protocVersion, includeStdTypes);
		int exitCode = runProtoc(protocTemp.getAbsolutePath(), Arrays.asList(args));
		protocTemp.delete();
		return exitCode;
	}

	public static int runProtoc(String cmd, String[] args) throws IOException, InterruptedException {
		return runProtoc(cmd, Arrays.asList(args));
	}

	public static int runProtoc(String cmd, List<String> argList) throws IOException, InterruptedException {
		ProtocVersion protocVersion = ProtocVersion.PROTOC_VERSION;
		String javaShadedOutDir = null;
		
		List<String> protocCmd = new ArrayList<String>();
		protocCmd.add(cmd);
		for (String arg : argList) {
			if (arg.startsWith("--java_shaded_out=")) {
				javaShadedOutDir = arg.split("--java_shaded_out=")[1];
				protocCmd.add("--java_out=" + javaShadedOutDir);
			}
			else if (arg.equals("--include_std_types")) {
				File stdTypeDir = new File(new File(cmd).getParentFile().getParentFile(), "include");
				protocCmd.add("-I" + stdTypeDir.getAbsolutePath());
			}
			else {
				ProtocVersion v = getVersion(arg);
				if (v != null) protocVersion = v; else protocCmd.add(arg);				
			}
		}
		
		Process protoc = null;
		int numTries = 1;
		while (protoc == null) {
			try {
				log("executing: " + protocCmd);
				ProcessBuilder pb = new ProcessBuilder(protocCmd);
				protoc = pb.start();
			}
			catch (IOException ioe) {
				if (numTries++ >= 3) throw ioe;
				log("caught exception, retrying: " + ioe);
				Thread.sleep(1000);
			}
		}
		
		new Thread(new StreamCopier(protoc.getInputStream(), System.out)).start();
		new Thread(new StreamCopier(protoc.getErrorStream(), System.err)).start();
		int exitCode = protoc.waitFor();
		
		if (javaShadedOutDir != null) {
			log("shading (version " + protocVersion + "): " + javaShadedOutDir);
			doShading(new File(javaShadedOutDir), protocVersion.mVersion);
		}
		
		return exitCode;
	}

	public static void doShading(File dir, String version) throws IOException {
		if (dir.listFiles() == null) return;
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				doShading(file, version);
			}
			else if (file.getName().endsWith(".java")) {
				//log(file.getPath());
				File tmpFile = null;
				PrintWriter pw = null;
				BufferedReader br = null;
				FileInputStream is = null;
				FileOutputStream os = null;
				try {
					tmpFile = File.createTempFile(file.getName(), null);
					pw = new PrintWriter(tmpFile);
					br = new BufferedReader(new FileReader(file));
					String line;
					while ((line = br.readLine()) != null) {
						pw.println(line.replace("com.google.protobuf", "com.github.os72.protobuf" + version));
					}
					pw.close();
					br.close();
					// tmpFile.renameTo(file) only works on same filesystem, make copy instead:
					if (!file.delete()) log("Failed to delete: " + file.getName());
					is = new FileInputStream(tmpFile);
					os = new FileOutputStream(file);
					streamCopy(is, os);
				}
				finally {
					if (br != null) { try {br.close();} catch (Exception e) {} }
					if (pw != null) { try {pw.close();} catch (Exception e) {} }
					if (is != null) { try {is.close();} catch (Exception e) {} }
					if (os != null) { try {os.close();} catch (Exception e) {} }
					if (tmpFile != null) tmpFile.delete();
				}
			}
		}
	}

	public static File extractProtoc(ProtocVersion protocVersion, boolean includeStdTypes) throws IOException {
		File protocTemp = extractProtoc(protocVersion);
		if (includeStdTypes) extractStdTypes(protocVersion, protocTemp.getParentFile().getParentFile());
		return protocTemp;
	}

	public static File extractProtoc(ProtocVersion protocVersion) throws IOException {
		log("protoc version: " + protocVersion + ", detected platform: " + getPlatform());
		
		String srcFilePath = null;
		if (protocVersion.mArtifact == null) { // extract embedded protoc
			String binVersionDir = "bin_" + protocVersion;
			String osName = System.getProperty("os.name").toLowerCase();
			String osArch = System.getProperty("os.arch").toLowerCase();
			if (osName.startsWith("win")) {
				srcFilePath = binVersionDir + "/win32/protoc.exe";
			}
			else if (osName.startsWith("linux") && osArch.contains("64")) {
				srcFilePath = binVersionDir + "/linux/protoc";
			}
			else if (osName.startsWith("mac") && osArch.contains("64")) {
				srcFilePath = binVersionDir + "/mac/protoc";
			}
			else {
				throw new IOException("Unsupported platform: " + getPlatform());
			}
		}
		else { // download protoc from maven central
			srcFilePath = downloadProtoc(protocVersion).getAbsolutePath();
		}
		
		File tmpDir = File.createTempFile("protocjar", "");
		tmpDir.delete(); tmpDir.mkdirs();
		tmpDir.deleteOnExit();
		File binDir = new File(tmpDir, "bin");
		binDir.mkdirs();
		binDir.deleteOnExit();
		
		File protocTemp = new File(binDir, "protoc.exe");
		populateFile(srcFilePath, protocTemp);
		protocTemp.setExecutable(true);
		protocTemp.deleteOnExit();
		return protocTemp;
	}

	public static File extractStdTypes(ProtocVersion protocVersion, File tmpDir) throws IOException {
		if (tmpDir == null) {
			tmpDir = File.createTempFile("protocjar", "");
			tmpDir.delete(); tmpDir.mkdirs();
			tmpDir.deleteOnExit();
		}
		
		File tmpDirProtos = new File(tmpDir, "include/google/protobuf");
		tmpDirProtos.mkdirs();
		tmpDirProtos.getParentFile().getParentFile().deleteOnExit();
		tmpDirProtos.getParentFile().deleteOnExit();
		tmpDirProtos.deleteOnExit();
		
		final String majorProtoVersion = String.valueOf(protocVersion.mVersion.charAt(0));
		final String srcPathPrefix = String.format("proto%s/", majorProtoVersion);
		final String[] stdTypes = sStdTypesMap.get(majorProtoVersion);
		for (String srcFilePath : stdTypes) {
			File tmpFile = new File(tmpDir, srcFilePath);
			populateFile(srcPathPrefix + srcFilePath, tmpFile);
			tmpFile.deleteOnExit();
		}
		
		return tmpDir;
	}

	public static File downloadProtoc(ProtocVersion protocVersion) throws IOException {				
		Properties detectorProps = new Properties();
		new PlatformDetector().detect(detectorProps, null);
		String platform = detectorProps.getProperty("os.detected.classifier");
		
		String exeName = protocVersion.mGroup.replace(".", "/") + "/" +
				protocVersion.mArtifact + "/" + protocVersion.mVersion + "/" +
				protocVersion.mArtifact + "-" + protocVersion.mVersion + "-" + platform + ".exe";
		
		File tmpFile = File.createTempFile("protocjar", ".exe");
		File exeFile = new File(tmpFile.getParentFile(), "protocjar.webcache/" + exeName);
		exeFile.getParentFile().mkdirs();
		if (!exeFile.exists()) {
			URL exeUrl = new URL("http://central.maven.org/maven2/" + exeName);
			log("downloading: " + exeUrl);
			
			InputStream is = null;
			FileOutputStream os = null;
			try {
				is = exeUrl.openStream();
				os = new FileOutputStream(tmpFile);
				streamCopy(is, os);
				is.close();
				os.close();
				tmpFile.renameTo(exeFile);
			}
			catch (IOException e) {
				tmpFile.delete();
				throw e;
			}
			finally {
				if (is != null) is.close();
				if (os != null) os.close();			
			}			
		}
		
		log("cached: " + exeFile);
		return exeFile;
	}

	public static File populateFile(String srcFilePath, File destFile) throws IOException {
		String resourcePath = "/" + srcFilePath; // resourcePath for jar, srcFilePath for test
		
		FileOutputStream os = null;
		InputStream is = Protoc.class.getResourceAsStream(resourcePath);
		if (is == null) is = new FileInputStream(srcFilePath);
		
		try {
			os = new FileOutputStream(destFile);
			streamCopy(is, os);
		}
		finally {
			if (is != null) is.close();
			if (os != null) os.close();
		}
		
		return destFile;
	}

	public static void streamCopy(InputStream in, OutputStream out) throws IOException {
		int read = 0;
		byte[] buf = new byte[4096];
		while ((read = in.read(buf)) > 0) out.write(buf, 0, read);		
	}

	static ProtocVersion getVersion(String spec) {
		return ProtocVersion.getVersion(spec);
	}

	static String getPlatform() {
		return System.getProperty("os.name").toLowerCase() + "/" + System.getProperty("os.arch").toLowerCase();
	}

	static void log(String msg) {
		System.out.println("protoc-jar: " + msg);
	}

	static class StreamCopier implements Runnable
	{
		public StreamCopier(InputStream in, OutputStream out) {
			mIn = in;
			mOut = out;
		}

		public void run() {
			try {
				streamCopy(mIn, mOut);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		private InputStream mIn;
		private OutputStream mOut;
	}

	static String[] sStdTypesProto2 = {
		"include/google/protobuf/descriptor.proto",
	};
	static String[] sStdTypesProto3 = {
		"include/google/protobuf/any.proto",
		"include/google/protobuf/api.proto",
		"include/google/protobuf/descriptor.proto",
		"include/google/protobuf/duration.proto",
		"include/google/protobuf/empty.proto",
		"include/google/protobuf/field_mask.proto",
		"include/google/protobuf/source_context.proto",
		"include/google/protobuf/struct.proto",
		"include/google/protobuf/timestamp.proto",
		"include/google/protobuf/type.proto",
		"include/google/protobuf/wrappers.proto",
	};

	static Map<String,String[]> sStdTypesMap = new HashMap<String,String[]>();
	static {
		sStdTypesMap.put("2", sStdTypesProto2);
		sStdTypesMap.put("3", sStdTypesProto3);
	}
}
