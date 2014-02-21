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

package org.wso2.carbon.esb.endpoint.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.endpoint.EndPointAdminClient;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.esb.endpoint.test.util.EndpointTestUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;

public class DynamicFailOverEpTestCase {
    private EndPointAdminClient endPointAdminClient;
    private final String ENDPOINT_PATH_1 = "conf:/DynamicFOEndpointConf";
    private final String ENDPOINT_PATH_2 = "gov:/DynamicFOEndpointGov";
    private final String ENDPOINT_XML = "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\" name=\"anonymous\">\n" +
                                               "   <failover>\n" +
                                               "      <endpoint name=\"endpoint_urn_uuid_9582E686A7E285451E20649893138844-540148289\">\n" +
                                               "         <address uri=\"http://webservices.amazon.com/AWSECommerceService/UK/AWSECommerceService.wsdl\"/>\n" +
                                               "      </endpoint>\n" +
                                               "   </failover>\n" +
                                               "</endpoint>";


    @Test(groups = {"wso2.esb"})
    public void testDynamicFailOverEndpoint() throws Exception {
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);
        EnvironmentVariables esbServer = builder.build().getEsb();
        endPointAdminClient = new EndPointAdminClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());

        cleanupEndpoints();

        dynamicEndpointAdditionScenario(ENDPOINT_PATH_1);
        dynamicEndpointAdditionScenario(ENDPOINT_PATH_2);

        dynamicEndpointDeletionScenario(ENDPOINT_PATH_1);
        dynamicEndpointDeletionScenario(ENDPOINT_PATH_2);
        endPointAdminClient = null;
    }

    private void cleanupEndpoints()
            throws RemoteException, EndpointAdminEndpointAdminException {
        EndpointTestUtils.cleanupDynamicEndpoint(ENDPOINT_PATH_1, endPointAdminClient);
        EndpointTestUtils.cleanupDynamicEndpoint(ENDPOINT_PATH_2, endPointAdminClient);
    }

    private void dynamicEndpointAdditionScenario(String path)
            throws IOException, EndpointAdminEndpointAdminException, XMLStreamException {
        int beforeCount = endPointAdminClient.getDynamicEndpointCount();
        endPointAdminClient.addDynamicEndPoint(path, AXIOMUtil.stringToOM(ENDPOINT_XML));
        EndpointTestUtils.assertDynamicEndpointAddition(path, beforeCount, endPointAdminClient);

    }

    private void dynamicEndpointDeletionScenario(String path)
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endPointAdminClient.getDynamicEndpointCount();
        endPointAdminClient.deleteDynamicEndpoint(path);
        EndpointTestUtils.assertDynamicEndpointDeletion(beforeCount, endPointAdminClient);
    }


}
