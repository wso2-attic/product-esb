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

package org.wso2.carbon.endpoint.test;

import org.testng.annotations.Test;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class DynamicAddressEpTestCase extends ESBIntegrationTestCase {
    private LoginLogoutUtil loginLogoutUtil = new LoginLogoutUtil();

    private static final String ENDPOINT_PATH_1 = "conf:/DynamicAddressEndpointConf";
    private static final String ENDPOINT_PATH_2 = "gov:/DynamicAddressEndpointGov";
    private static final String ENDPOINT_XML = "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                                                 "   <address uri=\"http://webservices.amazon.com/AWSECommerceService/UK/AWSECommerceService.wsdl\" >\n" +
                                                 "      <suspendOnFailure>\n" +
                                                 "         <progressionFactor>1.0</progressionFactor>\n" +
                                                 "      </suspendOnFailure>\n" +
                                                 "      <markForSuspension>\n" +
                                                 "         <retriesBeforeSuspension>0</retriesBeforeSuspension>\n" +
                                                 "         <retryDelay>0</retryDelay>\n" +
                                                 "      </markForSuspension>\n" +
                                                 "   </address>\n" +
                                                 "</endpoint>";

    public DynamicAddressEpTestCase() {
        super("EndpointAdmin");
    }

    @Test(groups ={"wso2.esb"})
    public void testDynamicAddressEndpoint()
            throws RemoteException, EndpointAdminEndpointAdminException {
        EndpointAdminStub endpointAdminStub = new EndpointAdminStub(getAdminServiceURL());

        authenticate(endpointAdminStub);
        cleanupEndpoints(endpointAdminStub);

        dynamicEndpointAdditionScenario(endpointAdminStub, ENDPOINT_PATH_1);
        dynamicEndpointAdditionScenario(endpointAdminStub, ENDPOINT_PATH_2);

        dynamicEndpointDeletionScenario(endpointAdminStub, ENDPOINT_PATH_1);
        dynamicEndpointDeletionScenario(endpointAdminStub, ENDPOINT_PATH_2);
    }

    private void cleanupEndpoints(EndpointAdminStub endpointAdminStub)
            throws RemoteException, EndpointAdminEndpointAdminException {
        String[] endpointNames = endpointAdminStub.getDynamicEndpoints();
        List endpointList;
        if (endpointNames != null && endpointNames.length > 0 && endpointNames[0] != null) {
            endpointList = Arrays.asList(endpointNames);

            if (endpointList.contains(ENDPOINT_PATH_1)) {
                endpointAdminStub.deleteEndpoint(ENDPOINT_PATH_1);
            }
            if (endpointList.contains(ENDPOINT_PATH_2)) {
                endpointAdminStub.deleteEndpoint(ENDPOINT_PATH_2);
            }
        }
    }

    private void dynamicEndpointAdditionScenario(EndpointAdminStub endpointAdminStub,
                                                 String path)
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endpointAdminStub.getDynamicEndpointCount();
        endpointAdminStub.addDynamicEndpoint(path, ENDPOINT_XML);
        assertEndpointAddition(path, beforeCount, endpointAdminStub);
    }

    private void dynamicEndpointDeletionScenario(EndpointAdminStub endpointAdminStub,
                                                 String path)
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endpointAdminStub.getDynamicEndpointCount();
        endpointAdminStub.deleteDynamicEndpoint(path);
        assertEndpointDeletion(beforeCount, endpointAdminStub);
    }

    private void assertEndpointDeletion(int beforeCount,
                                        EndpointAdminStub endpointAdminStub)
            throws RemoteException, EndpointAdminEndpointAdminException {
        int afterCount = endpointAdminStub.getDynamicEndpointCount();
        assertEquals(1, beforeCount - afterCount);
    }

    private void assertEndpointAddition(String path, int beforeCount,
                                        EndpointAdminStub endpointAdminStub)
            throws RemoteException, EndpointAdminEndpointAdminException {

        int afterCount = endpointAdminStub.getDynamicEndpointCount();
        assertEquals(1, afterCount - beforeCount);

        String[] endpointNames = endpointAdminStub.getDynamicEndpoints();
        if (endpointNames != null && endpointNames.length > 0 && endpointNames[0] != null) {
            assertTrue(Arrays.asList(endpointNames).contains(path));
        } else {
            fail("Dynamic endpoint hasn't been added successfully");
        }
    }
}
