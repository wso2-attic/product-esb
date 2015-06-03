
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.carbon.esb.util.SimpleSocketServer;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestCaseUtils;

import java.io.File;

/**
 * https://wso2.org/jira/browse/ESBJAVA-3686 Duplicate transfer-encoding
 * response header doesn't work.
 */
public class ESBJAVA3686DuplicateTransferEncodingTestCase extends ESBIntegrationTest {

    private static final String SERVICE_NAME = "duplicateHeaders";
    private final SimpleHttpClient httpClient = new SimpleHttpClient();
    private SimpleSocketServer simpleSocketServer;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        // load the proxy config
        String relativePath = "artifacts" + File.separator + "ESB" +
                              File.separator + "synapseconfig" + File.separator + "duplicateHeaders" +
                              File.separator;
        ESBTestCaseUtils util = new ESBTestCaseUtils();
        relativePath = relativePath.replaceAll("[\\\\/]", File.separator);
        OMElement proxyConfig = util.loadResource(relativePath + "proxy.xml");
        addProxyService(proxyConfig);
        int port = 1989;
        String expectedResponse = "HTTP/1.0 200 OK\r\nServer: testServer\r\n" +
                                  "Content-Type: text/html\r\n" +
                                  "Transfer-Encoding: chunked\r\n" +
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
        Thread.sleep(10000);
    }

    @Test(groups = "wso2.esb", description = "test to verify duplicate Transfer-Encoding response headers.")
    public void testDuplicateTransferEncodingHeaders() throws Exception {
        String serviceURL = getProxyServiceURLHttp(SERVICE_NAME);

        try {
            httpClient.doGet(serviceURL, null);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("get request fail when response has duplicate Transfer-Encoding");
        }

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
