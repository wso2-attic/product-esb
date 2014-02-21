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

package org.wso2.esb.integration.servlet.transport;

import org.apache.axiom.om.OMElement;
import org.apache.http.HttpRequest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;
import org.wso2.esb.integration.http.RequestInterceptor;
import org.wso2.esb.integration.http.SimpleHttpServer;

import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;

/**
 * This test case starts an ESB instance with the servlet transport and performs a SOAP to
 * POX conversion through it. It makes sure that POX messages are passed to backend service
 * properly. This test case verifies the fixes done for CARBON-5993.
 */
public class POXOverServletTransportTestCase extends ESBIntegrationTestCase{

    private StockQuoteClient axis2Client;

    @Test(groups = "wso2.esb", description = "Tests SOAP to POX Conversion")
    public void testSoapToPOXConversion() throws RemoteException {

        axis2Client = new StockQuoteClient();

        loadESBConfigurationFromClasspath("/soap_2_pox.xml");

        SimpleHttpServer httpServer = new SimpleHttpServer();
        try {
            httpServer.start();
        } catch (IOException e) {
            log.error("Error while starting the HTTP server", e);
        }

        TestRequestInterceptor interceptor = new TestRequestInterceptor();
        httpServer.getRequestHandler().setInterceptor(interceptor);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                getProxyServiceURL("SOAP2POX", true),
                "http://localhost:9000/services/SimpleStockQuoteService", "WSO2");
        log.info("Response received: " + response);
        assertEquals("/services/SimpleStockQuoteService", interceptor.getLastRequestURI());

        try {
            httpServer.stop();
        } catch (IOException e) {
            log.warn("Error while shutting down the HTTP server", e);
        }

    }

    @AfterMethod(groups = "wso2.esb")
    public void cleanUp(){
        super.cleanup();
        axis2Client.destroy();
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
