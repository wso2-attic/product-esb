/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.esb.nhttp.transport.mtom.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * This test case check whether MIMEBOUNDARY is added in content type header for MTOM messages
 * Jira : https://wso2.org/jira/browse/ESBJAVA-5013
 */
public class ESBJAVA5013BoundryInContentTypeTestCase extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private LogViewerClient logViewer;
    private final String MTOM_SERVICE = "MTOMSwASampleService";

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        context = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
        serverConfigurationManager = new ServerConfigurationManager(context);
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        File log4jProperties = new File(carbonHome + File.separator + "repository" + File.separator + "conf" +
                File.separator + "log4j.properties");
        applyProperty(log4jProperties, "log4j.logger.org.apache.synapse.transport.http.wire", "DEBUG");
        serverConfigurationManager.restartGracefully();
        super.init();
        loadESBConfigurationFromClasspath
                ("/artifacts/ESB/mediatorconfig/mtom/MtomSample.xml");
        logViewer = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }

    @Test(groups = "wso2.esb", description = "Test boundary header included in the contentType in MTOM")
    public void testBoundaryPropertyTest() throws Exception {
        SampleAxis2Server axis2Server = new SampleAxis2Server("test_axis2_server_9001.xml");
        axis2Server.start();
        axis2Server.deployService(MTOM_SERVICE);
        logViewer.clearLogs();
        String targetEPR = getProxyServiceURLHttp("MtomSample");
        try {
            axis2Client.sendSimpleQuoteRequest(null, targetEPR, "WSO2");
        } catch (Exception e) {
            //ignore, getting the error since stockquote client is used in place of MTOM
        }

        LogEvent[] logs = logViewer.getAllRemoteSystemLogs();
        boolean boundaryFound = false;
        boolean multipartFound = false;
        for (LogEvent logEvent : logs) {
            if (logEvent.getMessage().contains("multipart/related")) {
                multipartFound = true;
                if (logEvent.getMessage().contains("MIMEBoundary")) {
                    boundaryFound = true;
                }
                break;
            }
        }
        if (multipartFound) {
            Assert.assertTrue(boundaryFound, "MIMEBoundary not included in multipart header");
        } else {
            Assert.fail();
        }

    }

    /**
     * Apply the given property
     *
     * @param srcFile
     * @param key
     * @param value
     * @throws Exception
     */
    private void applyProperty(File srcFile, String key, String value) throws Exception {
        File destinationFile = new File(srcFile.getName());
        Properties properties = new Properties();
        properties.load(new FileInputStream(srcFile));
        properties.setProperty(key, value);
        properties.store(new FileOutputStream(destinationFile), null);
        serverConfigurationManager.applyConfigurationWithoutRestart(destinationFile);
    }

    public void cleanUp() throws Exception {
        super.cleanup();
    }
}