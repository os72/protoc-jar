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
