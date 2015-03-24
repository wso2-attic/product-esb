package org.wso2.carbon.esb.message.processor.test.sampling;

import java.rmi.RemoteException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;
import org.wso2.esb.integration.common.utils.servers.WireMonitorServer;

public class SamplingMessageProcessorWithJMSQueueTestCase extends
		ESBIntegrationTest {
	@BeforeClass(alwaysRun = true)
	public void deployeService() throws Exception {
		super.init();
		/*
		 * Deploying the artifacts here.
		 */
		loadESBConfigurationFromClasspath("/artifacts/ESB/messageProcessorConfig/SamplingProcessorJMSStore_synapse_config.xml");
	}

	@Test(groups = { "wso2.esb" }, description = "Test Message Sampling Processor With placeOrder request.")
	public void testPlaceOrderWithSamplingProcessor()
			throws InterruptedException, RemoteException {
		// invoking the service through the proxy service
		final String proxyUrl = getProxyServiceURLHttp("SamplingProxy");
		final String symbol = "Accepted order";
		final String qty = "9401";
		final String price = "165.85724455886088";

		AxisServiceClient client = new AxisServiceClient();

		WireMonitorServer wireServer = new WireMonitorServer(8991);

		wireServer.start();

		// send the message here
		OMElement placeOrderRequest = createPlaceOrderRequest(symbol, qty,
				price);
		client.sendRobust(placeOrderRequest, proxyUrl, "placeOrder");

		Thread.sleep(5000);

		String response = wireServer.getCapturedMessage();

		Assert.assertEquals(response, placeOrderRequest.getText(),
				"Stock Order is NOT placed properly.");

	}

	private static OMElement createPlaceOrderRequest(String symbol, String qty,
			String purchPrice) {
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace ns = factory.createOMNamespace("http://services.samples",
				"m0");
		OMElement placeOrder = factory.createOMElement("placeOrder", ns);
		OMElement order = factory.createOMElement("order", ns);
		OMElement price = factory.createOMElement("price", ns);
		OMElement quantity = factory.createOMElement("quantity", ns);
		OMElement symb = factory.createOMElement("symbol", ns);
		price.setText(purchPrice);
		quantity.setText(qty);
		symb.setText(symbol);
		order.addChild(price);
		order.addChild(quantity);
		order.addChild(symb);
		placeOrder.addChild(order);
		return placeOrder;
	}

	@AfterClass(alwaysRun = true)
	public void UndeployeService() throws Exception {
		super.cleanup();
	}

}
