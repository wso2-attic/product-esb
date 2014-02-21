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
package org.wso2.carbon.mediator.tests.fault;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/*This Class tests ESB Sample4*/
public class CustomErrorMessageTestCase extends ESBIntegrationTestCase {
    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        loadSampleESBConfiguration(4);
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"}, description = "Sample 4: Introduction to error handling.")
    public void testErrorHandling() throws AxisFault {
        OMElement response;
        try {
            response = axis2Client.sendSimpleStockQuoteRequest(
                    getMainSequenceURL(),
                    null,
                    "MSFT");
            fail("This query must throw an exception.");
        } catch (AxisFault expected) {
            assertTrue(expected.getMessage().contains("The input stream for an incoming message is null")
                    , "Error message not contain message > The input stream for an incoming message is null");
            log.info("Test passed with symbol MSFT");
        }

        try {
            response = axis2Client.sendSimpleStockQuoteRequest(
                    getMainSequenceURL(),
                    null,
                    "SUN");
            fail("This query must throw an exception.");
        } catch (AxisFault expected) {
            assertTrue(expected.getMessage().contains("The input stream for an incoming message is null")
                    , "Error message not contain message > The input stream for an incoming message is null");
            log.info("Test passed with symbol SUN");
        }

        response = axis2Client.sendSimpleStockQuoteRequest(
                getMainSequenceURL(),
                null,
                "IBM");
        assertTrue(response.toString().contains("IBM"));
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }

}
