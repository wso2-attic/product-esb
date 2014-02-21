/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.esb.samples.advanced;

import org.apache.http.HttpResponse;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.http.SimpleHttpClient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Sample440Test extends ESBIntegrationTestCase {

    private SimpleHttpClient httpClient;

    public void init() throws Exception {
        httpClient = new SimpleHttpClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"}, description = "Sample 440: Exposing a SOAP Service Over JSON")
    public void testJSONMediation() throws Exception {
        loadSampleESBConfiguration(440);

        String payload = "{\"getQuote\":{\"request\":{\"symbol\":\"WSO2\"}}}";
        HttpResponse response = httpClient.doPost(getProxyServiceURL("JSONProxy", false),
                null, payload, "application/json");
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        String responsePayload = httpClient.getResponsePayload(response);
        log.info("Response received: " + responsePayload);
        assertTrue(responsePayload.startsWith("{\"getQuoteResponse\":{\"return\":{"));
        assertTrue(responsePayload.contains("WSO2"));
    }
}
