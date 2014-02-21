/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.proxyservice.test.secureProxy;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.security.SecurityAdminServiceClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.SecureServiceClient;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ChangingPoliciesTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        loadESBConfigurationFromClasspath(
                "/artifacts/ESB/proxyconfig/proxy/secureProxy/stockquote_proxy_unsecured.xml");


    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

    @Test(groups = "wso2.esb", description = "- Secure proxy" +
                                             "- Change the policy and checking whether messages are processed accordingly" +
                                             "- used scenario1-policy(UsernameToken) and scenario5-policy(Sign and encrypt - X509 Authentication)")
    public void testPolicyChanges() throws Exception {

        OMElement response;
        String lastPrice;
        String symbol;
        SecureServiceClient secureAxisServiceClient;
        applySecurity(1); //only https available

        secureAxisServiceClient = new SecureServiceClient();
        response = secureAxisServiceClient.sendSecuredStockQuoteRequest(userInfo, getProxyServiceSecuredURL("StockQuoteProxy"), 1, "WSO2");

        lastPrice = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "last"))
                .getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        symbol = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "symbol"))
                .getText();
        assertEquals(symbol, "WSO2", "Fault: value 'symbol' mismatched");
        disableSecurity();

        applySecurity(5);  //uses http
        response = secureAxisServiceClient.sendSecuredStockQuoteRequest(userInfo, getProxyServiceSecuredURL("StockQuoteProxy"), 5, "IBM");

        lastPrice = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "last"))
                .getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        symbol = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "symbol"))
                .getText();
        assertEquals(symbol, "IBM", "Fault: value 'symbol' mismatched");
        disableSecurity();
    }


    private void applySecurity(int scenarioNumber)
            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException,
                   InterruptedException {
        applySecurity("StockQuoteProxy", scenarioNumber, getUserRole(userInfo.getUserId()));
    }

    private void disableSecurity()
            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException {
        SecurityAdminServiceClient securityAdminServiceClient = new SecurityAdminServiceClient(
                esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        securityAdminServiceClient.disableSecurity("StockQuoteProxy");
    }


}
