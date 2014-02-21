/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.mediator.send.test;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

public class SendToDefinedEndpointTestCase extends ESBIntegrationTestCase{

    private StockQuoteClient axis2Client;

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
        axis2Client = new StockQuoteClient();
    }

    @Test(groups = "wso2.esb", description = "Test sending request to defined endpoint")
    public void testSendingToDefinedEndpoint() throws IOException {
        updateESBConfiguration("/defined.xml");
        launchStockQuoteService();

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                null, "WSO2");
        assertTrue(response.toString().contains("WSO2"));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception{
        util.logout();
        super.cleanup();
        axis2Client.destroy();
    }

}
