/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.esb.passthru.transport.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;

/**
 * This test case is used to verify that the 200 OK response messages does not fail when it has a
 * Location header with a relative url path (e.g. /services/login.do)
 */

public class ESBJAVA3461LocationHeaderInResponseTestCase extends ESBIntegrationTest {

    private final String LOCATION_HEADER_NAME = "Location";

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB"
                                          + File.separator + "passthru" + File.separator + "transport"
                                          + File.separator + "ESBJAVA3461"
                                          + File.separator + "ESBJAVA3461LocationHeaderInResponseTestSynapse.xml");
    }

    @Test(groups = "wso2.esb", description = "Test to check whether there is a location header in the http response for 200 OK responses")
    public void testForLocationHeaderInResponse() throws Exception {

        String proxyServiceUrl = getProxyServiceURLHttp("mockServiceTestProxy");

        String requestPayload = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' >"
                                + "<soapenv:Body xmlns:ser='http://services.samples' xmlns:xsd='http://services.samples/xsd'> "
                                + "<ser:getQuote> <ser:request> <xsd:symbol>WSO2</xsd:symbol> </ser:request> </ser:getQuote> "
                                + "</soapenv:Body></soapenv:Envelope>";

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("SOAPAction", "urn:getQuote");

        HttpResponse response = HttpRequestUtil.doPost(new URL(proxyServiceUrl), requestPayload, headers);
        Map<String, String> responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        String locationHeaderValue = responseHeaders.get(LOCATION_HEADER_NAME);
        assertNotNull(locationHeaderValue);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }


}
