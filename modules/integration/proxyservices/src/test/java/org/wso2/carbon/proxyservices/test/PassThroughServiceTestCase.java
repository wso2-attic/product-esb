/**
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

package org.wso2.carbon.proxyservices.test;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.Test;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;

public class PassThroughServiceTestCase extends ESBIntegrationTestCase {

    private static final String PROXY_NAME = "StockQuoteProxy";

    private StockQuoteClient axis2Client;

    public PassThroughServiceTestCase() {
        super("ProxyServiceAdmin");
    }

    @Override
    protected void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }

    @Test(groups = {"wso2.esb"}, description = "Test Pass through service.")
    public void testPassThroughService() throws IOException, ProxyServiceAdminProxyAdminException {
        ProxyServiceAdminStub proxyServiceAdminStub = new ProxyServiceAdminStub(getAdminServiceURL());
        authenticate(proxyServiceAdminStub);
        createProxy(proxyServiceAdminStub);
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL(PROXY_NAME, false), null, "WSO2");
        log.info("Received response from proxy service: " + response);
        deleteProxy(proxyServiceAdminStub);
    }

    private void createProxy(ProxyServiceAdminStub proxyServiceAdminStub)
            throws RemoteException, ProxyServiceAdminProxyAdminException {
        ProxyData proxyData = new ProxyData();
        proxyData.setName(PROXY_NAME);
        proxyData.setOutSeqXML("<outSequence xmlns=\"http://ws.apache.org/ns/synapse\"><send /></outSequence>");
        proxyData.setEndpointXML("<endpoint xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<address uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/></endpoint>");
        proxyData.setWsdlURI("file:repository/samples/resources/proxy/sample_proxy_1.wsdl");
        proxyServiceAdminStub.addProxy(proxyData);
        assertNotNull(proxyServiceAdminStub.getProxy(PROXY_NAME));
    }

    private void deleteProxy(ProxyServiceAdminStub proxyServiceAdminStub)
            throws RemoteException, ProxyServiceAdminProxyAdminException {
        proxyServiceAdminStub.deleteProxyService(PROXY_NAME);
    }
}
