/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mqtt.transport.test.sample;

import org.apache.axis2.AxisFault;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class MQTTEndpointTestCase extends ESBIntegrationTest {
    private static String SERVER_URL = "tcp://localhost:1883";
    private static String CLIENT_ID = "elil";
    private static String PAYLOAD="\"Sample Mqtt text.\"";

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
    }

    @Test(groups = {"wso2.esb"},description = "send the message to MQTT Broker")
    public void TopicPublisher() throws AxisFault {
        try {
            MqttClient mqttClient = new MqttClient(SERVER_URL, CLIENT_ID);
            MqttConnectOptions clientOptions = new MqttConnectOptions(); // lets keep this to default..
            mqttClient.setCallback(new SampleMqttCallBack());
            mqttClient.connect(clientOptions); // actual connection happens
            if (mqttClient.isConnected()) {
                log.info("Mqtt client connected successfully...");
                MqttTopic topic = mqttClient.getTopic("esb.test2");
                MqttMessage message = new MqttMessage();
                message.setPayload(PAYLOAD.getBytes());
                message.setRetained(true);
            }
            mqttClient.disconnect();

            MqttClient mqttClient1 = new MqttClient(SERVER_URL,CLIENT_ID);
            MqttConnectOptions cleintOptions = new MqttConnectOptions(); // lets keep this to default..
            mqttClient.setCallback(new SampleMqttCallBack());
            mqttClient.connect(cleintOptions); // actual connection happens
            if (mqttClient1.isConnected()) {
                log.info("Mqtt client connected successfully...");
                MqttTopic topic = mqttClient.getTopic("esb.test2");
                mqttClient1.subscribe(topic.toString());
            }
        }
        catch(Exception e)
        {
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
     super.cleanup();
    }
}
