/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.esb.integration.nhttp;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.*;

public class ReturnServerIPTestCase extends ESBIntegrationTestCase {

    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }


    @Test(groups = {"wso2.esb"}, description = "Test Return Server IP Address")
    public void testReturnServerIP() throws Exception {
        loadESBConfigurationFromClasspath("/serverIP.xml");
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("StockQuoteProxy", false),
                null, "WSO2");

        log.info("Response : " + response.toString());
        assertEquals("serverIp", response.getLocalName());
        assertNotNull(response.getText());
        assertNotEquals("", response.getText());
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }
}
