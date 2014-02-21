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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.ESBTestConstant;

public class Sample1TestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        loadSampleESBConfiguration(1);

    }

    /**
     * Test for filter mediator  - filter using source and regex
     *
     * @throws Exception
     */
    @Test
    public void filterMediatorWithSourceAndRegexTest() throws Exception {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL()
                , getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");


        Assert.assertTrue(response.toString().contains("GetQuoteResponse"));
        Assert.assertTrue(response.toString().contains("WSO2 Company"));


    }

    @AfterClass
    private void destroy() throws Exception {
        super.cleanup();
    }
}
