package org.wso2.carbon.esb.blocking.sender.test;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;

import static org.testng.Assert.assertTrue;

/**
 * This test case verifies that blocking message sender received 50x responses from backend, it will set ERROR_CODE,
 * ERROR_MESSAGE and ERROR_DETAILS correctly.
 */
public class ESBJAVA4974BlockingSenderSetErroCodesTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        OMElement synapse = esbUtils.loadResource(File.separator + "artifacts" + File.separator + "ESB" +
                File.separator + "blockingSender" + File.separator + "ESBJAVA4974Config.xml");
        updateESBConfiguration(synapse);
    }

    @Test(groups = {"wso2.esb"}, description = "Test 502 Response hits fault sequence with ERROR_CODE, " +
            "ERROR_MESSAGE" + " updated")
    public void testErrorMessagesCodesMessageProcessor() throws Exception {
        String url = getApiInvocationURL("request") + "/store";
        LogViewerClient logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
        logViewerClient.clearLogs();

        SimpleHttpClient httpClient = new SimpleHttpClient();
        httpClient.doGet(url, null);

        Thread.sleep(10000);
        LogEvent[] logs = logViewerClient.getAllRemoteSystemLogs();
        boolean logFound = false;
        for (LogEvent item : logs) {
            if (item.getPriority().equals("INFO")) {
                String message = item.getMessage();
                if (message.contains("message = Transport error: 502 Error: Bad Gateway")) {
                    logFound = true;
                    break;
                }
            }
        }
        assertTrue(logFound, " Message -- message = Transport error: 502 Error: Bad Gateway -- not found in carbon " +
                "log");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
