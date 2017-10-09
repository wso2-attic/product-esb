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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.ClientConnectionUtil;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestCaseUtils;

public class ContentTypeTestCase extends ESBIntegrationTest {
    private Log log = LogFactory.getLog(ContentTypeTestCase.class);
    private HttpServer server = null;
    private String contentType;
    private static final String API_URL = "http://localhost:8280/serviceTest/test";
    private static final int HTTP_OK = 200;
    private static final int PORT = 8089;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTEXT_URL = "/gettest";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        String relativePath = File.separator + "artifacts" + File.separator + "ESB" + File.separator + "synapseconfig" +
            File.separator + "esbjava2283" + File.separator + "api.xml";
        ESBTestCaseUtils util = new ESBTestCaseUtils();
        OMElement apiConfig = util.loadResource(relativePath);
        addApi(apiConfig);
    }

    @Test(groups = {"wso2.esb"}, description = "Test different content types", dataProvider = "contentTypeProvider")
    public void testReturnContentType(String dataProviderContentType) throws Exception {
        contentType = dataProviderContentType;
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
                assertEquals(response.getFirstHeader(CONTENT_TYPE).getValue(), contentType, "Expected content type doesn't match");
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
        public void handle(HttpExchange exchange) throws IOException {

            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add(CONTENT_TYPE, contentType);
            String response = "This Content type test case";
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

    @DataProvider(name = "contentTypeProvider")
    public Object[][] getContentTypes() {
        return new Object[][]{
            {"application/xml"},
            {"text/plain"},
            {"application/json"},
            {"text/xml"},
            {"application/x-www-form-urlencoded"},
            {"multipart/form-data"},
            {"text/xml"},
        };
    }

    public boolean waitForPortCloser(int port) throws UnknownHostException {
        long time = System.currentTimeMillis() + 5000;
        boolean isPortAvailable = true;
        while (System.currentTimeMillis() < time) {
            isPortAvailable = ClientConnectionUtil.isPortOpen(port, InetAddress.getLocalHost().getHostName());
            if (!isPortAvailable) {
                return isPortAvailable;
            }
        }
        return isPortAvailable;
    }
}