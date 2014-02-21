package org.wso2.carbon.esb.jms.transport.test;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.JMSEndpointManager;
import org.wso2.carbon.esb.util.Utils;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;


public class ESBJAVA1910TestCase extends ESBIntegrationTest {
    private LogViewerClient logViewerClient = null;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        OMElement synapse = esbUtils.loadClasspathResource("/artifacts/ESB/jms/transport/HTTP_SC.xml");
        updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));
        logViewerClient = new LogViewerClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());

    }

    @Test(groups = {"wso2.esb"}, description = "Test Property get-property('HTTP_SC') in message.processor.reply.sequence")
    public void testStatusCode() throws Exception {
        int beforeLogCount = logViewerClient.getAllSystemLogs().length;
        AxisServiceClient client = new AxisServiceClient();
        Thread.sleep(3000); //force wait until message processor executes
        client.sendRobust(Utils.getStockQuoteRequest("WSO2"), getProxyServiceURL("MessageStoreProxy"), "getQuote");
        Thread.sleep(5000);
        LogEvent[] logs = logViewerClient.getAllSystemLogs();
        boolean status = false;
        for (int i = 0; i < (logs.length - beforeLogCount); i++) {
            if (logs[i].getMessage().contains("status code---------- = 200,")) {
                status = true;
                break;
            }
        }

        Assert.assertTrue(status, "Status Code not found in the logs");

    }

    @AfterClass(alwaysRun = true)
    protected void cleanup() throws Exception {
        super.cleanup();

    }

}
