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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		String protocVersion = "-v261";
		List<String> argList = new ArrayList<String>();
		for (String arg : args) {
			if (arg.equals("-v241") || arg.equals("-v250") || arg.equals("-v261")) protocVersion = arg;
			else argList.add(arg);
		}
		args = new String[argList.size()];
		argList.toArray(args);
		
		protocVersion = protocVersion.substring(2);
		log("protoc version: " + protocVersion + ", detected platform: " + getPlatform());
		File protocTemp = extractProtoc(protocVersion);
		int exitCode = runProtoc(protocTemp.getAbsolutePath(), args);
		protocTemp.delete();
		return exitCode;
	}

	public static int runProtoc(String cmd, String[] args) throws IOException, InterruptedException {
		List<String> protocCmd = new ArrayList<String>();
		protocCmd.add(cmd);
		protocCmd.addAll(Arrays.asList(args));
		ProcessBuilder pb = new ProcessBuilder(protocCmd);
		log("executing: " + protocCmd);
		
		Process protoc = pb.start();
		new Thread(new StreamCopier(protoc.getInputStream(), System.out)).start();
		new Thread(new StreamCopier(protoc.getErrorStream(), System.err)).start();
		int exitCode = protoc.waitFor();
		
		return exitCode;
	}

	static File extractProtoc(String protocVersion) throws IOException {
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
		
		try {
			File temp = File.createTempFile("protoc", ".exe");
			temp.setExecutable(true);
			temp.deleteOnExit();
			os = new FileOutputStream(temp);
			streamCopy(is, os);
			return temp;
		}
		finally {
			if (is != null) is.close();
			if (os != null) os.close();
		}
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

	static final String sProtocFilePath = "bin_" + ProtocVersion.PROTOC_VERSION;
}
