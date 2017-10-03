/*
*Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.esb.car.deployment.test;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test case to check Carbon apps (.CAR files) are correctly deployed before the transport starts.
 * JIRA CARBON16113
 */
public class CARBON16113TransportStartsBeforeArtifactDeploymentTestCase extends ESBIntegrationTest {

    private static final String CAR_FILE_NAME = "car-deployment-test.car";
    private static final String RESOURCE_CAR_ARTIFACTS_DIRECTORY =
            File.separator + "artifacts" + File.separator + "ESB" + File.separator + "car" + File.separator;
    private static final String CAPPS_DEPLOYMENT_DIRECTORY =
            File.separator + "repository" + File.separator + "deployment" + File.separator + "server" + File.separator +
                    "carbonapps" + File.separator;

    private static final String CAPP_MESSAGE = "Deploying Carbon Application : car-deployment-test.car";
    private static final String TRANSPORT_MESSAGE = "Pass-through HTTP Listener started on 0.0.0.0:8513";

    private TestServerManager testServerManager;
    private AutomationContext regTestContext;

    @BeforeClass(alwaysRun = true)
    protected void startServer() throws Exception {
        super.init();
        regTestContext = new AutomationContext("ESB", "esbCAppTest", TestUserMode.SUPER_TENANT_ADMIN);
        Map<String, String> startupParameterMap = new HashMap<String, String>();
        startupParameterMap.put("-DportOffset", "233");
        testServerManager = new TestServerManager(regTestContext, null, startupParameterMap) {

            public void configureServer() throws AutomationFrameworkException {
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    inputStream = getClass().getResourceAsStream(RESOURCE_CAR_ARTIFACTS_DIRECTORY + CAR_FILE_NAME);
                    outputStream = new FileOutputStream(
                            new File(this.getCarbonHome() + CAPPS_DEPLOYMENT_DIRECTORY + CAR_FILE_NAME));
                    IOUtils.copy(inputStream, outputStream);
                } catch (IOException e) {
                    throw new AutomationFrameworkException(
                            "Cannot copy CAR file '" + CAR_FILE_NAME + "' to the test ESB server.", e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        };
    }

    @Test(groups = "wso2.esb", enabled = true,
          description = "Test whether CApp deployment happens after transport has been started")
    public void test() throws Exception {
        testServerManager.startServer();
        LogViewerClient logViewerClient = new LogViewerClient(regTestContext.getContextUrls().getBackEndUrl(),
                                                              userInfo.getUserName(), userInfo.getPassword());
        List<LogEvent> logEvents = Arrays.asList(logViewerClient.getAllRemoteSystemLogs());
        // Order of these log events are in reversed-chronological order. So we need to manually revers this list.
        Collections.reverse(logEvents);
        boolean cappStarted = false;
        for (LogEvent event : logEvents) {
            if (!event.getPriority().equals("INFO")) {
                continue; // only interested in INFO logs
            }

            if (event.getMessage().contains(CAPP_MESSAGE)) {
                cappStarted = true;
            } else if (event.getMessage().contains(TRANSPORT_MESSAGE)) {
                Assert.assertTrue(cappStarted,
                                  "Transport started before deploying Carbon app '" + CAR_FILE_NAME + "'.");
                return;
            }
        }
        Assert.fail("Could not find '" + CAPP_MESSAGE + "' and '" + TRANSPORT_MESSAGE + "' messages in the log.");
    }

    @AfterTest(alwaysRun = true)
    public void cleanupEnvironment() throws Exception {
        try {
            super.cleanup();
        } finally {
            testServerManager.stopServer();
        }
    }
}
