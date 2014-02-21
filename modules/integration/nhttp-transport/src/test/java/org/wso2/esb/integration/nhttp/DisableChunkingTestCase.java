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

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.http.RequestInterceptor;
import org.wso2.esb.integration.http.SimpleHttpClient;

import java.io.IOException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DisableChunkingTestCase extends ESBIntegrationTestCase {

    private TestRequestInterceptor interceptor;

    @Override
    protected void init() throws Exception {
        this.interceptor = new TestRequestInterceptor();
        launchBackendHttpServer(interceptor);
    }

    @Test(groups = {"wso2.esb"}, description = "Tests the ability to control chunking behavior of the ESB")
    public void testChunking() throws IOException {
        SimpleHttpClient httpClient = new SimpleHttpClient();
        loadESBConfigurationFromClasspath("/chunking_default.xml");
        HttpResponse response = httpClient.doPost(getMainSequenceURL(), null, "<test/>", "application/xml");
        assertNotNull(response);
        assertTrue(interceptor.isChunkModeEnabled());

        loadESBConfigurationFromClasspath("/chunking_disabled.xml");
        response = httpClient.doPost(getMainSequenceURL(), null, "<test/>", "application/xml");
        assertNotNull(response);
        assertTrue(!interceptor.isChunkModeEnabled());
    }

    private static class TestRequestInterceptor implements RequestInterceptor {

        private boolean chunkModeEnabled;

        public void requestReceived(HttpRequest request) {
            Header header = request.getFirstHeader(HttpHeaders.TRANSFER_ENCODING);
            chunkModeEnabled = header != null && "chunked".equals(header.getValue());
        }

        public boolean isChunkModeEnabled() {
            return chunkModeEnabled;
        }
    }

}
