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

package org.wso2.carbon.discovery.test;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.discovery.stub.types.*;
import org.wso2.carbon.discovery.stub.types.mgt.DiscoveryProxyDetails;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.lang.Exception;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class DiscoveryProxyAdditionTestCase extends ESBIntegrationTestCase{

    private String proxyName = "DiscoveryProxy";

    private DiscoveryAdminStub discoveryStub = null;

    public DiscoveryProxyAdditionTestCase() {
        super("DiscoveryAdmin");
    }

    @Test(groups = "wso2.esb", description = "Testing simple proxy addition")
    public void testSimpleProxyAddition() throws RemoteException, org.wso2.carbon.discovery.stub.types.Exception {
        
        discoveryStub = new DiscoveryAdminStub(getAdminServiceURL());
        authenticate(discoveryStub);

        log.info("Testing simple WS-Discovery proxy addition");

        String proxyURL = "http://localhost:9763/services/DiscoveryProxy";
        DiscoveryProxyDetails pd = new DiscoveryProxyDetails();
        pd.setName(proxyName);
        pd.setOnline(true);
        pd.setUrl(proxyURL);
        discoveryStub.addDiscoveryProxy(pd);

        DiscoveryProxyDetails[] proxyInfo = discoveryStub.getDiscoveryProxies();
        assertEquals(1, proxyInfo.length);
        assertNotNull(proxyInfo[0]);

        DiscoveryProxyDetails newProxy = discoveryStub.getDiscoveryProxy(proxyName);
        assertEquals(proxyName, newProxy.getName());
        assertEquals(proxyURL, newProxy.getUrl());
        assertTrue(newProxy.getOnline());
        assertNull(newProxy.getPolicy());
        log.info("Simple proxy addition test has passed");
    }

    @AfterMethod(groups = "wso2.esb", description = "Remove the proxy added by the Test Case")
    public void removeProxy() throws org.wso2.carbon.discovery.stub.types.Exception, RemoteException {
        DiscoveryProxyDetails pd = discoveryStub.getDiscoveryProxy(proxyName);
        if(pd != null && pd.getName().equals(proxyName)){
            discoveryStub.removeDiscoveryProxy(proxyName);
        }
    }

}
