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
package org.wso2.carbon.esb.samples.test.messaging;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.JMSBrokerController;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;
import org.wso2.esb.integration.common.utils.servers.ActiveMQServer;

public class Sample264TestCase extends ESBIntegrationTest {

    private JMSBrokerController activeMqBroker;
    private ServerConfigurationManager serverManager = null;
    LogViewerClient logViewerClient = null;

    private ActiveMQServer activeMQServer
            = new ActiveMQServer();

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @BeforeClass(alwaysRun = true)
    public void startJMSBrokerAndConfigureESB() throws Exception {
        super.init();
        activeMQServer.startJMSBrokerAndConfigureESB();
        context = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
        super.init();
        loadSampleESBConfiguration(264);

        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        //activeMQServer.stopJMSBrokerRevertESBConfiguration();
        //reverting the changes done to esb sever
        Thread.sleep(10000); //let server to clear the artifact undeployment
        super.cleanup();
    }


    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        return JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration();
    }

    @Test(groups = {"wso2.esb"}, description = "Test JMS two way transport ")
    public void testJMSProxy() throws Exception {

        logViewerClient.clearLogs();
        Thread.sleep(2000);

        AxisServiceClient client = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            client.sendRobust(Utils.getStockQuoteRequest("WSO2"), getMainSequenceURL(), "getQuote");
        }

        LogEvent[] logs = logViewerClient.getAllSystemLogs();
        boolean wso2Found = false;
        for (LogEvent item : logs) {
            String message = item.getMessage();
            if (message.contains("<ns:symbol>WSO2</ns:symbol>")) {
                wso2Found = true;
                break;
            }
        }

        Assert.assertTrue(wso2Found, "JMS messages not sent");

    }

    private OMElement createPayload() {   // creation of payload for placeOrder

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ser");
        OMNamespace xsdNs = fac.createOMNamespace("http://services.samples", "xsd");
        OMElement payload = fac.createOMElement("placeOrder", omNs);
        OMElement order = fac.createOMElement("order", omNs);

        OMElement price = fac.createOMElement("price", xsdNs);
        price.setText("10");
        OMElement quantity = fac.createOMElement("quantity", xsdNs);
        quantity.setText("100");
        OMElement symbol = fac.createOMElement("symbol", xsdNs);
        symbol.setText("WSO2");

        order.addChild(price);
        order.addChild(quantity);
        order.addChild(symbol);
        payload.addChild(order);
        return payload;
    }

}
