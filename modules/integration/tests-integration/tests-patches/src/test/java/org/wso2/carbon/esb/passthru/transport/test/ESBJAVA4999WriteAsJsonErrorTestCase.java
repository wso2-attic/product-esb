package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * This test verifies that 201 response with empty body from backend receieves to the client without any build errors
 * inside ESB.
 */
public class ESBJAVA4999WriteAsJsonErrorTestCase extends ESBIntegrationTest {

    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/passthru/transport/ESBJAVA4999/synapseconfig.xml");
    }

    @Test(groups = "wso2.esb", description = " Checking 201 response with empty body from backend")
    public void testNoEntityBodyPropertyTestCase() throws Exception {
        String payload = "{\"index\": 0,\"guid\": \"d3140cb1-0e33-46e9-a7a6-e8b66c08649f\",\"isActive\": false}";
        HttpResponse response = httpClient.doPost("http://localhost:8480/services/JsonTestProxy",
                null, payload, "application/json");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int statusCode = response.getStatusLine().getStatusCode();
        Assert.assertEquals(statusCode, 201, "Status code 201 not recieved");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
