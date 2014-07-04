/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.jms.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.core.customservers.tomcat.TomcatServerManager;
import org.wso2.carbon.automation.core.customservers.tomcat.TomcatServerType;

import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.automation.utils.esb.JSONClient;

import org.wso2.esb.integration.common.clients.logging.LoggingAdminClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.JMSEndpointManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.services.jaxrs.customersample.CustomerConfig;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Check if the message processor retries with JSON messages as expected.
 * https://wso2.org/jira/browse/ESBJAVA-3018
 */
public class MSMPJsonRetryTestCase extends ESBIntegrationTest {

    private TomcatServerManager tomcatServerManager;
    private LoggingAdminClient logAdmin;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        // START THE ESB
        super.init();
        logAdmin = new LoggingAdminClient(contextUrls.getBackEndUrl(), getSessionCookie());
        OMElement synapse = esbUtils.loadResource("/artifacts/ESB/jms/transport/MSMP_JSON_RETRY.xml");
        updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));

    }

    @Test(groups = {"wso2.esb"}, description = "Test JSON retry of message processor")
    public void testMessageProcessorJSONRetry() throws Exception {
        logAdmin.updateLoggerData("org.apache.synapse", LoggingAdminClient.logLevel.DEBUG.name(), true, false);
        // SEND THE REQUEST
        String addUrl = getProxyServiceURLHttp("MSMPRetrytest");
        String payload = "{\"name\":\"Jack\"}";

        Map<String, String>   headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");

         HttpResponse response = HttpRequestUtil.doPost(new URL(addUrl), payload, headers);

        // IT HAS TO BE 202 ACCEPTED
        assertEquals(response.getResponseCode(), 202, "ESB failed to send 202 even after setting FORCE_SC_ACCEPTED");

        // WAIT FOR THE MESSAGE PROCESSOR TO TRIGGER
        Thread.sleep(5000);

        // START THE SERVER
        tomcatServerManager = new TomcatServerManager(
                CustomerConfig.class.getName(), TomcatServerType.jaxrs.name(), 8080);

        tomcatServerManager.startServer();  // staring tomcat server instance
        Thread.sleep(10000);

        // ASSERT RESULTS
        LogViewerClient logViewerClient =
                new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
        LogEvent[] logEvents = logViewerClient.getAllSystemLogs();

        boolean success = false;
        for (int i = 0; i < logEvents.length ; i++) {
             if (logEvents[i].getMessage().contains("Jack")) {
                success = true;
                break;
            }
        }

        assertTrue(success, "Message work for json");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        tomcatServerManager.stop();
        super.cleanup();
    }
}
