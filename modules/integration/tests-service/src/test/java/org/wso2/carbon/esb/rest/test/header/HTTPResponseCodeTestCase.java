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

package org.wso2.carbon.esb.rest.test.header;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.customservers.webserver.SimpleWebServer;
import org.wso2.carbon.automation.core.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.core.utils.httpserverutils.SimpleHttpClient;
import org.wso2.carbon.esb.ESBIntegrationTest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class HTTPResponseCodeTestCase extends ESBIntegrationTest {
    private Log log = LogFactory.getLog(HTTPResponseCodeTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/esbjava2283/synapse.xml");
    }

    @Test(groups = {"wso2.esb"}, description = "Test different response codes",
          dataProvider = "getResponseCodes")
    public void testReturnResponseCode(int responseCode) throws Exception {
        int port = 9005;
        String contentType = "text/xml";
        SimpleWebServer simpleWebServer = new SimpleWebServer(port, responseCode);
        log.info("Running the test for context type -" + contentType);
        try {
            simpleWebServer.start();
            SimpleHttpClient httpClient = new SimpleHttpClient();
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", contentType);
            HttpResponse response = httpClient.doGet(esbServer.getServiceUrl() + "/passThoughProxy", headers);
            log.info(response.getEntity().getContentType());
            log.info(response.getStatusLine().getStatusCode());
            simpleWebServer.terminate();

            assertEquals(response.getEntity().getContentType().getValue(), contentType, "Expected content type doesn't match");
            assertEquals(response.getStatusLine().getStatusCode(), responseCode, "response code doesn't match");
        } finally {
            simpleWebServer.terminate();
            waitForPortCloser(port);
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

    @DataProvider(name = "responseCodeProvider")
    public Object[][] getResponseCodes() {
        return new Object[][]{
                {200},
                {400},
                {403},
                {404},
                {500},
                {501},
                {503},
        };
    }

    public boolean waitForPortCloser(int port) throws UnknownHostException {
        long time = System.currentTimeMillis() + 5000;
        boolean isPortAvailable = true;
        while (System.currentTimeMillis() < time) {
            isPortAvailable = ClientConnectionUtil.isPortOpen(port, InetAddress.getLocalHost().getHostName());
            if (!isPortAvailable) {
                return isPortAvailable;
            }
        }
        return isPortAvailable;
    }
}