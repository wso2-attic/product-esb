/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 
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

package org.wso2.carbon.mediators.xslt.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.esb.integration.ESBIntegrationTest;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.rmi.RemoteException;

public class DefaultXpathTest extends ESBIntegrationTest {

    private StockQuoteClient axis2Client;

    @Override
    public void init() {
        super.init();
        axis2Client = new StockQuoteClient();
    }

    @Override
    public void successfulScenario() throws RemoteException {
        updateESBConfiguration("/default.xml");
        launchStockQuoteService();

        try {
            OMElement response = axis2Client.sendCustomQuoteRequest(getMainSequenceURL(),
                    null, "WSO2");
            String str = response.toString();
            log.info("Response received: " + str);
            assertTrue(str.contains("CheckPriceResponse"));
            assertTrue(str.contains("WSO2"));
        } catch (AxisFault axisFault) {
            handleError("Error while invoking the ESB endpoint", axisFault);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }
}
