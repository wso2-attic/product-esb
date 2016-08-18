/*
*Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.mqtt.inbound.test;

import org.apache.activemq.broker.TransportConnector;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.jmsserver.controller.JMSBrokerController;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;
import org.wso2.esb.integration.common.utils.servers.ActiveMQServer;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class MQTTBrokerStartupTestCase extends ESBIntegrationTest {
    private final String PAHO_CLIENT_LIB = "org.eclipse.paho.client.mqttv3.jar";
    private ActiveMQServer activeMQServer = new ActiveMQServer();
    private JMSBrokerController activeMqBroker;
    private ServerConfigurationManager serverManager = null;

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @BeforeTest(alwaysRun = true)
    public void startJMSBrokerAndConfigureESB() throws Exception {
        super.init();
        context = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
        serverManager = new ServerConfigurationManager(context);

        List<TransportConnector> connectorList = new ArrayList<>();
        connectorList.add(getMQTTConnector());

        activeMqBroker = new JMSBrokerController("localhost", connectorList);
        if (!JMSBrokerController.isBrokerStarted()) {
            Assert.assertTrue(activeMqBroker.start(), "MQTT Broker(ActiveMQ) stating failed");
        }

        serverManager.copyToComponentLib(new File(
                TestConfigurationProvider.getResourceLocation() + File.separator + "artifacts" + File.separator + "ESB"
                        + File.separator + "jar" + File.separator + PAHO_CLIENT_LIB));

        serverManager.applyConfiguration(new File(
                TestConfigurationProvider.getResourceLocation() + File.separator + "artifacts" + File.separator + "ESB"
                        + File.separator + "mqtt" + File.separator + "transport" + File.separator + "axis2config"
                        + File.separator + "axis2.xml"));
    }

    private TransportConnector getMQTTConnector() {
        TransportConnector mqtt = new TransportConnector();
        mqtt.setName("mqtt");

        try {
            mqtt.setUri(new URI("mqtt://localhost:1883"));
        } catch (URISyntaxException var3) {
            log.error("Error while setting MQTT uri:mqtt://localhost:1883");
        }

        return mqtt;
    }

    @AfterTest(alwaysRun = true)
    public void close() throws Exception {
        try {
            //reverting the changes done to esb sever
            Thread.sleep(10000); //let server to clear the artifact undeployment
            if (serverManager != null) {
                serverManager.removeFromComponentLib(PAHO_CLIENT_LIB);
                serverManager.restoreToLastConfiguration();
            }

        } finally {
            if (activeMqBroker != null) {
                assertTrue(activeMqBroker.stop(), "MQTT Broker(ActiveMQ) Stopping failed");
            }
        }
    }
}



