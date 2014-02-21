/*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

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

package org.wso2.carbon.mediator.validate.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.axis2.GetLogs;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ValidateMediatorTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(ValidateMediatorTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    String searchString = "<ax21:symbol>IBM</ax21:symbol>";

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing Validate Mediator")
    public void testValidateMediator() throws Exception {

        updateESBConfiguration("/validate.xml");

        launchStockQuoteService();

        StockQuoteClient stockQuoteClient = new StockQuoteClient();
        OMElement result = null;

        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest("http://" + FrameworkSettings.HOST_NAME +
                    ":" + FrameworkSettings.HTTP_PORT +
                    "/services/" + FrameworkSettings.TENANT_NAME +
                    "/", null, "IBM");
        }
        
        log.info(result);

        assertNotNull(result);
        assertTrue(result.toString().contains("IBM Company"));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }

}
/*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

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

package org.wso2.carbon.mediator.validate.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.axis2.GetLogs;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ValidateMediatorTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(ValidateMediatorTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    String searchString = "<ax21:symbol>IBM</ax21:symbol>";

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing Validate Mediator")
    public void testValidateMediator() throws Exception {

        updateESBConfiguration("/validate.xml");

        launchStockQuoteService();

        StockQuoteClient stockQuoteClient = new StockQuoteClient();
        OMElement result = null;

        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest("http://" + FrameworkSettings.HOST_NAME +
                    ":" + FrameworkSettings.HTTP_PORT +
                    "/services/" + FrameworkSettings.TENANT_NAME +
                    "/", null, "IBM");
        }
        
        log.info(result);

        assertNotNull(result);
        assertTrue(result.toString().contains("IBM Company"));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }

}
