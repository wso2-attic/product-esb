/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.esb.jms.inbound.transport.test;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.esb.jms.inbound.transport.test.utills.HornetQBrokerConfigurationProvider;
import org.wso2.carbon.esb.jms.inbound.transport.test.utills.HornetQLoader;
import org.wso2.carbon.esb.jms.inbound.transport.test.utills.JMS2TopicMessageProducer;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.clients.inbound.endpoint.InboundAdminClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.JMSEndpointManager;
import org.wso2.esb.integration.common.utils.servers.ActiveMQServer;

/**
 * JMS 2.0 Test
 * class tests consuming message from a shared topic subscription
 */
public class JMSInboundSharedSubscriberTestCase extends ESBIntegrationTest{
	private LogViewerClient logViewerClient = null;
	private ServerConfigurationManager serverConfigurationManager;
	private InboundAdminClient inboundAdminClient;
	private HornetQLoader hornetQLoader = new HornetQLoader();

	@BeforeClass(alwaysRun = true)
	protected void init() throws Exception {
		//hornetQLoader.startJMSBrokerAndConfigureESB();
		super.init();
		serverConfigurationManager =
				new ServerConfigurationManager(new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
		OMElement synapse =
				esbUtils.loadResource("/artifacts/ESB/jms/inbound/transport/jms_transport_proxy_service.xml");
		updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));
		inboundAdminClient = new InboundAdminClient(context.getContextUrls().getBackEndUrl(),getSessionCookie());
		logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
	}

	@Test(groups = { "wso2.esb" }, description = "Consuming messages from a shared topic")
	public void testSharedTopic() throws Exception {
		deleteInboundEndpoints();
		JMS2TopicMessageProducer sender =
				new JMS2TopicMessageProducer(HornetQBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
		int messageCount = 5;

		try {
			sender.connect();
			for (int i = 0; i < messageCount; i++) {
				sender.pushMessage("<?xml version='1.0' encoding='UTF-8'?>" +
				                   "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
				                   " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">" +
				                   "   <soapenv:Header/>" +
				                   "   <soapenv:Body>" +
				                   "      <ser:placeOrder>" +
				                   "         <ser:order>" +
				                   "            <xsd:price>100</xsd:price>" +
				                   "            <xsd:quantity>2000</xsd:quantity>" +
				                   "            <xsd:symbol>WSO2ESB</xsd:symbol>" +
				                   "         </ser:order>" +
				                   "      </ser:placeOrder>" +
				                   "   </soapenv:Body>" +
				                   "</soapenv:Envelope>");
			}
		} finally {
			sender.disconnect();
		}

		int beforeLogCount = logViewerClient.getAllSystemLogs().length;
		addInboundEndpoint(addEndpoint1());
		Thread.sleep(3000);
		LogEvent[] logs = logViewerClient.getAllSystemLogs();
		int count = 0;
		for (int i = 0; i < (logs.length - beforeLogCount); i++) {
			if (logs[i].getMessage().contains("<xsd:symbol>WSO2ESB</xsd:symbol>")) {
				count++;
			}
		}

		Assert.assertTrue(count==messageCount, "Couldn't Consume messages from the topic");
		deleteInboundEndpoints();
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		//hornetQLoader.stopJMSBrokerRevertESBConfiguration();
		super.cleanup();
	}

	private OMElement addEndpoint1() throws Exception {
		OMElement synapseConfig = null;
		synapseConfig = AXIOMUtil
				.stringToOM("<inboundEndpoint xmlns=\"http://ws.apache.org/ns/synapse\"\n" +
				            "                 name=\"TestJMS\"\n" +
				            "                 sequence=\"requestHandlerSeq\"\n" +
				            "                 onError=\"inFault\"\n" +
				            "                 protocol=\"jms\"\n" +
				            "                 suspend=\"false\">\n" +
				            "    <parameters>\n" +
				            "        <parameter name=\"interval\">10000</parameter>\n" +
				            "        <parameter name=\"transport.jms.Destination\">/topic/exampleTopic</parameter>\n" +
				            "        <parameter name=\"transport.jms.CacheLevel\">5</parameter>\n" +
				            "        <parameter name=\"transport.jms" +
				            ".ConnectionFactoryJNDIName\">TopicConnectionFactory</parameter>\n" +
				            "        <parameter name=\"java.naming.factory.initial\">org.jnp.interfaces.NamingContextFactory</parameter>\n" +
				            "        <parameter name=\"java.naming.provider.url\">jnp://localhost:1099</parameter>\n" +
				            "        <parameter name=\"transport.jms.SessionAcknowledgement\">AUTO_ACKNOWLEDGE</parameter>\n" +
				            "        <parameter name=\"transport.jms.SessionTransacted\">false</parameter>\n" +
				            "        <parameter name=\"transport.jms.ConnectionFactoryType\">topic</parameter>\n" +
				            "        <parameter name=\"transport.jms.JMSSpecVersion\">2.0</parameter>\n" +
				            "        <parameter name=\"transport.jms.SharedSubscription\">true</parameter>\n" +
				            "        <parameter name=\"transport.jms.SubscriptionName\">mySubscription</parameter>\n" +
				            "    </parameters>\n" +
				            "</inboundEndpoint>");

		return synapseConfig;
	}

}
