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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.discovery.stub.types.*;
import org.wso2.carbon.discovery.stub.types.mgt.DiscoveryProxyDetails;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.lang.Exception;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertEquals;

public class DiscoveryProxyRemovalTestCase extends ESBIntegrationTestCase{

    private String proxyName = "DiscoveryProxy";

    private DiscoveryAdminStub discoveryStub = null;

    public DiscoveryProxyRemovalTestCase() {
        super("DiscoveryAdmin");
    }

    public void init() throws Exception {
        //Add Proxy for deleting
        String proxyURL = "http://localhost:9763/services/DiscoveryProxy";

        discoveryStub = new DiscoveryAdminStub(getAdminServiceURL());
        authenticate(discoveryStub);

        DiscoveryProxyDetails pd = new DiscoveryProxyDetails();
        pd.setName(proxyName);
        pd.setOnline(true);
        pd.setUrl(proxyURL);

        discoveryStub.addDiscoveryProxy(pd);
    }

    @Test(groups = "wso2.esb", description = "Test removal of a simple proxy")
    public void testSimpleProxyRemoval() throws org.wso2.carbon.discovery.stub.types.Exception, RemoteException {

        DiscoveryProxyDetails pd = discoveryStub.getDiscoveryProxy(proxyName);
        assertNotNull(pd);
        assertEquals(proxyName, pd.getName());

        discoveryStub.removeDiscoveryProxy(proxyName);
        pd = discoveryStub.getDiscoveryProxy(proxyName);
        assertNull(pd);
        log.info("Simple proxy removal test has passed");
    }
}
