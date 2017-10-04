package org.wso2.carbon.esb.mediator.test.callOut;

import org.apache.http.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.File;

import static org.testng.Assert.assertEquals;

public class EmptySuccessResponseTestCase extends ESBIntegrationTest {
    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath(
                "artifacts" + File.separator + "ESB" + File.separator + "mediatorconfig" + File.separator + "callout"
                        + File.separator + "CallOutMediatorEmpty200okConfig.xml");
    }

    @Test
    public void testCalloutMediatorWhenReceivingEmpty200ok() throws Exception {

        String requestPayload = "{\"hello\":\"world\"}";

        SimpleHttpClient httpClient = new SimpleHttpClient();
        String url = getMainSequenceURL() + "callouttest";
        HttpResponse httpResponse = httpClient.doPost(url, null, requestPayload, "application/json");

        String responsePayload = httpClient.getResponsePayload(httpResponse);

        /**
         * Even though the mock api returns an empty body, the initial payload will be return from the callout test
         * api.
         */
        assertEquals(responsePayload, requestPayload, "matching request and response");
    }

    @AfterClass(alwaysRun = true)
    private void clean() throws Exception {
        super.cleanup();
    }
}
