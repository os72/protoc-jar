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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Protoc
{
	public static void main(String[] args) {
		try {
			if (args.length > 0 && args[0].equals("-pp")) { // print platform
				PlatformDetector.main(args);
				return;
			}
			int exitCode = runProtoc(args);
			System.exit(exitCode);
		}
		catch (Exception e) {
			log(e);
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
		
		try {
			File protocTemp = extractProtoc(protocVersion, includeStdTypes, null);
			return runProtoc(protocTemp.getAbsolutePath(), Arrays.asList(args));
		}
		catch (FileNotFoundException e) {
			throw e;
		}
		catch (Exception e) {
			// some linuxes don't allow exec in /tmp, try user home
			String homeDir = System.getProperty("user.home");
			File protocTemp = extractProtoc(protocVersion, includeStdTypes, new File(homeDir));
			return runProtoc(protocTemp.getAbsolutePath(), Arrays.asList(args));
		}
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
				if (numTries++ >= 3) throw ioe; // retry loop, workaround text file busy issue
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
		return extractProtoc(protocVersion, includeStdTypes, null);
	}

	public static File extractProtoc(ProtocVersion protocVersion, boolean includeStdTypes, File dir) throws IOException {
		File protocTemp = extractProtoc(protocVersion, dir);
		if (includeStdTypes) extractStdTypes(protocVersion, protocTemp.getParentFile().getParentFile());
		return protocTemp;
	}

	public static File extractProtoc(ProtocVersion protocVersion, File dir) throws IOException {
		log("protoc version: " + protocVersion + ", detected platform: " + getPlatformVerbose());
		
		File tmpDir = File.createTempFile("protocjar", "", dir);
		tmpDir.delete(); tmpDir.mkdirs();
		tmpDir.deleteOnExit();
		File binDir = new File(tmpDir, "bin");
		binDir.mkdirs();
		binDir.deleteOnExit();
		
		File exeFile = null;
		if (protocVersion.mArtifact == null) { // look for embedded protoc and on web (maven central)
			// look for embedded version
			String srcFilePath = "bin/" + protocVersion.mVersion + "/" + getProtocExeName(protocVersion);
			try {
				File protocTemp = new File(binDir, "protoc.exe");
				populateFile(srcFilePath, protocTemp);
				log("embedded: " + srcFilePath);
				protocTemp.setExecutable(true);
				protocTemp.deleteOnExit();
				return protocTemp;
			}
			catch (FileNotFoundException e) {
				//log(e);
			}
			
			// look in cache and maven central
			exeFile = findDownloadProtoc(protocVersion);
		}
		else { // download by artifact id from maven central
			String downloadPath = protocVersion.mGroup.replace(".", "/") + "/" + protocVersion.mArtifact + "/";
			exeFile = downloadProtoc(protocVersion, downloadPath);
		}
		
		if (exeFile == null) throw new FileNotFoundException("Unsupported platform: " + getProtocExeName(protocVersion));
		
		File protocTemp = new File(binDir, "protoc.exe");
		populateFile(exeFile.getAbsolutePath(), protocTemp);
		protocTemp.setExecutable(true);
		protocTemp.deleteOnExit();
		return protocTemp;
	}

	public static File findDownloadProtoc(ProtocVersion protocVersion) throws IOException {
		// look in webcache
		File webcacheDir = getWebcacheDir();
		for (String downloadPath : sDdownloadPaths) {
			String srcSubPath = protocVersion.mVersion + "/" + getProtocExeName(protocVersion);
			File exeFile = new File(webcacheDir, downloadPath + srcSubPath);
			if (exeFile.exists()) {
				log("cached: " + exeFile);
				return exeFile;
			}
		}
		
		// look on maven central
		for (String downloadPath : sDdownloadPaths) {
			try {
				return downloadProtoc(protocVersion, downloadPath);
			}
			catch (IOException e) {
				//log(e);
			}
		}
		
		return null;
	}

	public static File downloadProtoc(ProtocVersion protocVersion, String downloadPath) throws IOException {
		if (protocVersion.mVersion.endsWith("-SNAPSHOT")) {
			return downloadProtocSnapshot(protocVersion, downloadPath);
		}
		String srcSubPath = protocVersion.mVersion + "/" + getProtocExeName(protocVersion);
		URL exeUrl = new URL("http://central.maven.org/maven2/" + downloadPath + srcSubPath);
		File exeFile = new File(getWebcacheDir(), downloadPath + srcSubPath);
		return downloadFile(exeUrl, exeFile);
	}

	public static File downloadProtocSnapshot(ProtocVersion protocVersion, String downloadPath) throws IOException {
		String snapshotUrlStr = "https://oss.sonatype.org/content/repositories/snapshots/";
		
		// download maven-metadata.xml (cache for 1hr)
		String mdSubPath = protocVersion.mVersion + "/maven-metadata.xml";
		URL mdUrl = new URL(snapshotUrlStr + downloadPath + mdSubPath);
		File mdFile = new File(getWebcacheDir(), downloadPath + mdSubPath);
		if (mdFile.exists() && (System.currentTimeMillis() - mdFile.lastModified() > 3600*1000)) mdFile.delete();
		mdFile = downloadFile(mdUrl, mdFile);
		
		// parse exe name from maven-metadata.xml
		String exeName = null;
		try {
			String clsStr = getPlatformClassifier();
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmlDoc = xmlBuilder.parse(mdFile);
			NodeList versions = xmlDoc.getElementsByTagName("snapshotVersion");
			for (int i = 0; i < versions.getLength(); i++) {
				Node ver = versions.item(i);
				Node cls = null;
				Node val = null;
				for (int j = 0; j < ver.getChildNodes().getLength(); j++) {
					Node n = ver.getChildNodes().item(j);
					if (n.getNodeName().equals("classifier")) cls = n;
					if (n.getNodeName().equals("value")) val = n;
				}
				if (cls != null && val != null && cls.getTextContent().equals(clsStr))	{
					exeName = "protoc-" + val.getTextContent() + "-" + clsStr + ".exe";
					break;
				}
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		if (exeName == null) return null;
		
		// download exe
		String exeSubPath = protocVersion.mVersion + "/" + exeName;
		URL exeUrl = new URL(snapshotUrlStr + downloadPath + exeSubPath);
		File exeFile = new File(getWebcacheDir(), downloadPath + exeSubPath);
		return downloadFile(exeUrl, exeFile);
	}

	public static File downloadFile(URL srcUrl, File destFile) throws IOException {
		if (!destFile.exists()) {
			File tmpFile = File.createTempFile("protocjar", ".tmp");
			InputStream is = null;
			FileOutputStream os = null;
			try {
				URLConnection con = srcUrl.openConnection();
				con.setRequestProperty("User-Agent", "Mozilla"); // sonatype only returns proper maven-metadata.xml if this is set
				is = con.getInputStream();
				os = new FileOutputStream(tmpFile);
				log("downloading: " + srcUrl);
				streamCopy(is, os);
				is.close();
				os.close();
				destFile.getParentFile().mkdirs();
				tmpFile.renameTo(destFile);
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
		log("cached: " + destFile);
		return destFile;
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

	static File getWebcacheDir() throws IOException {
		File tmpFile = File.createTempFile("protocjar", ".tmp");
		File cacheDir = new File(tmpFile.getParentFile(), "protocjar.webcache");
		cacheDir.mkdirs();
		tmpFile.delete();
		return cacheDir;
	}

	static String getProtocExeName(ProtocVersion protocVersion) {
		return "protoc-" + protocVersion.mVersion + "-" + getPlatformClassifier() + ".exe";
	}

	static String getPlatformVerbose() {
		return getPlatformClassifier() + " (" + System.getProperty("os.name").toLowerCase() + "/" + System.getProperty("os.arch").toLowerCase() + ")";
	}
	static String getPlatformClassifier() {
		Properties detectorProps = new Properties();
		new PlatformDetector().detect(detectorProps, null);
		return detectorProps.getProperty("os.detected.classifier");
	}

	static ProtocVersion getVersion(String spec) {
		return ProtocVersion.getVersion(spec);
	}

	static void log(Object msg) {
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

	static String[] sDdownloadPaths = {
		"com/google/protobuf/protoc/",
		"com/github/os72/protoc/",
	};

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
