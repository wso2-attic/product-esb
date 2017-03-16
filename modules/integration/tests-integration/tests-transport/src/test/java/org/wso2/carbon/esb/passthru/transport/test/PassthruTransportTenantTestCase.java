/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.automation.test.utils.http.client.HttpURLConnectionClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;
import java.io.IOException;

/**
 * This class will test the scenario: in the tenant space, starting passthru calls. ex: WSDLs should be accessible.
 * https://wso2.org/jira/browse/ESBJAVA-5046
 */

public class PassthruTransportTenantTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init(TestUserMode.TENANT_ADMIN);
        String artifactPath = "artifacts" + File.separator + "ESB" + File.separator + "passthru" + File.separator
                + "transport" + File.separator + "httpproxy" + File.separator + "httpProxy.xml";
        loadESBConfigurationFromClasspath(artifactPath);
    }

    @Test(groups = "wso2.esb", description = "Passthru  test case for tenant" )
    public void passthruProxyWSDLTest() throws AxisFault {
        try {
            HttpResponse responsee = HttpURLConnectionClient.
                    sendGetRequest("http://localhost:8480/services/t/wso2.com/HttpProxyTest?wsdl", null);
            Assert.assertNotNull(responsee, "WSDL is accessible");
        } catch (IOException e) {
            Assert.fail("WSDL is not found", e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
