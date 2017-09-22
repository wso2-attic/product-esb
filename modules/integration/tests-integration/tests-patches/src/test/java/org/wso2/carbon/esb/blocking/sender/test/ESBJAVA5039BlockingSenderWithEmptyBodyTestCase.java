/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.blocking.sender.test;

import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;

import java.io.File;

public class ESBJAVA5039BlockingSenderWithEmptyBodyTestCase extends ESBIntegrationTest {

    private final SimpleHttpClient httpClient = new SimpleHttpClient();
    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("artifacts" + File.separator + "ESB" +
                                          File.separator + "blockingsender" + File.separator + "ESBJAVA5039Config.xml");
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }

    @Test(groups = "wso2.esb", description = " Checking 202 response with empty body from backend")
    public void testBlockingSendEmptyBodyResponse() throws Exception {
        String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                         "<soapenv:Header/>\n" +
                         "<soapenv:Body>\n" +
                         "</soapenv:Body>\n" +
                         "</soapenv:Envelope>\n";
        boolean errorLog = false;

        HttpResponse response = httpClient.doPost("http://localhost:8480/services/blockingSenderProxy",
                                                  null, payload, "application/xml");

        LogEvent[] logs = logViewerClient.getAllSystemLogs();
        for (LogEvent logEvent : logs) {
            if (logEvent.getPriority().equals("ERROR")) {
                String message = logEvent.getMessage();
                if (message.contains("Could not build full log message")) {
                    errorLog = true;
                    break;
                }
            }
        }

        assertFalse(errorLog, "Mediator Hasn't invoked successfully.");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}