/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.rabbitmq.transport.test;

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;
import org.wso2.esb.integration.common.utils.clients.rabbitmqclient.RabbitMQConsumerClient;

import java.util.List;

public class RabbitMQProducerTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/rabbitmq/transport/rabbitmq_endpoint_proxy.xml");
    }

    @Test(groups = {"wso2.esb"}, description = "Test ESB as a RabbitMQ Consumer ")
    public void testRabbitMQProducer() throws Exception {
        AxisServiceClient client = new AxisServiceClient();

        for (int i = 0; i < 5; i++) {
            client.sendRobust(Utils.getStockQuoteRequest("RMQ"), getProxyServiceURLHttp("RabbitMQProducerProxy"), "getQuote");
        }
        Thread.sleep(10000);

        RabbitMQConsumerClient consumer = new RabbitMQConsumerClient("localhost");
        consumer.connect("exchange1", "queue1");

        List<String> messages = consumer.popAllMessages();
        if (messages == null || messages.size() == 0) {
            Assert.fail("Messages not received at RabbitMQ Broker");
        } else {
            for (int i = 0; i < 5; i++) {
                org.testng.Assert.assertNotNull(messages.get(i), "Message not found. message sent by proxy service not reached to the destination Queue");
                org.testng.Assert.assertTrue(messages.get(i).contains("<ns:getQuote xmlns:ns=\"http://services.samples\"><" +
                        "ns:request><ns:symbol>RMQ</ns:symbol></ns:request></ns:getQuote>")
                        , "Message mismatched");
            }
        }
        consumer.disconnect();
    }

    @AfterClass(alwaysRun = true)
    public void end() throws Exception {
        super.init();
        super.cleanup();
    }
}
