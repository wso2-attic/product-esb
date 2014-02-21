package org.wso2.carbon.esb.jms.transport.test;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.endpoint.EndPointAdminClient;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.client.JMSQueueMessageConsumer;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.JMSEndpointManager;

public class JMSEndpointTestCase extends ESBIntegrationTest {
    private EndPointAdminClient endPointAdminClient;

    @BeforeClass(alwaysRun = true)
    public void deployeService() throws Exception {
        super.init();
        OMElement synapse = esbUtils.loadClasspathResource("/artifacts/ESB/jms/transport/jms_transport.xml");
        updateESBConfiguration(JMSEndpointManager.setConfigurations(synapse));
        endPointAdminClient = new EndPointAdminClient(esbServer.getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
    }


    @Test(groups = {"wso2.esb"}, description = "Test JMS to JMS ")
    public void testJMSProxy() throws Exception {
        Thread.sleep(7000);

        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String message = "<?xml version='1.0' encoding='UTF-8'?>" +
                         "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                         " xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">" +
                         "  <soapenv:Header/>" +
                         "  <soapenv:Body>" +
                         "   <ser:placeOrder>" +
                         "     <ser:order>" +
                         "      <xsd:price>100</xsd:price>" +
                         "      <xsd:quantity>2000</xsd:quantity>" +
                         "      <xsd:symbol>JMSTransport</xsd:symbol>" +
                         "     </ser:order>" +
                         "   </ser:placeOrder>" +
                         "  </soapenv:Body>" +
                         "</soapenv:Envelope>";
        try {
            sender.connect("JmsProxy");
            for (int i = 0; i < 3; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        Thread.sleep(10000);
        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect("SimpleStockQuoteService");
            for (int i = 0; i < 3; i++) {
                if (consumer.popMessage() == null) {
                    Assert.fail("Message not received at SimpleStockQuoteService");
                }
            }
        } finally {
            consumer.disconnect();
        }
    }


    @AfterClass(alwaysRun = true)
    public void UndeployeService() throws Exception {
        super.init();
        endPointAdminClient = null;
        super.cleanup();
    }
}
