/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.message.processor.test;

import java.io.File;
import java.net.URL;

import javax.activation.DataHandler;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.integration.common.admin.client.CarbonAppUploaderClient;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;

public class ESBJAVA3239_DeactivatedMPBehaviourOnRestartsWithCappTestCase extends
                                                                         ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManager;
    private CarbonAppUploaderClient carbonAppUploaderClient;
    private SampleAxis2Server axis2Server;
    private static final String PROXY_SERVICE_NAME = "ProxyMP";
    private static final String EXPECTED_ERROR_MESSAGE =
                                                         "Error occurred while deploying Carbon Application";
    private LogViewerClient logViewerClient;

    @BeforeClass(alwaysRun = true)
    public void deployeService() throws Exception {
        super.init();
        context = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
        serverConfigurationManager = new ServerConfigurationManager(context);
        carbonAppUploaderClient =
                                  new CarbonAppUploaderClient(contextUrls.getBackEndUrl(),
                                                              getSessionCookie());
        axis2Server = new SampleAxis2Server("test_axis2_server_9001.xml");
        axis2Server.deployService(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
        axis2Server.start();
        /*
         * Deploying Message store, message processor,proxy service, endpoint
         * and reply sequence.
         */
        carbonAppUploaderClient.uploadCarbonAppArtifact("DeactivatedMPUpOnServerRestart-capp_1.0.0.car",
                                                        new DataHandler(
                                                                        new URL(
                                                                                "file:" +
                                                                                        File.separator +
                                                                                        File.separator +
                                                                                        getESBResourceLocation() +
                                                                                        File.separator +
                                                                                        "car" +
                                                                                        File.separator +
                                                                                        "DeactivatedMPUpOnServerRestart-capp_1.0.0.car")));
        isProxyDeployed(PROXY_SERVICE_NAME);
    }

    @Test(groups = { "wso2.esb" }, description = "Test whether a deactivated Message Forwarding Processor is deployed successfully upon ESB server reestarts when it is uploaded via a capp.")
    public void testDeactivatedMPUponServerRestart() throws Exception {
        boolean isDeploymentError = false;
        // invoking the service through the proxy service
        final String proxyUrl = getProxyServiceURLHttp(PROXY_SERVICE_NAME);

        // invoking the service through the proxy service
        AxisServiceClient client = new AxisServiceClient();

        // Stopping the axis2 Server before sending the client request.
        axis2Server.stop();

        try {
            client.sendRobust(createPlaceOrderRequest(3.141593E0, 4, "IBM"), proxyUrl, "placeOrder");
        } catch (AxisFault axisFault) {
            // Just ignore this.
        }

        /*
         * Wait till the MP deactivates successfully. It usually takes 2 * 4
         * secs with this configuration.
         */
        Thread.sleep(15000);

        axis2Server.start();
        Thread.sleep(5000);

        /*
         * Restart the ESB Server after the MP is deactivated.
         */
        serverConfigurationManager.restartGracefully();

        super.init();
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());

        // Waits until the ESB restarts
        Thread.sleep(10000);

        LogEvent[] logs = logViewerClient.getAllSystemLogs();
        for (LogEvent logEvent : logs) {
            String message = logEvent.getMessage();
            if (message.contains(EXPECTED_ERROR_MESSAGE)) {
                isDeploymentError = true;
            }
        }

        Assert.assertTrue(!isDeploymentError,
                          "Message Processor was not deployed successfully upon a restart.");

    }

    /*
     * This method will create a request required for place orders
     */
    public static OMElement createPlaceOrderRequest(double purchPrice, int qty, String symbol) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://services.samples", "m0");
        OMElement placeOrder = factory.createOMElement("placeOrder", ns);
        OMElement order = factory.createOMElement("order", ns);
        OMElement price = factory.createOMElement("price", ns);
        OMElement quantity = factory.createOMElement("quantity", ns);
        OMElement symb = factory.createOMElement("symbol", ns);
        price.setText(Double.toString(purchPrice));
        quantity.setText(Integer.toString(qty));
        symb.setText(symbol);
        order.addChild(price);
        order.addChild(quantity);
        order.addChild(symb);
        placeOrder.addChild(order);
        return placeOrder;
    }

    @AfterClass(alwaysRun = true)
    public void UndeployeService() throws Exception {
        if (axis2Server.isStarted()) {
            axis2Server.stop();
        }
        axis2Server = null;
    }

}
