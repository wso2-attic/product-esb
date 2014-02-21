package org.wso2.carbon.mediator.tests.clone;/*
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

import static org.testng.Assert.assertTrue;

import java.rmi.RemoteException;

import org.apache.axiom.om.OMElement;
import org.jboss.logging.LogMessage;
import org.testng.annotations.Test;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

public class InlineEndpointTestCase extends ESBIntegrationTestCase {

    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"})
    // https://wso2.org/jira/browse/ESBJAVA-1046
    public void testInlineEndpoint() throws RemoteException {
        boolean isInlineMsgFound = false;
        String filePath = "/mediators/clone/Inline_endpoint.xml";
        loadESBConfigurationFromClasspath(filePath);

        LogViewerStub logViewerStub = new LogViewerStub(getAdminServiceURL("LogViewer"));
        authenticate(logViewerStub);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                null, getMainSequenceURL(), "IBM");
        assertTrue(response.toString().contains("IBM"));

        LogEvent[] logs = logViewerStub.getLogs("INFO", "LogMediator");
        assertTrue(logs != null && logs.length > 0 && logs[0] != null);

        for (LogEvent l : logs) {
            if (l.getMessage().contains("CLONE1-TARGET2")) {
                isInlineMsgFound = true;
            }
        }
        assertTrue(isInlineMsgFound, "Inline Endpoint test case failed");
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }
}