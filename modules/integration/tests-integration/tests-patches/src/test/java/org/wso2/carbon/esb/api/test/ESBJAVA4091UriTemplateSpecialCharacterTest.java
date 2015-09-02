package org.wso2.carbon.esb.api.test;

/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;

public class ESBJAVA4091UriTemplateSpecialCharacterTest extends ESBIntegrationTest {

    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("artifacts" + File.separator + "ESB"
                                          + File.separator + "synapseconfig" + File.separator + "rest"
                                          + File.separator + "ESBJAVA-4091" + File.separator +"uri-template-encoding" +
                                          ".xml");
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a query param consist of" +
                                                 " special character : ")
    public void testURITemplateExpandWithPercentEncoding() throws Exception {
        boolean isPercentEncoded = false;
        logViewerClient.clearLogs();
        HttpResponse response = HttpRequestUtil.sendGetRequest(
                getProxyServiceURLHttp("services/TestProxy"),
                null);
        LogEvent[] logs = logViewerClient.getAllSystemLogs();
        for (LogEvent logEvent : logs) {
            String message = logEvent.getMessage();
            if (message.contains("%2bGoofle%20%20fadfasfsf%20%40%20acsff%20**%20ad%20ascs%20999999900000000999999%20%20%20%20!%40%23%24%5e*()")) {
                isPercentEncoded = true;
                break;
            }
        }
        Assert.assertTrue(isPercentEncoded,
                          "Query parameters are not valid");

    }
}
