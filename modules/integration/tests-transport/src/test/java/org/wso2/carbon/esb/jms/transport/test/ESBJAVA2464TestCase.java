package org.wso2.carbon.esb.jms.transport.test;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.JMSBrokerController;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

public class ESBJAVA2464TestCase extends ESBIntegrationTest {

	private static final String logLine0 =
	                                       "org.wso2.carbon.proxyadmin.service.ProxyServiceAdmin is not an admin service. Service name ";

	private ServerConfigurationManager configurationManager;
	private LogViewerClient logViewer;

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		super.init();

		configurationManager = new ServerConfigurationManager(esbServer.getBackEndUrl());

		configurationManager.applyConfiguration(new File(
		                                                 getClass().getResource(File.separator +
		                                                                                "artifacts" +
		                                                                                File.separator +
		                                                                                "ESB" +
		                                                                                File.separator +
		                                                                                "synapseconfig" +
		                                                                                File.separator +
		                                                                                "nonBlockingHTTP" +
		                                                                                File.separator +
		                                                                                "axis2.xml")
		                                                           .getPath()));

		super.init(); // After restarting, this will establish the sessions.
		logViewer = new LogViewerClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
		uploadSynapseConfig();
	}

	private void uploadSynapseConfig() throws Exception {
		loadESBConfigurationFromClasspath("artifacts" + File.separator + "ESB" + File.separator +
		                                  "synapseconfig" + File.separator + "nonBlockingHTTP" +
		                                  File.separator + "local_jms_proxy_synapse.xml");
	}
	
	@Test(groups = { "wso2.esb" }, description = "Test ESBJAVA2464 proxy service with jms and nonBlockingLocal transport")
	public void testMessageInjection() throws Exception {
		Thread.sleep(7000);

		JMSQueueMessageProducer sender =
		                                 new JMSQueueMessageProducer(
		                                                             JMSBrokerConfigurationProvider.getInstance()
		                                                                                           .getBrokerConfiguration());
		String message =
		                 "<?xml version='1.0' encoding='UTF-8'?>"
		                         + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:echo=\"http://echo.services.core.carbon.wso2.org\">"
		                         + "  <soapenv:Header/>" + "  <soapenv:Body>"
		                         + "     <echo:echoInt>" + "        <!--Optional:-->"
		                         + "       <in>1</in>" + "     </echo:echoInt>"
		                         + "  </soapenv:Body>" + "</soapenv:Envelope>";
		try {
			sender.connect("echoProxy");
			for (int i = 0; i < 3; i++) {
				sender.pushMessage(message);
			}
		} finally {
			sender.disconnect();
		}

		LogEvent[] logs = logViewer.getAllSystemLogs();
		for (LogEvent log : logs) {
			if (log.getMessage().contains(logLine0)) {
				Assert.fail(logLine0 + "is in log");
			}
		}
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {

		// Restore the axis2 configuration altered by this test case
		super.cleanup();
		configurationManager.restoreToLastConfiguration();
	}

}
