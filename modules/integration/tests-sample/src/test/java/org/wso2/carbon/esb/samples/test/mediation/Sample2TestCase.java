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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.ESBTestConstant;

import java.rmi.RemoteException;

import static org.testng.AssertJUnit.assertTrue;


public class Sample2TestCase extends ESBIntegrationTest {

    @BeforeClass
    public void uploadSynapseConfig() throws Exception {
        super.init();
        loadSampleESBConfiguration(2);

    }

    @Test(groups = {"wso2.esb"}, description = "Sample 2: CBR with the Switch-case mediator, using message properties")
    public void testSample2() throws RemoteException {
        OMElement response;

        response =
                axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                                                        getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                        "IBM");

        assertTrue("Requested Symbon not found in Response", response.toString().contains("IBM"));

        response =
                axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                                                        getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                        "MSTF");
        assertTrue("Requested Symbon not found in Response", response.toString().contains("MSTF"));

        response =
                axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                                                        getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                        "WSO2");

        assertTrue("Requested Symbon not found in Response", response.toString().contains("WSO2"));
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
