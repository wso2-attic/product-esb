/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.esb.message.processor.test.jms.forwarding;

import java.io.IOException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.extensions.axis2server.Axis2ServerManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

/**
 * This class verifies the proper functionality of the Forwarding message
 * processor after introducing the ntask components. This checks basic life
 * cycle related scenarios such as forwarding to the back end etc.
 *
 */
public class ForwardingMessageProcessorWithJMSQueueTestCase extends ESBIntegrationTest {
    private static final int MAX_DELIVERY_ATTEMPTS = 4;
    private LogViewerClient logViewerClient;
    private Axis2ServerManager axis2Server;

    @BeforeClass(alwaysRun = true)
    public void deployeService() throws Exception {
        super.init();
        /*
         * Deploying Message store, message processor,proxy service, endpoint
         * and reply sequence.
         */
        axis2Server = new Axis2ServerManager("test_axis2_server_9001.xml");
        axis2Server.deployService("SimpleStockQuoteService");
        axis2Server.start();
        loadESBConfigurationFromClasspath("/artifacts/ESB/messageProcessorConfig/ForwardingProcessorJMSStore_synapse_config.xml");
        isProxyDeployed("Proxy2");
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }

    /**
     * Tests whether a SOAP request is sent to the back end Stock Quote service
     * successfully.
     * 
     * @throws Exception
     */
    @Test(groups = { "wso2.esb" }, description = "Test Message Forwarding Processor With JMS queue.")
    public void testMessageForwardingProcessorWithJMSQueue() throws Exception {
        /*
         * invoking the proxy service and getting the response using a service
         * client
         */
        // invoking the service through the proxy service
        final String proxyUrl = getProxyServiceURLHttp("Proxy2");

        final String expectedSymbol = "IBM Company";
        final String expectedCompany = "IBM";
        boolean responseInLog = false;
        // invoking the service through the proxy service
        AxisServiceClient client = new AxisServiceClient();
        client.sendRobust(getStockQuoteRequest("IBM"), proxyUrl, "getQuote");

        Thread.sleep(2000);

        LogEvent[] logs = logViewerClient.getAllSystemLogs();

        for (LogEvent logEvent : logs) {
            String message = logEvent.getMessage();
            if (message.contains(getExpectedSOAPResponse()) && message.contains(expectedSymbol) &&
                message.contains(expectedCompany)) {
                responseInLog = true;
                break;
            }
        }

        // Asserting the results here.
        Assert.assertTrue(responseInLog, "Expected SOAP Response was NOT found in the LOG stream.");
    }

    /**
     * Checks whether the message Processor is getting deactivated successfully
     * after reaching the maximum delivery attempts when the back end service is
     * down.
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    @Test(groups = { "wso2.esb" }, description = "Test Message Forwarding Processor Deactivation after Max delivery attempts reached.")
    public void testMessageProcessorDeactivationOnMaxDelivaryAttempts()
                                                                       throws InterruptedException,
                                                                       IOException {
        final String expectedDeactivationMsg =
                                               "Successfully deactivated the message processor [Processor1]";
        final String expectedErrorMsg =
                                        "BlockingMessageSender of message processor [Processor1] failed to send message to the endpoint";
        boolean isDeactivated = false;
        int maxDeliveryAttmpts = 0;

        // invoking the service through the proxy service
        final String proxyUrl = getProxyServiceURLHttp("Proxy2");

        // invoking the service through the proxy service
        AxisServiceClient client = new AxisServiceClient();

        // Stopping the axis2 Server before sending the client request.
        axis2Server.stop();

        Thread.sleep(2000);

        try {
            client.sendRobust(getStockQuoteRequest("IBM"), proxyUrl, "getQuote");
        } catch (AxisFault axisFault) {
            // Just ignore this.
        }

        Thread.sleep(6000);

        LogEvent[] logs = logViewerClient.getAllSystemLogs();

        /*
         * Checking whether the max Delivery attempt is reached.
         */
        for (LogEvent logEvent : logs) {
            String message = logEvent.getMessage();
            if (message.contains(expectedErrorMsg)) {
                maxDeliveryAttmpts++;
            }
        }

        /*
         * Checking whether the message processor is deactivated successfully.
         */
        for (LogEvent logEvent : logs) {
            String message = logEvent.getMessage();
            if (message.contains(expectedDeactivationMsg)) {
                isDeactivated = true;
                break;
            }
        }

        Assert.assertEquals(maxDeliveryAttmpts, MAX_DELIVERY_ATTEMPTS,
                            "Max Delivery Attempt is NOT reached.");
        Assert.assertTrue(isDeactivated,
                          "Message Processor has NOT been deactivated after reaching max delivery attempts.");

        // Makesure to restart the Axis2Server once you are done.
        axis2Server.start();
    }

    private String getExpectedSOAPResponse() {
        final String expectedResponse =
                                        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                                                + "<soapenv:Body><ns:getQuoteResponse xmlns:ns=\"http://services.samples\">";

        return expectedResponse;
    }

    private OMElement getStockQuoteRequest(String symbol) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement method = fac.createOMElement("getQuote", omNs);
        OMElement value1 = fac.createOMElement("request", omNs);
        OMElement value2 = fac.createOMElement("symbol", omNs);

        value2.addChild(fac.createOMText(value1, symbol));
        value1.addChild(value2);
        method.addChild(value1);

        return method;
    }

    @AfterClass(alwaysRun = true)
    public void UndeployeService() throws Exception {
        super.cleanup();
        if (axis2Server != null) {
            axis2Server.stop();
        }
    }

}
