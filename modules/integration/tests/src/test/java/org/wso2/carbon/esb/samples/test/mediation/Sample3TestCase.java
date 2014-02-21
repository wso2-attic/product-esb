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
package org.wso2.carbon.esb.samples.test.mediation;

import org.apache.axis2.AxisFault;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.ESBTestConstant;

public class Sample3TestCase extends ESBIntegrationTest {

    @BeforeClass
    public void beforeClass() throws Exception {
        init();
        loadSampleESBConfiguration(3);
    }

    @Test(groups = {"wso2.esb"}, enabled = false, description = "Sample 3: Add a static value as an" +
                                                                " inline text entry With get-property expression, " +
                                                                "use that value inside property mediator to be included as a part of a log")
    public void testLocalValueInPropertyMediator() throws AxisFault {
        axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                                                getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE),
                                                "IBM");
        // TODO Assert property version of INFO log for string "0.1"
    }

    @AfterClass
    public void afterClass() {
        axis2Client.destroy();
    }

}
