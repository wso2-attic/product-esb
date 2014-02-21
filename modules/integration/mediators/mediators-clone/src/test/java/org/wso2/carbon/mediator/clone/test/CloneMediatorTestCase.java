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

package org.wso2.carbon.mediator.clone.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.core.utils.ArtifactReader;
import org.wso2.carbon.integration.core.utils.StockQuoteClient;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;

public class CloneMediatorTestCase {

    private static final Log log = LogFactory.getLog(CloneMediatorTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    private ConfigServiceAdminStub configServiceAdminStub;

    @BeforeMethod(groups = "wso2.esb")
    public void login() throws Exception{
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();

        AuthenticateStub authenticateStub = new AuthenticateStub();

        configServiceAdminStub = new ConfigServiceAdminStub("https://localhost:9443/services/ConfigServiceAdmin");

        ServiceClient client = configServiceAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);

        authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
    }

    @Test(groups = "wso2.esb")
    public void testCloneMediator() throws Exception {

        OMElement result = null;
        ArtifactReader artifactReader = new ArtifactReader();
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        //FrameworkSettings.init();

        OMElement omElement = artifactReader.getOMElement(CloneMediatorTest.class.getResource("/clone.xml").getPath());
        configServiceAdminStub.updateConfiguration(omElement);

        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = stockQuoteClient.stockQuoteClientforProxy("http://localhost:9763", null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" +
                    FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/", null, "IBM");
        }
        log.info(result);
        Thread.sleep(2000);
        assert result != null;
        boolean isFound = result.getChildren().next().toString().contains("IBM");
        if (!isFound) {
            log.error("Response didn't match");
        }
    }

}
