/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.esb.jms.transport.test;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageConsumer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.client.JMSQueueMessageProducer;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.JMSBrokerController;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.io.File;

public class JMSBrokerStartupTestCase extends ESBIntegrationTest {
    //    private EnvironmentBuilder builder = null;
    private JMSBrokerController activeMqBroker;
    private ServerConfigurationManager serverManager = null;

    private final String ACTIVEMQ_CORE = "activemq-core-5.2.0.jar";
    private final String GERONIMO_J2EE_MANAGEMENT = "geronimo-j2ee-management_1.1_spec-1.0.1.jar";
    private final String GERONIMO_JMS = "geronimo-jms_1.1_spec-1.1.1.jar";

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @BeforeTest(alwaysRun = true)
    public void startJMSBrokerAndConfigureESB() throws Exception {
        context = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(context);

        activeMqBroker = new JMSBrokerController("localhost", getJMSBrokerConfiguration());
        if (!JMSBrokerController.isBrokerStarted()) {
            Assert.assertTrue(activeMqBroker.start(), "JMS Broker(ActiveMQ) stating failed");
        }

        //copping dependency jms jar files to component/lib
        serverManager.copyToComponentLib(new File(TestConfigurationProvider.getResourceLocation() + File.separator + "artifacts" + File.separator + "ESB" + File.separator + "jar" + File.separator + ACTIVEMQ_CORE));

        serverManager.copyToComponentLib(new File(TestConfigurationProvider.getResourceLocation() + File.separator + "artifacts" + File.separator + "ESB"
                + File.separator + "jar" + File.separator + GERONIMO_J2EE_MANAGEMENT));

        serverManager.copyToComponentLib(new File(TestConfigurationProvider.getResourceLocation() + File.separator + "artifacts" + File.separator + "ESB"
                + File.separator + "jar" + File.separator + GERONIMO_JMS));

        //enabling jms transport with ActiveMQ
        serverManager.applyConfiguration(new File(TestConfigurationProvider.getResourceLocation() + File.separator + "artifacts" + File.separator + "ESB"
                + File.separator + "jms" + File.separator + "transport"
                + File.separator + "axis2config" + File.separator
                + "activemq" + File.separator + "axis2.xml"));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @AfterTest(alwaysRun = true)
    public void stopJMSBrokerRevertESBConfiguration() throws Exception {
//        if (builder.getFrameworkSettings().getEnvironmentSettings().is_builderEnabled()) {
        try {
            //reverting the changes done to esb sever
            Thread.sleep(10000); //let server to clear the artifact undeployment
            if (serverManager != null) {
                serverManager.removeFromComponentLib(ACTIVEMQ_CORE);
                serverManager.removeFromComponentLib(GERONIMO_J2EE_MANAGEMENT);
                serverManager.removeFromComponentLib(GERONIMO_JMS);
                serverManager.restoreToLastConfiguration();
            }

        } finally {
            if (activeMqBroker != null) {
                Assert.assertTrue(activeMqBroker.stop(), "JMS Broker(ActiveMQ) Stopping failed");
            }
        }
//        }
    }

    @Test(groups = {"wso2.esb"}, description = "Test JMS broker queue clients with popMessage(java.lang.Class<T> clzz)")
    public void JMSBrokerQueueTest1() throws Exception {
        int numberOfMsgToExpect = 10;
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String queueName = "JmsBrokerTestQueue1";
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
            sender.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                if (consumer.popMessage(javax.jms.Message.class) == null) {
                    Assert.fail("Unable to pop the expected number of message in the queue" + queueName);
                }
            }
        } finally {
            consumer.disconnect();
        }
    }

    @Test(groups = {"wso2.esb"}, description = "Test JMS broker queue clients with popMessage()")
    public void JMSBrokerQueueTest2() throws Exception {
        int numberOfMsgToExpect = 10;
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String queueName = "JmsBrokerTestQueue2";
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
            sender.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                if (consumer.popMessage() == null) {
                    Assert.fail("Unable to pop the expected number of message in the queue" + queueName);
                }
            }
        } finally {
            consumer.disconnect();
        }
    }

    @Test(groups = {"wso2.esb"}, description = "Test JMS broker queue clients with popRawMessage()")
    public void JMSBrokerQueueTest3() throws Exception {
        int numberOfMsgToExpect = 10;
        JMSQueueMessageProducer sender = new JMSQueueMessageProducer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        String queueName = "JmsBrokerTestQueue3";
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
            sender.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                sender.pushMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        JMSQueueMessageConsumer consumer = new JMSQueueMessageConsumer(JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration());
        try {
            consumer.connect(queueName);
            for (int i = 0; i < numberOfMsgToExpect; i++) {
                if (consumer.popRawMessage() == null) {
                    Assert.fail("Unable to pop the expected number of message in the queue" + queueName);
                }
            }
        } finally {
            consumer.disconnect();
        }
    }


    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        return JMSBrokerConfigurationProvider.getInstance().getBrokerConfiguration();
    }
}

