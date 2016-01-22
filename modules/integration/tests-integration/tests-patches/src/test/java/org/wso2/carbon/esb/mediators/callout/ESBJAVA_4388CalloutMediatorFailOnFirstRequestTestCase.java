package org.wso2.carbon.esb.mediators.callout;
/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
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

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.HashMap;
import java.util.Map;

public class ESBJAVA_4388CalloutMediatorFailOnFirstRequestTestCase extends ESBIntegrationTest {
    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        super.init();

        loadESBConfigurationFromClasspath(
                "/artifacts/ESB/mediatorconfig/callout/CallMediatorBlockProxyForSalesforce.xml");
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }

    @Test(groups = "wso2.esb", description = "Test to check whether callout mediator throws ERROR_MESSAGE = Error while performing the call operation")
    public void testIfFirstRequestFailsForSalesforce() throws Exception {
        String proxyServiceUrl = getProxyServiceURLHttp("CallMediatorBlockProxyForSalesforce");

        String requestPayload = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                                "   <soapenv:Body/>\n" +
                                "</soapenv:Envelope>";

        Map<String, String> headers = new HashMap<String, String>(1);
        headers.put("Content-Type", "application/xml");

        SimpleHttpClient httpClient = new SimpleHttpClient();
        httpClient.doPost(proxyServiceUrl, headers, requestPayload, "application/xml");

        LogEvent[] logs = logViewerClient.getAllRemoteSystemLogs();
        Assert.assertTrue(logs.length > 0, "Failed to retrieve logs");
        for (LogEvent logEvent : logs) {
            if (logEvent.getPriority().equals("INFO")) {
                String message = logEvent.getMessage();
                Assert.assertFalse(message.contains("ERROR_MESSAGE = Error while building message"),
                                   "First Request failed");
            }
        }
    }

    @AfterTest(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
