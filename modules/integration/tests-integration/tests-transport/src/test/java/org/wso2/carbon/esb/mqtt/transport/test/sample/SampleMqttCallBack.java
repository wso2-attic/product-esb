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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SampleMqttCallBack implements MqttCallback {
    public static String message;

    private static final Log log = LogFactory.getLog(SampleMqttCallBack.class);

    public void connectionLost(Throwable throwable) {
        log.info("Connection lost...");
    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        log.info("Message arrived...");
        log.info("Topic : " + s);
        log.info("Message : " + mqttMessage.toString());
        this.message=mqttMessage.toString();
        boolean isMessage=message.contains("Wso2");
        assert (isMessage);
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("Delivery complete....");
        log.info("Delivery Token : " + iMqttDeliveryToken.toString());
    }
}
