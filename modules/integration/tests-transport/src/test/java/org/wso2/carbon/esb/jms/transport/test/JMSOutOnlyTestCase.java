/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.esb.jms.transport.test;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.JMSEndpointManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

public class JMSOutOnlyTestCase extends ESBIntegrationTest {
    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        OMElement synapse = esbUtils.loadClasspathResource("/artifacts/ESB/jms/transport/jms_out_only_proxy.xml");
        updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));
    }

    @Test(groups = {"wso2.esb"}, description = "Test proxy service with out-only jms transport")
    public void testJMSProxy() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        String payload = "<?xml version='1.0' encoding='UTF-8'?>" +
                         "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                         " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">" +
                         "   <soapenv:Header/>" +
                         "   <soapenv:Body>" +
                         "      <ser:placeOrder>" +
                         "         <ser:order>" +
                         "            <xsd:price>100</xsd:price>" +
                         "            <xsd:quantity>2000</xsd:quantity>" +
                         "            <xsd:symbol>JMSTransport</xsd:symbol>" +
                         "         </ser:order>" +
                         "      </ser:placeOrder>" +
                         "   </soapenv:Body>" +
                         "</soapenv:Envelope>";

        AXIOMUtil.stringToOM(payload);
        client.sendRobust(AXIOMUtil.stringToOM(payload), esbServer.getServiceUrl() + "/MainProxy", "placeOrder");
        client.sendRobust(AXIOMUtil.stringToOM(payload), esbServer.getServiceUrl() + "/MainProxy", "placeOrder");
        client.sendRobust(AXIOMUtil.stringToOM(payload), esbServer.getServiceUrl() + "/MainProxy", "placeOrder");

        Thread.sleep(60000); //wait until all message received to jms proxy


        LogViewerClient logViewerClient = new LogViewerClient(esbServer.getBackEndUrl(),
                                                              esbServer.getSessionCookie());
        LogEvent[] logs = logViewerClient.getAllSystemLogs();
        boolean terminate = false;
        for (LogEvent item : logs) {
            if (item.getPriority().equals("WARN")) {
                String message = item.getMessage();
                if (message.startsWith("Expiring message ID") && message.endsWith("dropping message after global timeout of : 120 seconds")) {
                    terminate = true;
                    break;
                } else {
                    continue;
                }
            } else {
                continue;
            }
        }

        Assert.assertTrue("Unnecessary Call Back Registered", !terminate);

    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
