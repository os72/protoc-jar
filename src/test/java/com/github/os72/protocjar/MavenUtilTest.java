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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import com.github.os72.protocjar.MavenUtil.MavenSettings;

public class MavenUtilTest
{
	@Test
	public void testParseMavenSettings() throws Exception {
		log("testParseMavenSettings");
		MavenSettings settings = MavenUtil.parseMavenSettings(new File("src/test/resources/maven-settings.xml"));
		assertEquals("http://mirrors.ibiblio.org/pub/mirrors/maven2/", settings.mMirrorUrl);
		log(settings.mMirrorUrl);
		assertEquals("localhost", settings.mProxyHost);
		assertEquals(3128, settings.mProxyPort);
		log(settings.mProxyHost + ":" + settings.mProxyPort);
	}

	@Test
	public void testParseReleaseBuild() throws Exception {
		log("testParseReleaseBuild");
		File mdFile = new File("src/test/resources/maven-metadata-release.xml");
		String build = MavenUtil.parseLastReleaseBuild(mdFile, new ProtocVersion(null, null, "2.4.1"));
		assertEquals("2.4.1-build3", build);
		build = MavenUtil.parseLastReleaseBuild(mdFile, new ProtocVersion(null, null, "3.5.1"));
		assertEquals("3.5.1-build2", build);
		build = MavenUtil.parseLastReleaseBuild(mdFile, new ProtocVersion(null, null, "3.8.0"));
		assertNull(build);
	}

	@Test
	public void testParseSnapshotExeName() throws Exception {
		log("testParseSnapshotExeName");
		File mdFile = new File("src/test/resources/maven-metadata-snapshot.xml");
		String name = MavenUtil.parseSnapshotExeName(mdFile);
    	Properties props = new Properties();
		new PlatformDetector().detect(props, null);
		String osName = props.getProperty(PlatformDetector.DETECTED_NAME);
		
		assertEquals(String.format("protoc-2.4.1-20180823.052533-7-%s-x86_64.exe", osName), name);
	}

	static void log(Object msg) {
		System.out.println("protoc-jar-test: " + msg);
	}
}
