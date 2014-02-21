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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.discovery.stub.types.*;
import org.wso2.carbon.discovery.stub.types.mgt.DiscoveryProxyDetails;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.lang.Exception;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class DiscoveryProxyUpdateTestCase extends ESBIntegrationTestCase {

    private String proxyName = "DiscoveryProxy";

    private DiscoveryAdminStub discoveryStub = null;

    private String proxyURL = "http://localhost:9763/services/DiscoveryProxy";

    public DiscoveryProxyUpdateTestCase() {
        super("DiscoveryAdmin");
    }

    public void init() throws Exception {
        //Add Proxy for editing
        discoveryStub = new DiscoveryAdminStub(getAdminServiceURL());
        authenticate(discoveryStub);

        DiscoveryProxyDetails pd = new DiscoveryProxyDetails();
        pd.setName(proxyName);
        pd.setOnline(true);
        pd.setUrl(proxyURL);

        discoveryStub.addDiscoveryProxy(pd);
    }

    @Test(groups = "wso2.esb", description = "Test Simple Proxy update")
    public void testSimpleProxyUpdate() throws org.wso2.carbon.discovery.stub.types.Exception, RemoteException {
        log.info("Testing WS-Discovery proxy update");

        String newProxyURL = "http://new.proxy.url:9763/services/DiscoveryProxy";

        DiscoveryProxyDetails proxy = discoveryStub.getDiscoveryProxy(proxyName);
        proxy.setUrl(newProxyURL);
        discoveryStub.updateDiscoveryProxy(proxy);

        DiscoveryProxyDetails newProxy = discoveryStub.getDiscoveryProxy(proxyName);

        assertEquals(proxyName, newProxy.getName());
        assertEquals(newProxyURL, newProxy.getUrl());
        assertNull(newProxy.getPolicy());
        assertFalse(newProxy.getOnline());

        log.info("Proxy update test has passed");
    }

    @AfterMethod(groups = "wso2.esb", description = "Remove the proxy added by the Before Method")
    public void removeProxy() throws org.wso2.carbon.discovery.stub.types.Exception, RemoteException {
        DiscoveryProxyDetails pd = discoveryStub.getDiscoveryProxy(proxyName);
        if(pd != null && pd.getName().equals(proxyName)){
            discoveryStub.removeDiscoveryProxy(proxyName);
        }
    }

}
