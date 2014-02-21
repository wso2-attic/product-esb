/*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.mediator.log.test;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.esb.integration.ESBIntegrationTest;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.rmi.RemoteException;

public class LogMediatorHeaderLevelTest extends ESBIntegrationTest {

    private StockQuoteClient axis2Client;

    @Override
    public void init() {
        super.init();
        axis2Client = new StockQuoteClient();
    }

    @Override
    public void successfulScenario() throws RemoteException {
        updateESBConfiguration("/header/synapse.xml");
        launchStockQuoteService();

        axis2Client.setHeader("TestHeader", "http://test.wso2.org", "TestHeaderValue");
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                null, "WSO2");
        assertTrue(response.toString().contains("WSO2"));

        LogViewerStub logViewerStub = new LogViewerStub(getAdminServiceURL("LogViewer"));
        authenticate(logViewerStub);

        LogMessage[] logs = logViewerStub.getLogs("INFO", "LogMediator");
        assertTrue(logs != null && logs.length > 0 && logs[0] != null);

        boolean requestLogOk = false;
        boolean responseLogOk = false;
        for (LogMessage l : logs) {
            String message = l.getLogMessage();
            if (message.contains("inComing = ***Incoming Message***") &&
                    message.contains("inExpression = Echo String - urn:getQuote") &&
                    !message.contains("Envelope") && !message.contains("WSO2") &&
                    message.contains("TestHeader") && message.contains("TestHeaderValue")) {
                requestLogOk = true;
            }

            if (message.contains("outgoing = ***Outgoing Message***") &&
                    message.contains("Envelope") && message.contains("WSO2 Company")) {
                responseLogOk = true;
            }
        }

        assertTrue(requestLogOk);
        assertTrue(responseLogOk);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }
}
