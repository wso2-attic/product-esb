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

package org.wso2.carbon.esb.proxyservice.test.proxyservices;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.apache.http.HttpResponse;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.testng.Assert.assertEquals;

/**
 * This test case tests the behaviour of pinnedservers property in proxy. When pinnedserver is set in a proxy it should only
 * be deployed if the SynapseConfig.ServerName defined in axis2.xml is same
 * eg in axis2.xml we have this property <parameter name="SynapseConfig.ServerName" locked="false">localhost</parameter>
 */
public class ESBJAVA4201 extends ESBIntegrationTest {

    Map<String, String> headers = null;
    String payload;
    SimpleHttpClient simpleHttpClient = null;
    String testResponse;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();

        loadESBConfigurationFromClasspath("artifacts/ESB/synapseconfig/patchAutomation/loopBackProxy.xml");


        headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/xml");

        payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <testBody>\n" +
                "      <foo/>\n" +
                "      </testBody>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";

        testResponse = "<testResponse xmlns=\"http://ws.apache.org/ns/synapse\"><foo/></testResponse>";
        simpleHttpClient = new SimpleHttpClient();

    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {
        super.cleanup();
        headers = null;
        simpleHttpClient = null;
    }


    @Test(groups = {"wso2.esb"}, description = "Testing localhostPinnedServerProxy, this should deployed as " +
            "pinnedservers value is same in both proxy and in SynapseConfig.ServerName in axis2.xml", priority = 1)
    public void testDeployedProxy() throws IOException {
        String proxyServiceURL = getProxyServiceURLHttp("localhostPinnedServerProxy");
        HttpResponse response = simpleHttpClient.doPost(proxyServiceURL, headers, payload, "application/xml");
        String responsePayload = simpleHttpClient.getResponsePayload(response);
        assertEquals(responsePayload, testResponse); //As this is a echo proxy, it should return the same payload as response
    }

    @Test(groups = {"wso2.esb"}, description = "Testing loopbackProxy, this should deployed as there are no" +
            " pinnedservers configured", priority = 2)
    public void testProxyWithoutPinnedServer() throws IOException {
        String proxyServiceURL = getProxyServiceURLHttp("loopBackProxy");
        HttpResponse response = simpleHttpClient.doPost(proxyServiceURL, headers, payload, "application/xml");
        String responsePayload = simpleHttpClient.getResponsePayload(response);
        assertEquals(responsePayload, testResponse); //As this is a echo proxy, it should return the same payload as response
    }

    @Test(groups = {"wso2.esb"}, description = "Testing host2PinnedServerProxy, this should not deployed as" +
            " pinnedservers value is different from the SynapseConfig.ServerName in axis2.xml", priority = 3)
    public void testNonDeployedProxy() throws Exception {

        loadESBConfigurationFromClasspath("artifacts/ESB/synapseconfig/patchAutomation/failureProxy.xml");
        String proxyServiceURL = getProxyServiceURLHttp("host2PinnedServerProxy");
        HttpResponse response = simpleHttpClient.doPost(proxyServiceURL, headers, payload, "application/xml");
        String responsePayload = simpleHttpClient.getResponsePayload(response);
        assertEquals(responsePayload, ""); //As this is a echo proxy, it should return the same payload as response
    }


}