package org.wso2.esb.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.fail;

public class ESBTestServerManager extends TestServerManager {
    protected static final Log log = LogFactory.getLog(ESBTestServerManager.class);

    private String carbonHome;

    @Override
    @BeforeSuite(timeOut = 300000)
    public String startServer() throws IOException {
        carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);
        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 120000)
    public void stopServer() throws Exception {
        super.stopServer();
    }

    public void copyArtifacts() throws IOException {
        String targetPath = "repository" + File.separator + "samples" + File.separator +
                "resources" + File.separator;
        copyResourceFile("/test.wsdl", targetPath + "test.wsdl");
        copyResourceFile("/hr.xsd", targetPath + "hr.xsd");
        log.info("Successfully copied the test.wsdl and hr.xsd to the resources directory");
    }

    @Override
    protected void copyArtifacts(String carbonHome) throws IOException {
    }

    private void copyResourceFile(String sourcePath, String targetPath) throws IOException {
        InputStream in = getClass().getResourceAsStream(sourcePath);
        if (in == null) {
            fail("Unable to locate the specified configuration resource: " + sourcePath);
        }

        File target = new File(carbonHome + File.separator + targetPath);
        FileOutputStream fos = new FileOutputStream(target);

        byte[] data = new byte[1024];
        int i;
        while ((i = in.read(data)) != -1) {
            fos.write(data, 0, i);
        }
        fos.flush();
        fos.close();
        in.close();
    }
}
