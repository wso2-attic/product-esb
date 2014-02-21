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

import org.apache.axis2.AxisFault;
import org.testng.annotations.Test;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

public class LoadBalanceEndpointTestCase extends ESBIntegrationTestCase {

    private static final String ENDPOINT_NAME = "lbEpTest";

    public LoadBalanceEndpointTestCase() {
        super("EndpointAdmin");
    }

    @Test(groups = {"wso2.esb"})
    public void testLoadBalanceEndpoint() throws RemoteException {
        EndpointAdminStub endpointAdminStub = null;
        try {
            endpointAdminStub = new EndpointAdminStub(getAdminServiceURL());
        } catch (AxisFault axisFault) {
            log.error("Error while creating the endpoint admin stub", axisFault);
        }

        authenticate(endpointAdminStub);

        cleanupEndpoints(endpointAdminStub);
        endpointAdditionScenario(endpointAdminStub);
        endpointStatisticsScenario(endpointAdminStub);
        endpointDeletionScenario(endpointAdminStub);
    }

    private void cleanupEndpoints(EndpointAdminStub endpointAdminStub) throws RemoteException {
        String[] endpointNames = new String[0];
        try {
            endpointNames = endpointAdminStub.getEndPointsNames();
        } catch (EndpointAdminEndpointAdminException e) {
            log.error("Unexpected error while obtaining the endpoint names", e);
        }
        List endpointList;
        if (endpointNames != null && endpointNames.length > 0 && endpointNames[0] != null) {
            endpointList = Arrays.asList(endpointNames);

            if (endpointList.contains(ENDPOINT_NAME)) {
                try {
                    endpointAdminStub.deleteEndpoint(ENDPOINT_NAME);
                } catch (EndpointAdminEndpointAdminException e) {
                    log.error("Unexpected error while deleting an endpoint", e);
                }
            }
        }
    }

    private void endpointAdditionScenario(EndpointAdminStub endpointAdminStub) throws RemoteException {
        int beforeCount = 0;
        try {
            beforeCount = endpointAdminStub.getEndpointCount();
        } catch (EndpointAdminEndpointAdminException e) {
            log.error("Error while obtaining the endpoint count", e);
        }

        try {
            endpointAdminStub.addEndpoint("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                          "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" + ENDPOINT_NAME + "\">\n" +
                                          "    <loadbalance algorithm=\"org.apache.synapse.endpoints.algorithms.RoundRobin\">\n" +
                                          "        <endpoint name=\"endpoint_urn_uuid_7E71CCD625D839E55A25548428059450-1123062013\">\n" +
                                          "            <address uri=\"http://webservices.amazon.com/AWSECommerceService/UK/AWSECommerceService.wsdl\"/>\n" +
                                          "        </endpoint>\n" +
                                          "    </loadbalance>\n" +
                                          "</endpoint>");
        } catch (EndpointAdminEndpointAdminException e) {
            log.error("Error while adding a new endpoint", e);
        }

        int afterCount = 0;
        try {
            afterCount = endpointAdminStub.getEndpointCount();
        } catch (EndpointAdminEndpointAdminException e) {
            log.error("Error while obtaining the endpoint count", e);
        }
        assertEquals(1, afterCount - beforeCount);

        String[] endpoints = new String[0];
        try {
            endpoints = endpointAdminStub.getEndPointsNames();
        } catch (EndpointAdminEndpointAdminException e) {
            log.error("Error while obtaining endpoint names", e);
        }
        if (endpoints != null && endpoints.length > 0 && endpoints[0] != null) {
            List endpointList = Arrays.asList(endpoints);
            assertTrue(endpointList.contains(ENDPOINT_NAME));
        } else {
            fail("Endpoint has not been added to the system properly");
        }

    }

    private void endpointStatisticsScenario(EndpointAdminStub endpointAdminStub) {
            try {
                endpointAdminStub.enableStatistics(ENDPOINT_NAME);
            } catch (EndpointAdminEndpointAdminException e) {
                return;
            } catch (RemoteException e) {
                return;
            }
        fail("Enabling statistics on a load-balance endpoint did not cause an error");
    }

    private void endpointDeletionScenario(EndpointAdminStub endpointAdminStub) throws RemoteException {
            int beforeCount = 0;
            try {
                beforeCount = endpointAdminStub.getEndpointCount();
            } catch (EndpointAdminEndpointAdminException e) {
                log.error("Error while obtaining the endpoint count", e);
            }
            try {
                endpointAdminStub.deleteEndpoint(ENDPOINT_NAME);
            } catch (EndpointAdminEndpointAdminException e) {
                log.error("Error while deleting an endpoint", e);
            }
            int afterCount = 0;
            try {
                afterCount = endpointAdminStub.getEndpointCount();
            } catch (EndpointAdminEndpointAdminException e) {
                log.error("Error while obtaining the endpoint count", e);
            }
            assertEquals(1, beforeCount - afterCount);

    }
}
