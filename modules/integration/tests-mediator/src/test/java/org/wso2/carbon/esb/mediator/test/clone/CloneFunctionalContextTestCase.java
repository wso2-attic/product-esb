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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mediator.test.clone;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.api.clients.logging.LoggingAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Iterator;

/*
 * This tests tests endpoints from governors registry and configuration registry
 * for the clone mediator
 */

public class CloneFunctionalContextTestCase extends ESBIntegrationTest {

    private LogViewerClient logViewer;
    private LoggingAdminClient logAdmin;
    
    @BeforeClass(groups = "wso2.esb")
    public void setEnvironment() throws Exception {
        init();
        logAdmin = new LoggingAdminClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        logViewer = new LogViewerClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());        
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/clone/clone_functional_context.xml");
    }

    @Test(groups = "wso2.esb", description = "Tests SEQUENCES from  the governance registry and configuration registry")
    public void testSequence() throws Exception {
        logAdmin.updateLoggerData("org.apache.synapse", LoggingAdminClient.logLevel.DEBUG.name(), true, false);
        logViewer.clearLogs();
        
        OMElement response=axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL
                                                                   ("StockQuoteProxy"), null, "IBM");
        Assert.assertNotNull(response);
        
        LogEvent[] getLogsInfo = logViewer.getAllSystemLogs();
        boolean assertValue = false;
        for(LogEvent event:getLogsInfo){
        	if(event.getMessage().contains("REQUEST PARAM VALUE")){
        		assertValue = true;
        	}
        }
        Assert.assertTrue(assertValue, "Synapse functional context not cloned.");
    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        super.cleanup();
    }

}

