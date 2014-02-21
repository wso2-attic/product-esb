package org.wso2.carbon.mediator.script.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.esb.integration.ESBIntegrationTest;

import java.rmi.RemoteException;

public class InlineJavaScriptTest extends ESBIntegrationTest {

    private org.wso2.esb.integration.axis2.StockQuoteClient axis2Client;

    @Override
    public void init() {
        super.init();
        axis2Client = new org.wso2.esb.integration.axis2.StockQuoteClient();
    }

    @Override
    public void successfulScenario() throws RemoteException {
        updateESBConfiguration("/inline.xml");
        launchStockQuoteService();

        try {
            OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                    null, "WSO2");
            assertTrue(response.toString().contains("MSFT"));
        } catch (AxisFault axisFault) {
            handleError("Error while invoking the ESB endpoint", axisFault);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }
}
