/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.json;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.stratos.tenant.mgt.TenantMgtAdminServiceClient;
import org.wso2.carbon.automation.core.utils.HttpRequestUtil;

import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.servers.WireMonitorServer;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class ESBJAVA4290JSONPayloadNotFormattedProperlyTenantMode extends ESBIntegrationTest {

    public WireMonitorServer wireServer;

    private final String configLocation = "artifacts" + File.separator + "ESB" + File.separator + "synapseconfig" + File
            .separator + "rest" + File.separator + "ESBJAVA4290" + File.separator + "ESBJAVA4290SynapseConfig.xml";

    @BeforeClass(alwaysRun = true)
    public void deployService() throws Exception {
        super.init(5);
        wireServer = new WireMonitorServer(9002);
        wireServer.start();

        TenantMgtAdminServiceClient tenantMgtAdminServiceClient =
                new TenantMgtAdminServiceClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        tenantMgtAdminServiceClient.addTenant("testtenant.com", "admin", "admin", "demo");

        AuthenticatorClient authClient = new AuthenticatorClient(esbServer.getBackEndUrl());
        String session = authClient.login("admin@testtenant.com", "admin", "localhost");

        esbUtils.loadESBConfigurationFromClasspath(configLocation, esbServer.getBackEndUrl(), session);
    }

    @Test(groups = "wso2.esb", description = "Check whether JSON message formatting works properly in tenant mode")
    public void testJSONFormattingInTenantMode() {
        String JSON_PAYLOAD = "{\n" +
                "    \"emails\": [\n" +
                "        {\n" +
                "            \"value\": \"kevin@wso2.com\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        try {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            HttpRequestUtil.doPost(new URL("http://localhost:8280/t/testtenant.com/context"), JSON_PAYLOAD, headers);
        } catch (Exception e) {
            log.error("Error while sending the request to the endpoint. ", e);
        } finally {
            String response = wireServer.getCapturedMessage();
            assertTrue(!response.contains("{\"emails\":{\"value\":\"kevin@wso2.com\"}}"),
                    "JSON message is not properly formatted in tenant " +
                            "Mode when flowing through Tenant transport senders");
            assertTrue(response.contains(JSON_PAYLOAD),
                    "JSON message is not properly formatted in tenant " +
                            "Mode when flowing through Tenant transport senders");
        }
    }

    @AfterClass(alwaysRun = true)
    public void unDeployService() throws Exception {
        super.cleanup();
    }

}
