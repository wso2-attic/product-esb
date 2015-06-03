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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.util.SimpleSocketServer;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.JMSEndpointManager;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

/**
 * https://wso2.org/jira/browse/ESBJAVA-3670
 * Message duplication after a transient outage of the endpoint and the message store
 */

public class ESBJAVA3670TestCase extends ESBIntegrationTest {
    SimpleSocketServer simpleSocketServer;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        OMElement synapse = esbUtils.loadResource("/artifacts/ESB/jms/transport/ESBJAVA-3670-messageDuplication.xml");
        updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));

    }

    @Test(groups = {"wso2.esb"}, description = "Test Message duplication after a transient outage of the endpoint")
    public void testMessageDuplication() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        client.sendRobust(Utils.getStockQuoteRequest("JMS"), getProxyServiceURLHttp("Proxy1"), "getQuote");
        Thread.sleep(2000);
        JMSBrokerStartupTestCase.stop();
        JMSBrokerStartupTestCase.start(); //Restart Message broker
        Thread.sleep(10000);
        startBackendServer(); //Start backend service
        Thread.sleep(20000);
        Assert.assertEquals(simpleSocketServer.getRequestCount(), 1, "Message count should equals to 1");

    }


    private void startBackendServer() {

        int port = 1989;
        String expectedResponse = "HTTP/1.0 200 OK\r\n" +
                                  "Server: CERN/3.0 libwww/2.17\r\n" +
                                  "Date: Tue, 16 Nov 1994 08:12:31 GMT\r\n" +
                                  "\r\n" + "<HTML>\n" + "<!DOCTYPE HTML PUBLIC " +
                                  "\"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
                                  "<HEAD>\n" + " <TITLE>Test Server Results</TITLE>\n" +
                                  "</HEAD>\n" + "\n" + "<BODY BGCOLOR=\"#FDF5E6\">\n" +
                                  "<H1 ALIGN=\"CENTER\"> Results</H1>\n" +
                                  "Here is the request line and request headers\n" +
                                  "sent by your browser:\n" + "<PRE>";
        simpleSocketServer = new SimpleSocketServer(port, expectedResponse);
        simpleSocketServer.start();

    }

    @AfterClass(alwaysRun = true)
    public void UndeployService() throws Exception {
        try {
            super.cleanup();
        } finally {
            if (simpleSocketServer != null) {
                simpleSocketServer.shutdown();
            }
        }
    }
}
