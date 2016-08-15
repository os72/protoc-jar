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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		String protocVersion = ProtocVersion.PROTOC_VERSION;
		for (String arg : args) {
			String v = sVersionMap.get(arg);
			if (v != null) protocVersion = v;
		}
		
		File protocTemp = extractProtoc(protocVersion);
		int exitCode = runProtoc(protocTemp.getAbsolutePath(), Arrays.asList(args));
		protocTemp.delete();
		return exitCode;
	}

	public static int runProtoc(String cmd, String[] args) throws IOException, InterruptedException {
		return runProtoc(cmd, Arrays.asList(args));
	}

	public static int runProtoc(String cmd, List<String> argList) throws IOException, InterruptedException {
		String protocVersion = ProtocVersion.PROTOC_VERSION;
		String javaShadedOutDir = null;
		
		List<String> protocCmd = new ArrayList<String>();
		protocCmd.add(cmd);
		for (String arg : argList) {
			if (arg.startsWith("--java_shaded_out=")) {
				javaShadedOutDir = arg.split("--java_shaded_out=")[1];
				protocCmd.add("--java_out=" + javaShadedOutDir);
			}
			else {
				String v = sVersionMap.get(arg);
				if (v != null) protocVersion = v; else protocCmd.add(arg);				
			}
		}
		
		ProcessBuilder pb = new ProcessBuilder(protocCmd);
		log("executing: " + protocCmd);
		
		Process protoc = pb.start();
		new Thread(new StreamCopier(protoc.getInputStream(), System.out)).start();
		new Thread(new StreamCopier(protoc.getErrorStream(), System.err)).start();
		int exitCode = protoc.waitFor();
		
		if (javaShadedOutDir != null) {
			log("shading (version " + protocVersion + "): " + javaShadedOutDir);
			doShading(new File(javaShadedOutDir), protocVersion);
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
					file.delete();
					tmpFile.renameTo(file);
				}
				finally {
					if (br != null) { try {br.close();} catch (Exception e) {} }
					if (pw != null) { try {pw.close();} catch (Exception e) {} }
				}
			}
		}
	}

	public static File extractProtoc(String protocVersion) throws IOException {
		log("protoc version: " + protocVersion + ", detected platform: " + getPlatform());
		
		String protocFilePath = "bin_" + protocVersion;
		String resourcePath = null; // for jar
		String filePath = null; // for test
		
		String osName = System.getProperty("os.name").toLowerCase();
		String osArch = System.getProperty("os.arch").toLowerCase();
		if (osName.startsWith("win")) {
			filePath = protocFilePath + "/win32/protoc.exe";
		}
		else if (osName.startsWith("linux") && osArch.contains("64")) {
			filePath = protocFilePath + "/linux/protoc";
		}
		else if (osName.startsWith("mac") && osArch.contains("64")) {
			filePath = protocFilePath + "/mac/protoc";
		}
		else {
			throw new IOException("Unsupported platform: " + getPlatform());
		}
		resourcePath = "/" + filePath;
		
		FileOutputStream os = null;
		InputStream is = Protoc.class.getResourceAsStream(resourcePath);
		if (is == null) is = new FileInputStream(filePath);
		
		File temp = null;
		try {
			temp = File.createTempFile("protoc", ".exe");
			os = new FileOutputStream(temp);
			streamCopy(is, os);
		}
		finally {
			if (is != null) is.close();
			if (os != null) os.close();
		}
		temp.setExecutable(true);
		temp.deleteOnExit();
		return temp;
	}

	static void streamCopy(InputStream in, OutputStream out) throws IOException {
		int read = 0;
		byte[] buf = new byte[4096];
		while ((read = in.read(buf)) > 0) out.write(buf, 0, read);		
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

	static Map<String,String> sVersionMap = new HashMap<String,String>();
	static {
		sVersionMap.put("-v3.0.0", "300");
		sVersionMap.put("-v2.6.1", "261");
		sVersionMap.put("-v2.5.0", "250");
		sVersionMap.put("-v2.4.1", "241");
		sVersionMap.put("-v300", "300");
		sVersionMap.put("-v261", "261");
		sVersionMap.put("-v250", "250");
		sVersionMap.put("-v241", "241");
	}
}
