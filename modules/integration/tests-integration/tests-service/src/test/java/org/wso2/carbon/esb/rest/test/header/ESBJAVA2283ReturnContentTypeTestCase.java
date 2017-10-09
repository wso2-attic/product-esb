/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.rest.test.header;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestCaseUtils;

public class ESBJAVA2283ReturnContentTypeTestCase extends ESBIntegrationTest {
    private Log log = LogFactory.getLog(ESBJAVA2283ReturnContentTypeTestCase.class);
    private HttpServer server = null;
    private static final String API_URL = "http://localhost:8280/serviceTest/test";
    private static final int HTTP_OK = 200;
    private static final int PORT = 8089;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTEXT_URL = "/gettest";
    private static final String TEXT_XML = "text/xml";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        String relativePath = File.separator + "artifacts" + File.separator + "ESB" + File.separator + "synapseconfig" +
            File.separator + "esbjava2283" + File.separator + "api.xml";
        ESBTestCaseUtils util = new ESBTestCaseUtils();
        OMElement apiConfig = util.loadResource(relativePath);
        addApi(apiConfig);
    }

    @Test(groups = {"wso2.esb"}, description = "test return content type")
    public void testReturnContentType() throws Exception {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext(CONTEXT_URL, new ContentTypeHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(API_URL);
        HttpResponse response;
        InputStream instream = null;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                instream = entity.getContent();
                log.info("Content-Type of the HTTP response is : " + response.getEntity().getContentType());
                log.info("Status Code of the Http response is : " + response.getStatusLine().getStatusCode());
                assertEquals(response.getFirstHeader(CONTENT_TYPE).getValue(), TEXT_XML, "Expected content type doesn't match");
                assertEquals(response.getStatusLine().getStatusCode(), HTTP_OK, "response code doesn't match");
            }
        } catch (IOException e) {
            log.error("Error Occurred while sending http get request.", e);
        } finally {
            if (instream != null) {
                instream.close();
            }
            server.stop(0);
        }

    }

    private class ContentTypeHandler implements HttpHandler {
        public void handle(HttpExchange exchange)  {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add(CONTENT_TYPE, TEXT_XML);
            String response = "This is the test case for ESBJAVA-2283";
            OutputStream os = null;
            try {
                exchange.sendResponseHeaders(HTTP_OK, response.length());
                os = exchange.getResponseBody();
                os.write(response.getBytes());
            } catch (IOException e) {
                log.error("Error Occurred while writing the response.", e);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        log.error("Error Occurred while closing the ContentTypeHandler output stream.", e);
                    }
                }
            }

        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
