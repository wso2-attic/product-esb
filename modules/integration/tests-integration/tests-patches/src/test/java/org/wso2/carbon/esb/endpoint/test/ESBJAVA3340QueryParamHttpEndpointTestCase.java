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

package org.wso2.carbon.esb.endpoint.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.utils.HttpRequestUtil;
import org.wso2.carbon.automation.core.utils.HttpResponse;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.WireMonitorServer;

import java.io.File;
import java.io.IOException;

public class ESBJAVA3340QueryParamHttpEndpointTestCase extends ESBIntegrationTest {

    public WireMonitorServer wireMonitorServer;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        wireMonitorServer = new WireMonitorServer(6789);
        wireMonitorServer.start();

        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB"
                + File.separator + "synapseconfig" + File.separator + "rest"
                + File.separator + "query_params_api.xml");
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a Message Via REST to test query param works with space character")
    public void testPassParamsToEndpoint() {
        String requestString = "/my?some%20value";
        HttpResponse response = null;
        try {
            response = HttpRequestUtil.sendGetRequest(getApiInvocationURL("test") + requestString, null);
        } catch (IOException e) {
            log.error("Error while sending the request to the endpoint. ", e);
        } finally {
            String reply = wireMonitorServer.getCapturedMessage();
            if (reply.length() > 1) {
                Assert.assertFalse(reply.toString().split("HTTP/1.1")[0].contains("{query.param.type}"), "Parameters are properly mapped");
                Assert.assertTrue(reply.toString().split("HTTP/1.1")[0].contains("some%20value"));

            } else {
                Assert.assertTrue(false);
            }

        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }


}
