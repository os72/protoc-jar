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

import java.io.InputStream;
import java.net.URLConnection;

import org.junit.Test;

public class URLSpecTest
{
	//@Test
	public void testMavenProxy() throws Exception {
		log("testMavenProxy");
		URLSpec srcUrl = new URLSpec("https://repo.maven.apache.org/maven2/", "localhost", 3128);
		URLConnection con = srcUrl.openConnection();
		InputStream is = con.getInputStream();
		Protoc.streamCopy(is, System.out);
		log(srcUrl);
	}

	static void log(Object msg) {
		System.out.println("protoc-jar-test: " + msg);
	}
}
