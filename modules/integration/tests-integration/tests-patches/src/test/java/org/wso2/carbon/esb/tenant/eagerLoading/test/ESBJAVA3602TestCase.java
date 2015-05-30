/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.esb.tenant.eagerLoading.test;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

import java.io.File;

public class ESBJAVA3602TestCase extends ESBIntegrationTest {
    private ServerConfigurationManager serverManager = null;
    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    protected void startServerWithEagerLoading() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        serverManager = new ServerConfigurationManager(context);
        AutomationContext autoContext = new AutomationContext();

        // upload a faulty sequence which refer registry resource doesn't exists
        FileUtils.copyFileToDirectory(new File(FrameworkPathUtil.getSystemResourceLocation() + "/artifacts/ESB/sequence/ESBJAVA3602-FaultySeq.xml"), getSynapseDeploymentDir());

        File carbonXml = new File(FrameworkPathUtil.getSystemResourceLocation() + "/artifacts/ESB/config/ESBJAVA3602Carbon.xml");
        serverManager.applyConfiguration(carbonXml, getCarbonXmlFile());
        super.init(TestUserMode.TENANT_ADMIN);
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());

    }

    @Test(groups = "wso2.esb", enabled = true, description = "Test server start up with Eager loading")
    public void testStartupLogs() throws Exception {
        LogEvent[] logs = this.logViewerClient.getAllSystemLogs();
        Assert.assertNotNull(logs, "No logs found");
        Assert.assertTrue(logs.length > 0, "No logs found");
        boolean serverStarted = false;
        boolean serverEagerLoaded = false;
        if(logs.length > 0) {
            for (int i = 0; i < logs.length; i++) {
                if (logs[i].getMessage().contains("Using tenant eager loading policy")) {
                    serverEagerLoaded = true;
                } else if (logs[i].getMessage().contains("WSO2 Carbon started in ")) {
                    serverStarted = true;
                }
            }
            Assert.assertTrue(serverEagerLoaded, "Server starting with Tenant Eager Loading enabled.");
            Assert.assertTrue(serverStarted, "Server start-up completed with Tenant Eager Loading enabled.");
        }
        Assert.assertFalse(serverStarted, "Server didn't start properly...");
    }

    @AfterTest
    public void restoreSettings() throws Exception {
        serverManager.restoreToLastConfiguration();
    }

    public File getCarbonXmlFile() {
        String carbonHome = System.getProperty("carbon.home");
        File carbonXmlFile = new File(carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "carbon.xml");
        return carbonXmlFile;
    }

    public File getSynapseDeploymentDir() {
        String carbonHome = System.getProperty("carbon.home");
        File synapseDir = new File(carbonHome + File.separator + "repository" + File.separator + "deployment" + File.separator + "server/synapse-configs/default/sequences");
        return synapseDir;
    }

}
