/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.wso2.carbon.esb.mediators.callout;

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

import static org.testng.Assert.assertEquals;

public class CARBON_16103_204NoContentTest extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath(
                "artifacts" + File.separator + "ESB" + File.separator + "mediatorconfig" +
                        File.separator + "callout" + File.separator + "CallOutMediatorEmpty204noContentConfig.xml");
    }

    @Test
    public void testCalloutMediatorWhenReceiving204NoContent() throws Exception {

        String requestPayload = "{\"hello\":\"world\"}";

        String url = getApiInvocationURL("callouttest");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/json;charset=UTF-8");
        HttpResponse httpResponse = HttpRequestUtil.doPost(new URL(url), requestPayload, headers);

        assertEquals(httpResponse.getResponseCode(), 204, "correct status code not received.");
        assertEquals(httpResponse.getData() , "", "correct response body not received.");
    }

    @AfterClass(alwaysRun = true)
    private void clean() throws Exception {
        super.cleanup();
    }

}
