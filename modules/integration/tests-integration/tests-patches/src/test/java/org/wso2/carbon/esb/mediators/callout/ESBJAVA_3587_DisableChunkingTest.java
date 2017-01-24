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
package org.wso2.carbon.esb.mediators.callout;

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;
import java.rmi.RemoteException;

/**
 * This class test the fix for ESBJAVA_3587. It send a callout request with property "DISABLE_CHUNKING" set to true.
 * Monitors the wire logs and look for the content-length header since it should be there while the chunking is
 * disabled.
 */
public class ESBJAVA_3587_DisableChunkingTest extends ESBIntegrationTest {

    private ServerConfigurationManager serverManager = null;
    private LogViewerClient logViewerClient = null;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(context);
        String sourceLog4j =
                FrameworkPathUtil.getSystemResourceLocation() + File.separator + "artifacts" + File.separator + "ESB"
                        + File.separator + "mediatorconfig" + File.separator + "callout" + File.separator
                        + "log4j.properties";

        String targetLog4j = CarbonBaseUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "log4j.properties";

        File sourceLog4jFile = new File(sourceLog4j);
        File targetLog4jFile = new File(targetLog4j);
        serverManager.applyConfigurationWithoutRestart(sourceLog4jFile, targetLog4jFile, true);
        serverManager.restartGracefully();
        super.init();

        loadESBConfigurationFromClasspath(
                File.separator + "artifacts" + File.separator + "ESB" + File.separator + "mediatorconfig"
                        + File.separator + "callout" + File.separator + "disableChunkingProxy.xml");
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }

    @Test(groups = { "wso2.esb" },
          description = "Test disabling chunking for Callout")
    public void testChunkingDisabled() throws LogViewerLogViewerException {
        try {
            axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("CallOutProxy"), null, "WSO2");
        } catch (Exception exception) {
            //ignore
        }
        boolean lengthHeaderFound = false;
        LogEvent[] logEvents = new LogEvent[0];
        try {
            logEvents = logViewerClient.getAllRemoteSystemLogs();
        } catch (RemoteException e) {
            //ignore
        }
        for (LogEvent event : logEvents) {
            if (event.getMessage().contains(">> \"Content-Length:")) {
                lengthHeaderFound = true;
                break;
            }
        }

        Assert.assertTrue("No Content-length header found, Disable chunking not working", lengthHeaderFound);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
        if (serverManager != null) {
            serverManager.restoreToLastConfiguration();
            serverManager = null;
        }
    }
}
