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
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class URIWSDLEndpointTestCase extends ESBIntegrationTestCase {

    private static final String ENDPOINT_NAME = "wsdlURITest";

    public URIWSDLEndpointTestCase() {
        super("EndpointAdmin");
    }

    @Test(groups = {"wso2.esb"})
    public void testURIWSDLEndpoint() throws RemoteException, EndpointAdminEndpointAdminException {
        EndpointAdminStub endpointAdminStub = new EndpointAdminStub(getAdminServiceURL());

        authenticate(endpointAdminStub);

        cleanupEndpoints(endpointAdminStub);
        endpointAdditionScenario(endpointAdminStub);
        endpointStatisticsScenario(endpointAdminStub);
        endpointDeletionScenario(endpointAdminStub);
    }

    private void cleanupEndpoints(EndpointAdminStub endpointAdminStub)
            throws RemoteException, EndpointAdminEndpointAdminException {
        String[] endpointNames = endpointAdminStub.getEndPointsNames();
        List endpointList;
        if (endpointNames != null && endpointNames.length > 0 && endpointNames[0] != null) {
            endpointList = Arrays.asList(endpointNames);
            if (endpointList.contains(ENDPOINT_NAME)) {
                endpointAdminStub.deleteEndpoint(ENDPOINT_NAME);
            }
        }
    }

    private void endpointAdditionScenario(EndpointAdminStub endpointAdminStub)
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endpointAdminStub.getEndpointCount();
        endpointAdminStub.addEndpoint("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                      "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\" name=\"wsdlURITest\">\n" +
                                      "    <wsdl service=\"AWSECommerceService\" port=\"AWSECommerceServicePort\" uri=\"http://webservices.amazon.com/AWSECommerceService/JP/AWSECommerceService.wsdl\"/>\n" +
                                      "</endpoint>");

        int afterCount = endpointAdminStub.getEndpointCount();
        assertEquals(1, afterCount - beforeCount);

        String[] endpoints = endpointAdminStub.getEndPointsNames();
        if (endpoints != null && endpoints.length > 0 && endpoints[0] != null) {
            List endpointList = Arrays.asList(endpoints);
            assertTrue(endpointList.contains(ENDPOINT_NAME));
        } else {
            fail("Endpoint has not been added to the system properly");
        }
    }

    private void endpointStatisticsScenario(EndpointAdminStub endpointAdminStub)
            throws RemoteException, EndpointAdminEndpointAdminException {
        endpointAdminStub.enableStatistics(ENDPOINT_NAME);
        String endpoint = endpointAdminStub.getEndpointConfiguration(ENDPOINT_NAME);
        assertTrue(endpoint.contains("statistics=\"enable"));
    }

    private void endpointDeletionScenario(EndpointAdminStub endpointAdminStub)
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endpointAdminStub.getEndpointCount();
        endpointAdminStub.deleteEndpoint(ENDPOINT_NAME);
        int afterCount = endpointAdminStub.getEndpointCount();
        assertEquals(1, beforeCount - afterCount);
    }
}
