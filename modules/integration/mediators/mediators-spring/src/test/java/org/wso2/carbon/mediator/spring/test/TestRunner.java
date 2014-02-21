package org.wso2.carbon.mediator.spring.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestRunner extends TestSuite{

    public static Test suite() throws Exception {
        TestSuite testSuite = new TestSuite();
        /* String testName = "";
        Properties sysProps = System.getProperties();

        for (Enumeration e = sysProps.propertyNames(); e.hasMoreElements();) {

            String key = (String) e.nextElement();

            if (key.equals("test.suite")) {
                testName = System.getProperty("test.suite");
            }
        }  */
        testSuite.addTestSuite(SpringMediatorTest.class);
        return testSuite;
    }
}
