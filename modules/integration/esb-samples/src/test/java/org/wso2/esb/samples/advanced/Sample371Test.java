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

package org.wso2.esb.samples.advanced;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class Sample371Test extends ESBIntegrationTestCase {

    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"}, description = "Sample 371: Restricting requests based on policies")
    public void testTimeBasedThrottle() throws Exception {
        loadSampleESBConfiguration(371);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                null, "WSO2");
        assertTrue(response.toString().contains("WSO2"));

        response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                null, "WSO2");
        assertTrue(response.toString().contains("WSO2"));

        response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                null, "WSO2");
        assertTrue(response.toString().contains("WSO2"));

        response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                null, "WSO2");
        assertTrue(response.toString().contains("WSO2"));

        try {
            response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                    null, "WSO2");
            fail("Message didn't get throttled - Response received: " + response);
        } catch (AxisFault axisFault) {
            log.info("AxisFault received as expected", axisFault);
            assertTrue(axisFault.getMessage().contains("**Access Denied**"));
        }
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }
}
