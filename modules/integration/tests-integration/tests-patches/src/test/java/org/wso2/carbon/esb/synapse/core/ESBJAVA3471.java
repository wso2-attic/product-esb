/*
 *Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.synapse.core;

import static org.testng.Assert.assertTrue;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.WireMonitorServer;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

/**
 * This test case is to validate the use of clone mediator inside fault sequence
 */
public class ESBJAVA3471 extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    public WireMonitorServer wireServer;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB"
                + File.separator + "synapseconfig" + File.separator + "config3471" + File.separator
                + "synapse.xml");
        Thread.sleep(5000);
        wireServer = new WireMonitorServer(8991);
        wireServer.start();

    }

    @Test(groups = "wso2.esb", description = "Check clone mediator inside fault sequence.")
    public void testCloneInsideFaultSequence() throws XMLStreamException, InterruptedException {
        String requestPayload = "<level1><a><b>222</b></a></level1>";
        OMElement payload1 = AXIOMUtil.stringToOM(requestPayload);
        boolean faultReturned = false;
        try {
            axis2Client.send(getProxyServiceURL("CloneMediatorBugTest"), null, "mediate", payload1);
        } catch (AxisFault af) {
            faultReturned = true;
        }
        assertTrue(faultReturned,
                "AxisFault was not throw properly and root cause is java.util.EmptyStackException.");
        boolean logFound = false;
        LogViewerClient logViewerClient;
        try {
            logViewerClient = new LogViewerClient(esbServer.getBackEndUrl(),
                    esbServer.getSessionCookie());
            LogEvent[] logs = logViewerClient.getAllSystemLogs();
            for (LogEvent item : logs) {
                if (item.getPriority().equals("INFO")) {
                    String message = item.getMessage();
                    if (message.contains("Clone Test 01")) {
                        logFound = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(logFound,
                "AxisFault was not throw properly and root cause is java.util.EmptyStackException.");
    }

    @AfterClass(alwaysRun = true)
    public void stop() throws Exception {
        super.cleanup();
    }
}
