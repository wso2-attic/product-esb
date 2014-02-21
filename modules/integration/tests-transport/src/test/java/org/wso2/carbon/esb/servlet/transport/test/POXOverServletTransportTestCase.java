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

package org.wso2.carbon.esb.servlet.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.http.HttpRequest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.httpserverutils.RequestInterceptor;
import org.wso2.carbon.automation.core.utils.httpserverutils.SimpleHttpServer;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;

import java.io.File;
import java.io.IOException;


/**
 * This test case starts an ESB instance with the servlet transport and performs a SOAP to
 * POX conversion through it. It makes sure that POX messages are passed to backend service
 * properly. This test case verifies the fixes done for CARBON-5993.
 */
public class POXOverServletTransportTestCase extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        serverConfigurationManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        serverConfigurationManager.applyConfiguration(new File(getESBResourceLocation() + File.separator
                                                               + "synapseconfig" + File.separator + "servletTransport" + File.separator + "pox_servlet_transport_axis2.xml"));
        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB"
                                          + File.separator + "synapseconfig" + File.separator + "servletTransport" + File.separator + "soap_2_pox.xml");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.esb", description = "Tests SOAP to POX Conversion")
    public void testSoapToPOXConversion() throws IOException, InterruptedException {

        SimpleHttpServer httpServer = new SimpleHttpServer();
        try {
            httpServer.start();
            Thread.sleep(5000);
        } catch (IOException e) {
            log.error("Error while starting the HTTP server", e);
        }

        TestRequestInterceptor interceptor = new TestRequestInterceptor();
        httpServer.getRequestHandler().setInterceptor(interceptor);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                getProxyServiceURL("SOAP2POX"),
                null, "WSO2");
        log.info("Response received: " + response);
        Assert.assertEquals(interceptor.getLastRequestURI(), "/services/SimpleStockQuoteService");

        try {
            httpServer.stop();
        } catch (IOException e) {
            log.warn("Error while shutting down the HTTP server", e);
        }

    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() throws Exception {
        try {
            super.cleanup();
        } finally {
            Thread.sleep(3000);
            serverConfigurationManager.restoreToLastConfiguration();
        }


    }

    private static class TestRequestInterceptor implements RequestInterceptor {

        private String lastRequestURI;

        public void requestReceived(HttpRequest request) {
            lastRequestURI = request.getRequestLine().getUri();
        }

        public String getLastRequestURI() {
            return lastRequestURI;
        }
    }

}
