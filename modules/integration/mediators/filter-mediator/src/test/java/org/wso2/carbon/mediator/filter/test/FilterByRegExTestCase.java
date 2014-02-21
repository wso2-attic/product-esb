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

package org.wso2.carbon.mediator.filter.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class FilterByRegExTestCase extends ESBIntegrationTestCase {

    private StockQuoteClient axis2Client;

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        axis2Client = new StockQuoteClient();

        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb")
    public void testFilterByRegex() throws IOException {
        updateESBConfiguration("/regex.xml");
        launchStockQuoteService();

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                "http://localhost:9000/services/StockQuote", "WSO2");
        assertTrue(response.toString().contains("WSO2"));
    }

    @Test(groups = "wso2.esb", expectedExceptions = AxisFault.class)
    public void testFilterByRegexFailure() throws AxisFault{
        axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                "http://localhost:9000/services/Test", "WSO2");
    }


}
