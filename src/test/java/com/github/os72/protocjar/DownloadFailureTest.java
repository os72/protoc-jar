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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class DownloadFailureTest
{
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private HttpServer server;
    private String oldHome;
    private String[] oldPaths;
    private String prefix;
    private File home;
    private File cache;
    private volatile int mode;
    private final AtomicInteger requests = new AtomicInteger();
    private final ProtocVersion version = new ProtocVersion(null, null, "81.0.0-test");

    @Before
    public void setUp() throws Exception {
        home = temp.newFolder("home");
        oldHome = System.getProperty("user.home");
        oldPaths = Protoc.sDdownloadPaths;
        prefix = "failure-test-" + home.getParentFile().getName() + "/";
        cache = new File(Protoc.getWebcacheDir(), prefix);
        Protoc.sDdownloadPaths = new String[] {prefix + "first/", prefix + "second/"};
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                requests.incrementAndGet();
                String path = exchange.getRequestURI().getPath();
                boolean second = path.indexOf("/second/") >= 0;
                int status = mode == 1 ? 404 : mode == 2 && second ? 404
                        : mode == 3 && second ? 200 : 500;
                byte[] body = (path.endsWith("maven-metadata.xml")
                        ? "<metadata><versioning><versions/></versioning></metadata>"
                        : "test executable").getBytes("UTF-8");
                exchange.sendResponseHeaders(status, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            }
        });
        server.start();
        write(new File(home, ".m2/settings.xml"), "<settings><mirrors><mirror>"
                + "<url>http://127.0.0.1:" + server.getAddress().getPort() + "/</url>"
                + "<mirrorOf>central</mirrorOf></mirror></mirrors></settings>");
        System.setProperty("user.home", home.getAbsolutePath());
    }

    @After
    public void tearDown() {
        if (oldHome != null) System.setProperty("user.home", oldHome);
        if (oldPaths != null) Protoc.sDdownloadPaths = oldPaths;
        if (server != null) server.stop(0);
        if (cache != null) delete(cache);
    }

    @Test
    public void extractionPreservesRepositoryFailure() throws Exception {
        assertRepositoryFailure(version);
    }

    @Test
    public void laterMissingArtifactDoesNotHideRepositoryFailure() throws Exception {
        mode = 2;
        assertRepositoryFailure(version);
    }

    @Test
    public void successfulFallbackStillWins() throws Exception {
        mode = 3;
        File result = Protoc.extractProtoc(version, home);
        assertEquals("test executable".getBytes("UTF-8").length, result.length());
        assertTrue(requests.get() > 0);
    }

    @Test
    public void missingArtifactsKeepUnsupportedPlatformResult() throws Exception {
        mode = 1;
        try {
            Protoc.extractProtoc(version, home);
            fail("Expected missing platform artifact");
        } catch (FileNotFoundException expected) {
            assertTrue(expected.getMessage().startsWith("Unsupported platform:"));
        }
    }

    @Test
    public void cachedExecutableStillWinsDuringOutage() throws Exception {
        File directory = new File(Protoc.getWebcacheDir(), Protoc.sDdownloadPaths[0]);
        write(new File(directory, "maven-metadata.xml"),
                "<metadata><versioning><versions/></versioning></metadata>");
        write(new File(directory, version.mVersion + "/" + Protoc.getProtocExeName(version)),
                "test executable");
        File result = Protoc.extractProtoc(version, home);
        assertEquals("test executable".getBytes("UTF-8").length, result.length());
        assertEquals(0, requests.get());
    }

    @Test
    public void explicitArtifactFailureStillPropagates() throws Exception {
        assertRepositoryFailure(new ProtocVersion("failure.test", "tool", version.mVersion));
    }

    private void assertRepositoryFailure(ProtocVersion requested) throws Exception {
        try {
            Protoc.extractProtoc(requested, home);
            fail("Expected repository failure");
        } catch (IOException expected) {
            assertFalse("Repository failure must not become unsupported platform: " + expected,
                    expected instanceof FileNotFoundException);
            assertTrue(expected.getMessage(), expected.getMessage().indexOf("500") >= 0);
        }
    }

    private static void write(File file, String text) throws IOException {
        file.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(file);
        try { out.write(text.getBytes("UTF-8")); }
        finally { out.close(); }
    }

    private static void delete(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) delete(child);
        file.delete();
    }
}
