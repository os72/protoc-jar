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
import java.util.Properties;

import org.junit.Test;
import com.github.os72.protocjar.PlatformDetector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtocTest
{
	@Test
	public void testRunProtocBasic() throws Exception {
		{
			String[] args = {"--version"};
			assertEquals(0, Protoc.runProtoc(args));
		}
		{
			String[] args = {"--version", "-v2.4.1"};
			assertEquals(1, Protoc.runProtoc(args));
		}
		{
			String[] args = {"--version", "-v2.5.0"};
			assertEquals(0, Protoc.runProtoc(args));
		}
		{
			String[] args = {"--version", "-v2.6.1"};
			assertEquals(0, Protoc.runProtoc(args));
		}
		{
			String[] args = {"--version", "-v3.4.0"};
			assertEquals(0, Protoc.runProtoc(args));
		}
	}

	@Test
	public void testRunProtocDownload() throws Exception {
		{
			PlatformDetector detector = new PlatformDetector();
			Properties props = new Properties();
			detector.detect(props, null);
			if (!props.getProperty("os.detected.name").equals("freebsd")) {
				String[] args = {"--version", "-v:com.google.protobuf:protoc:3.0.0"};
				assertEquals(0, Protoc.runProtoc(args));
			}
		}
	}

	@Test
	public void testStdTypes() throws Exception {
		{
			String outDir = "target/test-protoc-stdtypes";
			new File(outDir).mkdirs();
			String[] args = {"-v2.6.1", "--include_std_types", "-I.", "--java_out="+outDir, sStdTypeExampleFile2};
			assertEquals(0, Protoc.runProtoc(args));
		}
		{
			String outDir = "target/test-protoc-stdtypes";
			new File(outDir).mkdirs();
			String[] args = {"-v3.4.0", "--include_std_types", "-I.", "--java_out="+outDir, sStdTypeExampleFile3};
			assertEquals(0, Protoc.runProtoc(args));
		}
	}

	@Test
	public void testRunProtocCompile() throws Exception {
		{
			String outDir = "target/test-protoc";
			new File(outDir).mkdirs();
			String[] args = {"-v2.4.1", "--java_out="+outDir, sPersonSchemaFile};
			assertEquals(0, Protoc.runProtoc(args));
		}
	}

	@Test
	public void testRunProtocCompileShade() throws Exception {
		{
			String outDir = "target/test-protoc-shaded-241";
			new File(outDir).mkdirs();
			String[] args = {"-v2.4.1", "--java_shaded_out="+outDir, sPersonSchemaFile};
			assertEquals(0, Protoc.runProtoc(args));
			assertHasGeneratedFile(outDir);
		}
		{
			String outDir = "target/test-protoc-shaded-250";
			new File(outDir).mkdirs();
			String[] args = {"-v2.5.0", "--java_shaded_out="+outDir, sPersonSchemaFile};
			assertEquals(0, Protoc.runProtoc(args));
			assertHasGeneratedFile(outDir);
		}
		{
			String outDir = "target/test-protoc-shaded-261";
			new File(outDir).mkdirs();
			String[] args = {"-v2.6.1", "--java_shaded_out="+outDir, sPersonSchemaFile};
			assertEquals(0, Protoc.runProtoc(args));
			assertHasGeneratedFile(outDir);
		}
	}

	private static void assertHasGeneratedFile(String outDir) {
		assertTrue(new File(outDir + "/com/github/os72/protocjar/PersonSchema.java").exists());
	}

	static final String sPersonSchemaFile = "src/test/resources/PersonSchema.proto";
	static final String sStdTypeExampleFile2 = "src/test/resources/StdTypeExample2.proto";
	static final String sStdTypeExampleFile3 = "src/test/resources/StdTypeExample3.proto";
}
