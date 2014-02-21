package org.wso2.esb.samples.proxy;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertTrue;

public class Sample151Test extends ESBIntegrationTestCase {

    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }


    @Test(groups = {"wso2.esb"}, description = "Sample 151: Custom sequences and endpoints with proxy services")
    public void testProxyServicesWithCustomEndpointsAndSequences() throws Exception {
        loadSampleESBConfiguration(151);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("StockQuoteProxy1", false),
                null, "IBM");

        assertTrue(response.toString().contains("IBM"));
        log.info("Response from StockQuoteProxy1 : " + response.toString());

        response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("StockQuoteProxy2", false),
                null, "MSFT");

        assertTrue(response.toString().contains("MSFT"));
        log.info("Response from StockQuoteProxy2 : " + response.toString());
    }
}
