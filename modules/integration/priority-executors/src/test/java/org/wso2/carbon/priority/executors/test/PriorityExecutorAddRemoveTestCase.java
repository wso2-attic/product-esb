/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.priority.executors.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.priority.executors.stub.PriorityMediationAdminStub;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

public class PriorityExecutorAddRemoveTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(PriorityExecutorAddRemoveTestCase.class);

    @Test(groups = "wso2.esb")
    public void testPriorityMediatorAddRemove() throws RemoteException, XMLStreamException {
        PriorityMediationAdminStub priorityMediationAdminStub =
                new PriorityMediationAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" +
                        FrameworkSettings.HTTPS_PORT + "/services/PriorityMediationAdmin");

        authenticate(priorityMediationAdminStub);

        String priorityExConfig = "<priority-executor xmlns=\"http://ws.apache.org/ns/synapse\" name=\"ex\">\n" +
                "   <queues>\n" +
                "      <queue size=\"34\" priority=\"2\" />\n" +
                "      <queue size=\"23\" priority=\"1\" />\n" +
                "   </queues>\n" +
                "   <threads max=\"100\" core=\"20\" keep-alive=\"5\" />\n" +
                "</priority-executor>";

        OMElement omElement = AXIOMUtil.stringToOM(priorityExConfig);
        priorityMediationAdminStub.add("TestExecutor", omElement);

        OMElement result = priorityMediationAdminStub.getExecutor("TestExecutor");

        if (result != null) {
            log.info("Priority Executor Added Successfully");
        } else {
            log.error("Priority Executor Adding Failed: cannot find priority executor with the name - TestExecutor");

        }
        priorityMediationAdminStub.remove("TestExecutor");
    }

}
