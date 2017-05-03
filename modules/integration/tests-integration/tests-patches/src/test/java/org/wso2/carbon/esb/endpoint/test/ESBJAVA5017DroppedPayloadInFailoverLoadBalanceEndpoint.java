/*
*Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ESBJAVA5017DroppedPayloadInFailoverLoadBalanceEndpoint extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true, enabled = false)
    public void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath(
                "artifacts" + File.separator + "ESB" + File.separator + "endpoint" + File.separator + "synapse.xml");
    }

    @Test(groups = "wso2.esb",
          description = "Test sending request to LoadBalancing Endpoint with application/json content type",
          enabled = false)
    public void testHTTPPostRequestJSONLoadBalanceEPScenario() throws Exception {

        String JSON_PAYLOAD = "{\"action\":\"ping\"}";
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getProxyServiceURLHttp("LBProxy"));
        StringEntity postingString = new StringEntity(JSON_PAYLOAD);
        httppost.setEntity(postingString);
        httppost.setHeader(HTTPConstants.CONTENT_TYPE, HTTPConstants.MEDIA_TYPE_APPLICATION_JSON);

        try {
            HttpResponse httpResponse = httpclient.execute(httppost);
            HttpEntity entity = httpResponse.getEntity();
            BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
            String result = "";
            String line;
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            Assert.assertTrue(result.contains("pong"), "Response doesn't contains the desired phrase.");
        } finally {
            httpclient.clearRequestInterceptors();
        }

    }

    @AfterClass(alwaysRun = true, enabled = false)
    public void stop() throws Exception {
        super.cleanup();
    }
}