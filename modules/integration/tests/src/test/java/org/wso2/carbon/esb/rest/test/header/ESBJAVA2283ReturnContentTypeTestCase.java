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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.customservers.webserver.SimpleWebServer;
import org.wso2.carbon.automation.core.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.core.utils.httpserverutils.SimpleHttpClient;
import org.wso2.carbon.esb.ESBIntegrationTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class ESBJAVA2283ReturnContentTypeTestCase extends ESBIntegrationTest {
    private Log log = LogFactory.getLog(ESBJAVA2283ReturnContentTypeTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/esbjava2283/synapse.xml");
    }

    @Test(groups = {"wso2.esb"}, description = "test return content type")
    public void testReturnContentType() throws Exception {
        SimpleWebServer simpleWebServer = new SimpleWebServer(9006, 200);
        try {
            simpleWebServer.start();
            SimpleHttpClient httpClient = new SimpleHttpClient();
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "text/xml");
            HttpResponse response = httpClient.doGet(esbServer.getServiceUrl() + "/passThoughProxy", headers);
            log.info(response.getEntity().getContentType());
            log.info(response.getStatusLine().getStatusCode());

            assertEquals(response.getEntity().getContentType().getValue(), "text/xml", "Expected content type doesn't match");
            assertEquals(response.getStatusLine().getStatusCode(), 200, "response code doesn't match");
        } finally {
            simpleWebServer.terminate();
            ClientConnectionUtil.waitForPort(9006, InetAddress.getLocalHost().getHostName());
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
