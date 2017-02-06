/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.esb.mediator.test.payload.factory;

import org.apache.http.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import static org.testng.Assert.assertTrue;

/**
 * This class to test the output payload from ESB, when the json request payload contains any escape characters.
 * Related public Jira - https://wso2.org/jira/browse/ESBJAVA-4305
 */
public class JsonWithEscapeCharacterTestCase extends ESBIntegrationTest {

    String responsePayload;
    private final String JSON_TYPE = "application/json";

    private final String JSON_PAYLOAD_NEW_LINE = "{\"fvSymbol\":\"SYMBOL\",\"fvChannel\":\"ATP\",\"fvTitle\":\"test1 \\n test2\"}";
    private final String JSON_PAYLOAD_Tab = "{\"fvSymbol\":\"SYMBOL\",\"fvChannel\":\"ATP\",\"fvTitle\":\"test1 \\t test2\"}";
    private final String JSON_PAYLOAD_CARRIAGE_RETURN = "{\"fvSymbol\":\"SYMBOL\",\"fvChannel\":\"ATP\",\"fvTitle\":\"test1 \\r test2\"}";
    private final String JSON_PAYLOAD_BACK_SPACE = "{\"fvSymbol\":\"SYMBOL\",\"fvChannel\":\"ATP\",\"fvTitle\":\"test1 \\b test2\"}";
    private final String JSON_PAYLOAD_FORM_FEED = "{\"fvSymbol\":\"SYMBOL\",\"fvChannel\":\"ATP\",\"fvTitle\":\"test1 \\f test2\"}";

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig()
            throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/mediatorconfig/payload/factory/JsonSample.xml");
    }

    @Test(groups = {
            "wso2.esb" }, description = "Json payload with escape character '/n'")
    public void payloadWithEscapeCharactorNewLine()
            throws Exception {
        postRequestWithJsonPayload(JSON_PAYLOAD_NEW_LINE, JSON_TYPE);
        assertTrue(responsePayload.contains("{ \"fvTitle\": \"test1 \\n test2\" }"),
                "The escape character is not handled properly in the payload");
    }

    @Test(groups = {
            "wso2.esb" }, description = "Json payload with escape character '/t'")
    public void payloadWithEscapeCharactorTab()
            throws Exception {
        postRequestWithJsonPayload(JSON_PAYLOAD_Tab, JSON_TYPE);
        assertTrue(responsePayload.contains("{ \"fvTitle\": \"test1 \\t test2\" }"),
                "The escape character tab is not handled properly in the payload");
    }

    @Test(groups = {
            "wso2.esb" }, description = "Json payload with escape character '/r'")
    public void payloadWithEscapeCharactorCarriageReturn()
            throws Exception {
        postRequestWithJsonPayload(JSON_PAYLOAD_CARRIAGE_RETURN, JSON_TYPE);
        assertTrue(responsePayload.contains("{ \"fvTitle\": \"test1 \\r test2\" }"),
                "The escape character tab is not handled properly in the payload");
    }

    @Test(groups = {
            "wso2.esb" }, description = "Json payload with escape character '/b'")
    public void payloadWithEscapeCharactorBackSpace()
            throws Exception {
        postRequestWithJsonPayload(JSON_PAYLOAD_BACK_SPACE, JSON_TYPE);
        assertTrue(responsePayload.contains("{ \"fvTitle\": \"test1 \\b test2\" }"),
                "The escape character tab is not handled properly in the payload");
    }

    @Test(groups = {
            "wso2.esb" }, description = "Json payload with escape character '/f'")
    public void payloadWithEscapeCharactorFormFeed()
            throws Exception {
        postRequestWithJsonPayload(JSON_PAYLOAD_FORM_FEED, JSON_TYPE);
        assertTrue(responsePayload.contains("{ \"fvTitle\": \"test1 \\f test2\" }"),
                "The escape character tab is not handled properly in the payload");
    }

    private void postRequestWithJsonPayload(String payload, String contentType) throws Exception {
        SimpleHttpClient httpClient = new SimpleHttpClient();
        String url = getApiInvocationURL("tojson");
        HttpResponse httpResponse = httpClient.doPost(url, null, payload, contentType);
        responsePayload = httpClient.getResponsePayload(httpResponse);
    }

   @AfterClass(alwaysRun = true)
   private void destroy() throws Exception {
        super.cleanup();
    }
}