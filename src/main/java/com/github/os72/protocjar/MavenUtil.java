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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenUtil
{
	public static class MavenSettings
	{
		public String mCentralUrl = "https://repo.maven.apache.org/maven2/";
		public String mSnapshotUrl = "https://oss.sonatype.org/content/repositories/snapshots/";	
		public String mMirrorUrl;
		public String mProxyHost;
		public int mProxyPort = 8080;
	}

	// get release download URL
	static URLSpec getReleaseDownloadUrl(String path, MavenSettings settings) throws IOException {
		String url = settings.mCentralUrl;
		if (settings.mMirrorUrl != null) url = settings.mMirrorUrl;
		String fullpath = URI.create(url + "/" + path).normalize().toString();
		return new URLSpec(fullpath, settings.mProxyHost, settings.mProxyPort);
	}
	// get snapshot download URL
	static URLSpec getSnapshotDownloadUrl(String path, MavenSettings settings) throws IOException {
		String url = settings.mSnapshotUrl;
		String fullpath = URI.create(url + "/" + path).normalize().toString();
		return new URLSpec(fullpath, settings.mProxyHost, settings.mProxyPort);
	}

	// get maven settings
	static MavenSettings getMavenSettings() {
		try {
			String homeDir = System.getProperty("user.home");
			return parseMavenSettings(new File(homeDir, ".m2/settings.xml"));
		}
		catch (FileNotFoundException e) {
			log("using default maven settings, didn't find user settings.xml");
		}
		catch (Exception e) {
			log("using default maven settings, " + e);
		}
		return new MavenSettings();
	}

	// parse maven settings.xml
	static MavenSettings parseMavenSettings(File settingsFile) throws IOException {
		MavenSettings settings = new MavenSettings();
		try {
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmlDoc = xmlBuilder.parse(settingsFile);
			
			NodeList mirrorList = xmlDoc.getDocumentElement().getElementsByTagName("mirror");
			for (int i = 0; i < mirrorList.getLength(); i++) {
				Element mirror = (Element)mirrorList.item(i);
				String url = mirror.getElementsByTagName("url").item(0).getTextContent().trim();
				String mirrorOf = mirror.getElementsByTagName("mirrorOf").item(0).getTextContent().trim();
				if (mirrorOf.equals("central") || mirrorOf.contains("*")) settings.mMirrorUrl = url;
			}
			
			NodeList proxyList = xmlDoc.getDocumentElement().getElementsByTagName("proxy");
			for (int i = 0; i < proxyList.getLength(); i++) {
				Node proxy = proxyList.item(i);
				Node host = null;
				Node port = null;
				for (int j = 0; j < proxy.getChildNodes().getLength(); j++) {
					Node n = proxy.getChildNodes().item(j);
					if (n.getNodeName().equals("active") && !Boolean.parseBoolean(n.getTextContent().trim())) {
						break;
					}
					if (n.getNodeName().equals("host")) host = n;
					if (n.getNodeName().equals("port")) port = n;
				}
				if (host != null) {
					settings.mProxyHost = host.getTextContent().trim();
					if (port != null) settings.mProxyPort = Integer.parseInt(port.getTextContent().trim());
					break;
				}
			}
		}
		catch (IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		return settings;
	}

	// find last build (if any) from maven-metadata.xml
	static String parseLastReleaseBuild(File mdFile, ProtocVersion protocVersion) throws IOException {
		int lastBuild = 0;
		try {
			DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmlDoc = xmlBuilder.parse(mdFile);
			NodeList versions = xmlDoc.getElementsByTagName("version");
			for (int i = 0; i < versions.getLength(); i++) {
				Node ver = versions.item(i);
				String verStr = ver.getTextContent();
				if (verStr.startsWith(protocVersion.mVersion+"-build")) {
					String buildStr = verStr.substring(verStr.indexOf("-build")+"-build".length());
					int build = Integer.parseInt(buildStr);
					if (build > lastBuild) lastBuild = build;
				}
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		if (lastBuild > 0) return protocVersion.mVersion+"-build"+lastBuild;
		return null;
	}

	// parse snapshot exe name from maven-metadata.xml
	static String parseSnapshotExeName(File mdFile) throws IOException {
		String exeName = null;
		try {
			String clsStr = Protoc.getPlatformClassifier();
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
		return exeName;
	}

	static void log(Object msg) {
		Protoc.log(msg);
	}
}
