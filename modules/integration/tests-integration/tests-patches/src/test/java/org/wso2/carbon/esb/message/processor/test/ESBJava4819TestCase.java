package org.wso2.carbon.esb.message.processor.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.http.client.HttpURLConnectionClient;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import static java.io.File.separator;

public class ESBJava4819TestCase extends ESBIntegrationTest {
    LogViewerClient logViewer = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        loadESBConfigurationFromClasspath("artifacts" + separator + "ESB" + separator
                + "synapseconfig" + separator + "processor" + separator + "forwarding" + separator + "ESBJAVA4819Config.xml");

    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.esb")
    public void testForwardingWithInMemoryStore() throws Exception {

        Reader data = new StringReader("<request><element>Test</element></request>");
        Writer writer = new StringWriter();

        String response = HttpURLConnectionClient.sendPostRequestAndReadResponse(data,
                new URL(getProxyServiceURLHttp("MessageProcessorWSAProxy")), writer, "application/xml");

        logViewer = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
        logViewer.clearLogs();

        boolean success = false;
        long startTime = System.currentTimeMillis();

        while (!success && (System.currentTimeMillis() - startTime) < 30000) {
            LogEvent[] logs = logViewer.getAllSystemLogs();
            for (LogEvent event : logs) {
                String message = event.getMessage();
                if (message.contains("MessageProcessorWSAProxy Request Received")) {
                    success = true;
                    break;
                }
            }
            logViewer = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
            logViewer.clearLogs();
            Thread.sleep(5000);
        }

        Assert.assertTrue(success);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
