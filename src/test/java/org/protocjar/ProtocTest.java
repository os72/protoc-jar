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

package org.protocjar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ProtocTest extends TestCase
{
    public ProtocTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ProtocTest.class);
    }

    public void testRunProtoc() throws Exception {
    	String[] args = {"--help"};
        Protoc.runProtoc(args);
		
		args[0] = "--version";
        Protoc.sProtocFilePath = "bin_241"; // test only
        Protoc.runProtoc(args);
		
		args[0] = "--version";
        Protoc.sProtocFilePath = "bin_250"; // test only
        Protoc.runProtoc(args);
    }
}
