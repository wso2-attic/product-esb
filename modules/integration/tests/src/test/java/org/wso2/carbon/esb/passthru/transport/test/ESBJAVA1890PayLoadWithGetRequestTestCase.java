package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.http.HttpResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.utils.httpserverutils.SimpleHttpClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import static org.testng.Assert.assertEquals;


/**
 * https://wso2.org/jira/browse/ESBJAVA-1890 ESB-4.6.0 adds a body to GET request
 *
 *
 */
public class ESBJAVA1890PayLoadWithGetRequestTestCase extends ESBIntegrationTest {

    private SimpleHttpClient httpClient;
    String api ;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath(
                "/artifacts/ESB/passthru/transport/getPostApi.xml");
        api =this.getApiInvocationURL("postApi");
        httpClient = new SimpleHttpClient();

    }

    @Test(groups = "wso2.esb", description = "- ESB-4.6.0 adds a body to GET request")
    public void testCustomProxy() throws Exception {

        String payload = "<a>test</>";
        HttpResponse response = httpClient.doPost(api,
                null, payload, "application/xml");
        assertEquals(response.getStatusLine().getStatusCode(), 200);

        HttpResponse response2 = httpClient.doPost(api,
                null, payload, "application/xml");
        assertEquals(response2.getStatusLine().getStatusCode(), 200);

    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
