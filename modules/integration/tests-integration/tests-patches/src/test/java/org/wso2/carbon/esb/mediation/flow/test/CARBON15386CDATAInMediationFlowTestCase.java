/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mediation.flow.test;

import org.apache.http.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * This test case tests whether CDATA blocks are preserved in the mediation flow
 */
public class CARBON15386CDATAInMediationFlowTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        Path proxyPath = Paths.get("artifacts", "ESB", "synapseconfig", "cdata", "CDATAProxy.xml");
        loadESBConfigurationFromClasspath(proxyPath.toString());
    }

    @Test(groups = {"wso2.esb"}, description = "test whether the CDATA blocks are preserved in the response to the proxy service")
    public void testCDATAPreservation() throws IOException {
        String proxyServiceURL = getProxyServiceURLHttp("CDATAProxy");

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/xml");

        String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                         "   <soapenv:Header/>\n" +
                         "   <soapenv:Body>\n" +
                         "      <propertyValue><![CDATA[&&&&& <<< >>>>>]]></propertyValue>\n" +
                         "   </soapenv:Body>\n" +
                         "</soapenv:Envelope>\n";

        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
        HttpResponse response = simpleHttpClient.doPost(proxyServiceURL, headers, payload, "application/xml");
        String responsePayload = simpleHttpClient.getResponsePayload(response);

        assertTrue(responsePayload.contains("<![CDATA[&&&&& <<< >>>>>]]>"),
                   "CDATA block is not preserved in the mediation flow");
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        super.cleanup();
    }

}
