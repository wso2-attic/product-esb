/**
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.message.store.jdbc.test;

import org.apache.axiom.om.OMElement;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class JDBCMessageStoreProcRESTTestCase extends ESBIntegrationTest{
    private static final String url = "http://localhost:8280/jdbc/store";
    private static final String logLine0 =
            "MESSAGE = ************RESTProxy IN, IN-Content-Type = application/json, IN-Test-Header-Field = TestHeaderValue";
    private static final String logLine1 =
            "MESSAGE = ************SamplingSeq IN, IN-Content-Type = application/json, IN-Test-Header-Field = TestHeaderValue";

    private final SimpleHttpClient httpClient = new SimpleHttpClient();
    private final Map<String, String> headers = new HashMap<String, String>(1);
    private final String payload =  "{\n" +
                                    "  \"email\" : \"jms@yomail.com\",\n" +
                                    "  \"firstName\" : \"Jms\",\n" +
                                    "  \"lastName\" : \"Broker\",\n" +
                                    "  \"id\" : 10\n" +
                                    "}";

    private LogViewerClient logViewer;

    private H2DataBaseManager h2;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        headers.put("Test-Header-Field", "TestHeaderValue");

        OMElement synapse = esbUtils.loadResource("/artifacts/ESB/jdbc/JDBCMessageStoreREST.xml");

        h2 = new H2DataBaseManager("jdbc:h2:repository/database/WSO2CARBON_DB", "wso2carbon", "wso2carbon");

        h2.execute("CREATE TABLE 'jdbc_store_table' (\n" +
                   "'indexId' BIGINT( 20 ) NOT NULL ,\n" +
                   "'msg_id' VARCHAR( 200 ) NOT NULL ,\n" +
                   "'message' BLOB NOT NULL ,\n" +
                   "PRIMARY KEY ( 'indexId' )\n" +
                   ")");

        updateESBConfiguration(synapse);
        Thread.sleep(1000);
        logViewer = new LogViewerClient(contextUrls.getBackEndUrl(),getSessionCookie());
    }

    @Test(groups = {"wso2.esb"}, description = "JDBC Message store support for RESTful services.")
    public void testJMSMessageStoreAndProcessor() throws Exception {
        HttpResponse response = httpClient.doPost(url, headers, payload, "application/json");
        Thread.sleep(10000);
        assertEquals(response.getStatusLine().getStatusCode(), 202);
        LogEvent[] logs = logViewer.getAllSystemLogs();
        int i = 1;
        for (LogEvent log : logs) {
            if (log.getMessage().contains(logLine0)) {
                ++i;
            }
            if (log.getMessage().contains(logLine1)) {
                ++i;
            }
        }
        if (i == 3) {
            Assert.assertTrue(true);
        } else {
            Assert.assertTrue(false);
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        h2.executeUpdate("DROP TABLE jdbc_store_table");
        super.cleanup();
    }
}
