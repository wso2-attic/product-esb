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

package org.wso2.esb.integration.nhttp;

import org.apache.http.*;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.http.RequestInterceptor;
import org.wso2.esb.integration.http.SimpleHttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GZipMediationTestCase extends ESBIntegrationTestCase {

    private TestRequestInterceptor interceptor;

    @Override
    protected void init() throws Exception {
        this.interceptor = new TestRequestInterceptor();
        launchBackendHttpServer(interceptor);
    }

    @Test(groups = {"wso2.esb"}, description = "Tests the ability to mediate GZip payloads")
    public void testGZipMediation() throws IOException {
        SimpleHttpClient httpClient = new SimpleHttpClient();
        loadESBConfigurationFromClasspath("/chunking_default.xml");
        Map<String,String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_ENCODING, "gzip");
        String payload = "<test>this is an excruciatingly long and boring payload</test>";
        HttpResponse response = httpClient.doPost(getMainSequenceURL(), headers, payload, "application/xml");
        assertNotNull(response);
        assertTrue(interceptor.isGzip());
        assertEquals(payload, interceptor.getPayload());
    }

    private static class TestRequestInterceptor implements RequestInterceptor {

        private boolean gzip;
        private String payload;

        public void requestReceived(HttpRequest request) {
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                Header encoding = entity.getContentEncoding();
                if (encoding != null && "gzip".equals(encoding.getValue())) {
                    try {
                        gzip = true;
                        GZIPInputStream in = new GZIPInputStream(entity.getContent());
                        StringBuilder builder = new StringBuilder();
                        int ch;
                        while ((ch = in.read()) != -1) {
                            builder.append((char) ch);    
                        }
                        payload = builder.toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public boolean isGzip() {
            return gzip;
        }

        public String getPayload() {
            return payload;
        }
    }
}
