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

import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class ProxyAdminServiceTestCase extends ESBIntegrationTestCase {

    private ProxyServiceAdminStub proxyServiceAdminStub;
    private static final String PROXY_NAME = "TestProxy";

    public ProxyAdminServiceTestCase() {
        super("ProxyServiceAdmin");
    }

    public void init() throws Exception {
        proxyServiceAdminStub = new ProxyServiceAdminStub(getAdminServiceURL());
        authenticate(proxyServiceAdminStub);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        try {
            proxyServiceAdminStub.cleanup();
        } catch (AxisFault ignored) {

        }
    }

    @Test(groups = {"wso2.esb"},
          description = "Test available transports to see if http and https are available.")
    public void testGetTransports() throws RemoteException, ProxyServiceAdminProxyAdminException {
        String[] transports = proxyServiceAdminStub.getAvailableTransports();
        assertTrue(transports != null && transports.length > 0 && transports[0] != null);
        boolean httpFound = false;
        boolean httpsFound = false;
        for (String t : transports) {
            if ("https".equals(t)) {
                httpsFound = true;
            } else if ("http".equals(t)) {
                httpFound = true;
            }
        }
        assertTrue(httpFound && httpsFound);
    }

    @Test(description = "Test available sequences to check if the 'fault' and 'main' sequences are" +
                        "available.",
          dependsOnMethods = "testGetTransports")
    public void testGetSequences() throws RemoteException, ProxyServiceAdminProxyAdminException {
        String[] sequences = proxyServiceAdminStub.getAvailableSequences();
        assertTrue(sequences != null && sequences.length > 0 && sequences[0] != null);
        boolean mainFound = false;
        boolean faultFound = false;
        for (String t : sequences) {
            if ("fault".equals(t)) {
                faultFound = true;
            } else if ("main".equals(t)) {
                mainFound = true;
            }
        }
        assertTrue(mainFound && faultFound);
    }

    @Test(description="Test proxy manipulation.", dependsOnMethods = "testGetSequences",
          expectedExceptions = {RemoteException.class, ProxyServiceAdminProxyAdminException.class})
    public void testProxyManipulation()
            throws RemoteException, ProxyServiceAdminProxyAdminException {
        ProxyData proxyData = new ProxyData();
        proxyData.setName(PROXY_NAME);
        proxyData.setOutSeqXML("<outSequence xmlns=\"http://ws.apache.org/ns/synapse\"><send /></outSequence>");
        proxyData.setEndpointXML("<endpoint xmlns=\"http://ws.apache.org/ns/synapse\">" +
                "<address uri=\"http://localhost:9000/services/SimpleStockQuoteService\"/></endpoint>");
        proxyData.setWsdlURI("file:repository/samples/resources/proxy/sample_proxy_1.wsdl");

        proxyServiceAdminStub.addProxy(proxyData);
        assertNotNull(proxyServiceAdminStub.getProxy(PROXY_NAME));

        proxyServiceAdminStub.enableStatistics(PROXY_NAME);
        ProxyData newProxy = proxyServiceAdminStub.getProxy(PROXY_NAME);
        assertTrue(newProxy.getEnableStatistics());

        proxyServiceAdminStub.disableStatistics(PROXY_NAME);
        newProxy = proxyServiceAdminStub.getProxy(PROXY_NAME);
        assertFalse(newProxy.getEnableStatistics());

        proxyServiceAdminStub.enableTracing(PROXY_NAME);
        newProxy = proxyServiceAdminStub.getProxy(PROXY_NAME);
        assertTrue(newProxy.getEnableTracing());

        proxyServiceAdminStub.disableTracing(PROXY_NAME);
        newProxy = proxyServiceAdminStub.getProxy(PROXY_NAME);
        assertFalse(newProxy.getEnableTracing());

        proxyServiceAdminStub.deleteProxyService(PROXY_NAME);

        proxyServiceAdminStub.getProxy(PROXY_NAME);
    }
}
