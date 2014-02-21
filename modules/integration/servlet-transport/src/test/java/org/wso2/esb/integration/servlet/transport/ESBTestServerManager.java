/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.esb.integration.servlet.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ESBTestServerManager extends TestServerManager {

    private static final Log log = LogFactory.getLog(ESBTestServerManager.class);

    @Override
    @BeforeSuite(timeOut = 300000)
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);

        log.debug("Test Start Server Success");
        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 120000)
    public void stopServer() throws Exception {
        log.debug("Test Stop Server Success");
        super.stopServer();
    }

    @Override
    protected void copyArtifacts(String s) throws IOException {
        log.info("Updating axis2.xml to enable the servlet transports");

        String targetPath = "repository" + File.separator + "conf" + File.separator + "axis2" +
                File.separator + "axis2.xml";
        copyResourceFile("/pox_servlet_transport_axis2.xml", targetPath, s);


        log.info("Successfully updated the axis2.xml file");
    }

    private void copyResourceFile(String sourcePath, String targetPath, String carbonHome) throws IOException {
        InputStream in = getClass().getResourceAsStream(sourcePath);
        if (in == null) {
            log.error("Unable to locate the specified configuration resource: " + sourcePath);
            return;
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
