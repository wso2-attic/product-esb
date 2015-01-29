package org.wso2.carbon.esb.jms.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.clients.inbound.endpoint.InboundAdminClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.JMSEndpointManager;
import org.wso2.esb.integration.common.utils.servers.ActiveMQServer;

/**
 * Test JMS tenant users with inbound endpoints.
 */
public class JMSInboundHttpTenantTestCase extends ESBIntegrationTest {
	private LogViewerClient logViewerClient;
	private ActiveMQServer activeMQServer = new ActiveMQServer();
	InboundAdminClient inboundAdminClient1, inboundAdminClient2;

	@BeforeClass(alwaysRun = true)
	public void init() throws Exception {

		OMElement synapse;
		activeMQServer.startJMSBrokerAndConfigureESB();

		super.init("esbs001","abc","user1");
		inboundAdminClient1 = new InboundAdminClient(context.getContextUrls().getBackEndUrl(),
		                                             sessionCookie);
		synapse = esbUtils.loadResource("/artifacts/ESB/jms/inbound/transport/jms_http_tenant_transport.xml");
		updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));
		logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), sessionCookie);
		super.init("esbs001","wso2","user1");
		inboundAdminClient2 = new InboundAdminClient(context.getContextUrls().getBackEndUrl(),
		                                             sessionCookie);
		synapse = esbUtils.loadResource("/artifacts/ESB/jms/inbound/transport/jms_http_tenant_transport.xml");
		updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));

	}

	@Test(groups = { "wso2.esb" }, description = "Tenants Sending Messages to the Same Backend")
	public void testTenantTestCase() throws Exception {

		boolean msftCheck = false, ibmCheck = false;
		int beforeLogCount1;
		LogEvent[] logs;
		JMSQueueMessageProducer sender =
				new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
		try {
			sender.connect("localq");
			for (int i = 0; i < 3; i++) {
				sender.pushMessage("<?xml version='1.0' encoding='UTF-8'?>" +
				                   "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
				                   "                   xmlns:ns= \"http://services.samples\"\n" +
				                   "                   xmlns:xsd=\"http://services.samples/xsd\">\n" +
				                   "      <soapenv:Header/>\n" +
				                   "       <soapenv:Body>\n" +
				                   "           <ns:getQuote >\n" +
				                   "               <ns:request>\n" +
				                   "                   <ns:symbol>IBM</ns:symbol>\n" +
				                   "               </ns:request>\n" +
				                   "           </ns:getQuote>\n" +
				                   "       </soapenv:Body>\n" +
				                   "</soapenv:Envelope>");
			}
		} finally {
			sender.disconnect();
		}
		try {
			sender.connect("localq_1");
			for (int i = 0; i < 3; i++) {
				sender.pushMessage("<?xml version='1.0' encoding='UTF-8'?>" +
				                   "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
				                   "                   xmlns:ns= \"http://services.samples\"\n" +
				                   "                   xmlns:xsd=\"http://services.samples/xsd\">\n" +
				                   "      <soapenv:Header/>\n" +
				                   "       <soapenv:Body>\n" +
				                   "           <ns:getQuote >\n" +
				                   "               <ns:request>\n" +
				                   "                   <ns:symbol>MSFT</ns:symbol>\n" +
				                   "               </ns:request>\n" +
				                   "           </ns:getQuote>\n" +
				                   "       </soapenv:Body>\n" +
				                   "</soapenv:Envelope>");
			}
		} finally {
			sender.disconnect();
		}
		beforeLogCount1 = logViewerClient.getAllSystemLogs().length;
		inboundAdminClient1.addInboundEndpoint(addEndpoint1().toString());
		Thread.sleep(3000);
		inboundAdminClient2.addInboundEndpoint(addEndpoint2().toString());
		Thread.sleep(3000);
		logs = logViewerClient.getAllSystemLogs();
		for (int i = 0; i < (logs.length - beforeLogCount1); i++) {
			if (logs[i].getMessage().contains("MSFT")) {
				msftCheck = true;
			} else if (logs[i].getMessage().contains("IBM")) {
				ibmCheck = true;
			}
		}
		Assert.assertTrue(msftCheck, "MSFT request message is not received ");
		Assert.assertTrue(ibmCheck, "IBM request message is not received ");
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		super.cleanup();
		Thread.sleep(4000);
		activeMQServer.stopJMSBrokerRevertESBConfiguration();
	}

	private OMElement addEndpoint1() throws Exception {
		OMElement synapseConfig;
		synapseConfig = AXIOMUtil
				.stringToOM("<inboundEndpoint xmlns=\"http://ws.apache.org/ns/synapse\"\n" +
				            "                 name=\"TestJMS\"\n" +
				            "                 sequence=\"requestHandlerSeq\"\n" +
				            "                 onError=\"inFault\"\n" +
				            "                 protocol=\"jms\"\n" +
				            "                 suspend=\"false\">\n" +
				            "    <parameters>\n" +
				            "        <parameter name=\"interval\">10000</parameter>\n" +
				            "        <parameter name=\"transport.jms.Destination\">localq</parameter>\n" +
				            "        <parameter name=\"transport.jms.CacheLevel\">0</parameter>\n" +
				            "        <parameter name=\"transport.jms.ConnectionFactoryJNDIName\">QueueConnectionFactory</parameter>\n" +
				            "        <parameter name=\"java.naming.factory.initial\">org.apache.activemq.jndi.ActiveMQInitialContextFactory</parameter>\n" +
				            "        <parameter name=\"java.naming.provider.url\">tcp://localhost:61616</parameter>\n" +
				            "        <parameter name=\"transport.jms.SessionAcknowledgement\">AUTO_ACKNOWLEDGE</parameter>\n" +
				            "        <parameter name=\"transport.jms.SessionTransacted\">false</parameter>\n" +
				            "        <parameter name=\"transport.jms.ConnectionFactoryType\">queue</parameter>\n" +
				            "    </parameters>\n" +
				            "</inboundEndpoint>");
		return synapseConfig;
	}

	private OMElement addEndpoint2() throws Exception {
		OMElement synapseConfig;
		synapseConfig = AXIOMUtil
				.stringToOM("<inboundEndpoint xmlns=\"http://ws.apache.org/ns/synapse\"\n" +
				            "                 name=\"TestJMS1\"\n" +
				            "                 sequence=\"requestHandlerSeq\"\n" +
				            "                 onError=\"inFault\"\n" +
				            "                 protocol=\"jms\"\n" +
				            "                 suspend=\"false\">\n" +
				            "    <parameters>\n" +
				            "        <parameter name=\"interval\">10000</parameter>\n" +
				            "        <parameter name=\"transport.jms.Destination\">localq_1</parameter>\n" +
				            "        <parameter name=\"transport.jms.CacheLevel\">0</parameter>\n" +
				            "        <parameter name=\"transport.jms.ConnectionFactoryJNDIName\">QueueConnectionFactory</parameter>\n" +
				            "        <parameter name=\"java.naming.factory.initial\">org.apache.activemq.jndi.ActiveMQInitialContextFactory</parameter>\n" +
				            "        <parameter name=\"java.naming.provider.url\">tcp://localhost:61616</parameter>\n" +
				            "        <parameter name=\"transport.jms.SessionAcknowledgement\">AUTO_ACKNOWLEDGE</parameter>\n" +
				            "        <parameter name=\"transport.jms.SessionTransacted\">false</parameter>\n" +
				            "        <parameter name=\"transport.jms.ConnectionFactoryType\">queue</parameter>\n" +
				            "    </parameters>\n" +
				            "</inboundEndpoint>");
		return synapseConfig;
	}
}
