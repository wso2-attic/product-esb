/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.esb.registry.task;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

import javax.activation.DataHandler;
import java.net.URL;

/**
 * ESBJAVA-4565
 * NullPointerException when registry resource is accessed inside a scheduled task.
 */

public class ESBJAVA4565TestCase extends ESBIntegrationTest {

    private static final String REGISTRY_ARTIFACT = "/_system/governance/services/test/config/ftp.xml";
    private ResourceAdminServiceClient resourceAdminServiceStub;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init(0);
        resourceAdminServiceStub =
                new ResourceAdminServiceClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());

        resourceAdminServiceStub.addResource(REGISTRY_ARTIFACT, "application/xml", "FTP Test account details",
                new DataHandler(new URL("file:///" + getESBResourceLocation() +
                                        "/registry/ftp.xml")));
        Thread.sleep(1000);
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/esbjava4565/synapse.xml");
    }

    @Test(groups = "wso2.esb", description = "Analyze carbon logs to find NPE due to unresolved tenant domain.")
    public void checkErrorLog() throws Exception {
        Thread.sleep(30000);
        LogViewerClient cli = new LogViewerClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        LogEvent[] logs = cli.getAllSystemLogs();
        Assert.assertNotNull(logs, "No logs found");
        Assert.assertTrue(logs.length > 0, "No logs found");
        boolean hasErrorLog = false;
        for (LogEvent logEvent : logs) {
            String msg = logEvent.getMessage();
            if (msg.contains("java.lang.NullPointerException: Tenant domain has not been set in CarbonContext")) {
                hasErrorLog = true;
                break;
            }
        }
        Assert.assertFalse(hasErrorLog, "Tenant domain not resolved when registry resource is accessed inside " +
                                        "a scheduled task");
    }

    @AfterClass(alwaysRun = true, enabled=false)
    public void UndeployService() throws Exception {
        resourceAdminServiceStub.deleteResource(REGISTRY_ARTIFACT);
        super.cleanup();
    }
}
