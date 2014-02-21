package org.wso2.esb.samples.security;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertTrue;

public class Sample100Test extends ESBIntegrationTestCase{

    protected Log log = LogFactory.getLog(this.getClass());

    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SECURE_STOCK_QUOTE_SERVICE);
    }


    @Test(groups = {"wso2.esb"}, description = "Sample 100: Using WS-Security for outgoing messages")
    public void testOutgoingSecurity() throws Exception {
        loadSampleESBConfiguration(100);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                null, "WSO2");

        assertTrue(response.toString().contains("WSO2"));
        log.info("Response : " + response.toString());
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }
}
