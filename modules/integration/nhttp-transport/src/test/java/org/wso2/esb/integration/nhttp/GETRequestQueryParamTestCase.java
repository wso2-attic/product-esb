/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.esb.integration.nhttp;

import org.apache.http.HttpRequest;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.http.RequestInterceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.testng.Assert.assertEquals;

public class GETRequestQueryParamTestCase extends ESBIntegrationTestCase {

    private TestRequestInterceptor interceptor;

    @Override
    protected void init() throws Exception {
        this.interceptor = new TestRequestInterceptor();
        launchBackendHttpServer(interceptor);
    }

    @Test(groups = {"wso2.esb"}, description = "Tests the query parameters of GET requests.")
    public void testGetRequestQueryParameters() throws IOException {
        loadESBConfigurationFromClasspath("/get_request.xml");

        String path = "/foo/bar";
        String query1 = "?param1=value1";
        String query2 = "?param1=value1&param2=value2&param3=value3";

        makeGET(new URL(getProxyServiceURL("GETRequestProxy", false)));
        assertEquals(path, interceptor.getLastRequestURI());

        makeGET(new URL(getProxyServiceURL("GETRequestProxy", false) + query1));
        assertEquals(path + query1, interceptor.getLastRequestURI());

        makeGET(new URL(getProxyServiceURL("GETRequestProxy", false) + query2));
        assertEquals(path + query2, interceptor.getLastRequestURI());
    }

    private void makeGET(URL url) throws IOException {
        URLConnection conn = url.openConnection ();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        log.info("Response received: " + sb.toString());
    }

    private static class TestRequestInterceptor implements RequestInterceptor {

        private String lastRequestURI;

        public void requestReceived(HttpRequest request) {
            lastRequestURI = request.getRequestLine().getUri();
        }

        public String getLastRequestURI() {
            return lastRequestURI;
        }
    }
}
