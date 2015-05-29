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
package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.carbon.esb.passthru.transport.test.util.SimpleSocketServer;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestCaseUtils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.io.File.separator;

/**
 * https://wso2.org/jira/browse/ESBJAVA-3677 Http Date header cannot preserve
 *
 */
public class ESBJAVA3677PreserveDateHeaderTestCase extends ESBIntegrationTest {

    private static final String SERVICE_NAME = "preserveHeaders";
    private SimpleHttpClient httpClient;
    private SimpleSocketServer simpleSocketServer;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        serverConfigurationManager = new ServerConfigurationManager(new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
        httpClient = new SimpleHttpClient();
        // load the proxy config
        String relativePath = "artifacts" + File.separator + "ESB" +
                              File.separator + "synapseconfig" + File.separator + "preserveHeaders" +
                              File.separator;
        ESBTestCaseUtils util = new ESBTestCaseUtils();
        relativePath = relativePath.replaceAll("[\\\\/]", File.separator);
        OMElement proxyConfig = util.loadResource(relativePath + "proxy.xml");
        addProxyService(proxyConfig);
        int port = 1990;
        String expectedResponse = "HTTP/1.0 200 OK\r\nServer: testServer\r\n" +
                                  "Content-Type: text/html\r\n" +
                                  "Date:Tue, 16 Nov 1994 08:12:31 GMTHost\r\n" +
                                  "Transfer-Encoding: chunked\r\n" +
                                  "\r\n" + "<HTML>\n" + "<!DOCTYPE HTML PUBLIC " +
                                  "\"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
                                  "<HEAD>\n" + " <TITLE>Test Server Results</TITLE>\n" +
                                  "</HEAD>\n" + "\n" + "<BODY BGCOLOR=\"#FDF5E6\">\n" +
                                  "<H1 ALIGN=\"CENTER\"> Results</H1>\n" +
                                  "Here is the request line and request headers\n" +
                                  "sent by your browser:\n" + "<PRE>";
        simpleSocketServer = new SimpleSocketServer(port, expectedResponse);
        simpleSocketServer.start();
        Thread.sleep(4000);
    }

    @Test(groups = "wso2.esb", description = "test to verify preserve date header.")
    public void testPreserveDateHeader() throws Exception {
        String serviceURL = getProxyServiceURLHttp(SERVICE_NAME);
        String expectedRequestDateHeader = "Date: Tue, 15 Nov 1994 08:12:31 GMTHost";
        String expectedResponseDateHeader = "Tue, 16 Nov 1994 08:12:31 GMTHost";

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Date", "Tue, 15 Nov 1994 08:12:31 GMT");

        HttpResponse beforeResponse = httpClient.doGet(serviceURL, headers);
        Thread.sleep(1000);
        Assert.assertFalse(checkExpectedHeaderContains(expectedRequestDateHeader), "Request Date header should not preserved");
        Assert.assertNotEquals(beforeResponse.getFirstHeader("Date").getValue(), expectedResponseDateHeader, "Response Date header should not preserved");

        simpleSocketServer.resetServerDetails();
        applyPassThroughProperties();

        HttpResponse afterResponse = httpClient.doGet(serviceURL, headers);
        Thread.sleep(1000);
        Assert.assertTrue(checkExpectedHeaderContains(expectedRequestDateHeader), "Request Date header not preserved");
        Assert.assertEquals(afterResponse.getFirstHeader("Date").getValue(), expectedResponseDateHeader, "Response Date header not preserved");

    }


    private void applyPassThroughProperties() throws Exception {

        URL url = getClass().getResource(separator + "artifacts" + separator + "ESB" + separator
                                         + "synapseconfig" + separator + "preserveHeaders" + separator + "passthru_transport" + separator
                                         + "passthru-http.properties");

        File srcFile = new File(url.getPath());
        serverConfigurationManager.applyConfiguration(srcFile);
    }

    private boolean checkExpectedHeaderContains(String expectedHeader) {
        String receivedRequest = simpleSocketServer.getReceivedRequest();

        if (receivedRequest == null || receivedRequest.isEmpty()) {
            return false;
        }
        return receivedRequest.contains(expectedHeader);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
            super.cleanup();
        } finally {
            if (simpleSocketServer != null) {
                simpleSocketServer.shutdown();
            }
        }
    }
}
