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
package org.wso2.carbon.esb.samples.test.mediation;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.ESBTestConstant;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/*This Class tests ESB Sample4*/
public class Sample4TestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        loadSampleESBConfiguration(4);
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
            assertEquals(expected.getMessage(), ESBTestConstant.INCOMING_MESSAGE_IS_NULL
                    , "Error message not contain message > The input stream for an incoming message is null");
        }

        try {
            response = axis2Client.sendSimpleStockQuoteRequest(
                    getMainSequenceURL(),
                    null,
                    "SUN");
            fail("This query must throw an exception.");
        } catch (AxisFault expected) {
            assertEquals(expected.getMessage(), ESBTestConstant.INCOMING_MESSAGE_IS_NULL
                    , "Error message not contain message > The input stream for an incoming message is null");
        }

        response = axis2Client.sendSimpleStockQuoteRequest(
                getMainSequenceURL(),
                null,
                "IBM");
        assertTrue(response.toString().contains("IBM"));
    }

    @AfterClass(alwaysRun = true)
    private void destroy() throws Exception {
        super.cleanup();
    }

}
