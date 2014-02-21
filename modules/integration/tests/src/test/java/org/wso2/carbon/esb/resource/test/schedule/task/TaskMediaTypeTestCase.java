/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.esb.resource.test.schedule.task;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.tasks.TaskAdminClient;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.registry.resource.stub.beans.xsd.MetadataBean;
import org.wso2.carbon.task.stub.TaskManagementException;

import javax.activation.DataHandler;
import java.rmi.RemoteException;

public class TaskMediaTypeTestCase {
    private Log log = LogFactory.getLog(TaskMediaTypeTestCase.class);

    private TaskAdminClient taskAdminClient;
    private ResourceAdminServiceClient resourceAdmin;
    private final String TASK_NAME = "automationScheduleTask";
    private final String TASK_GROUP = "synapse.simple.quartz";
    private boolean isTaskExist = false;

    @BeforeClass
    public void init() throws Exception {
        EnvironmentVariables esbServer;
        EnvironmentBuilder builder = new EnvironmentBuilder().esb(1);
        esbServer = builder.build().getEsb();
        taskAdminClient = new TaskAdminClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        resourceAdmin = new ResourceAdminServiceClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
    }
    //since Registry persistence is no longer available
    @Test(groups = {"wso2.esb"}, description = "Test Schedule Task media type - text/xml", enabled = false)
    public void scheduleTaskMediaTypeTest() throws Exception {

        OMElement task = AXIOMUtil.stringToOM("<task:task xmlns:task=\"http://www.wso2.org/products/wso2commons/tasks\" " +
                                              "name=\"" + TASK_NAME + "\" " +
                                              "class=\"org.apache.synapse.startup.tasks.MessageInjector\" " +
                                              "group=\"" + TASK_GROUP + "\">" +
                                              "<task:trigger count=\"5\" " +
                                              "interval=\"100000\" />" +
                                              "</task:task>");
        taskAdminClient.addTask((DataHandler) task);
        isTaskExist = true;
        //addEndpoint is a a asynchronous call, it will take some time to write to a registry
        Thread.sleep(10000);
        MetadataBean metadata = resourceAdmin.getMetadata("/_system/config/repository/synapse/default/synapse-startups/" + TASK_NAME);
        Assert.assertEquals(metadata.getMediaType(), "text/xml", "Media Type mismatched for Schedule Task");

    }

    @AfterClass
    public void destroy() throws TaskManagementException, RemoteException {
        if (isTaskExist) {
            taskAdminClient.deleteTask(TASK_NAME, TASK_GROUP);
        }
    }
}
