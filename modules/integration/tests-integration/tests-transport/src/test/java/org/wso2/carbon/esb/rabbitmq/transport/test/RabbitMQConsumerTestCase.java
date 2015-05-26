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
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.rabbitmqclient.RabbitMQProducerClient;

public class RabbitMQConsumerTestCase extends ESBIntegrationTest {

    LogViewerClient logViewer;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/rabbitmq/transport/rabbitmq_consumer_proxy.xml");
        logViewer = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
    }

    @Test(groups = {"wso2.esb"}, description = "Test ESB as a RabbitMQ Consumer ")
    public void testRabbitMQConsumer() throws Exception {
        int beforeLogSize = logViewer.getAllRemoteSystemLogs().length;

        RabbitMQProducerClient sender = new RabbitMQProducerClient("localhost", 5672, "guest", "guest");

        try {
            sender.connect("exchange2", "queue2");
            for (int i = 0; i < 200; i++) {
                String message =
                        "<ser:placeOrder xmlns:ser=\"http://services.samples\">\n" +
                                "<ser:order>\n" +
                                "<ser:price>100</ser:price>\n" +
                                "<ser:quantity>2000</ser:quantity>\n" +
                                "<ser:symbol>RMQ</ser:symbol>\n" +
                                "</ser:order>\n" +
                                "</ser:placeOrder>";
                sender.sendBasicMessage(message);
            }
        } finally {
            sender.disconnect();
        }

        Thread.sleep(20000);

        LogEvent[] logs = logViewer.getAllRemoteSystemLogs();
        int afterLogSize = logs.length;
        int count = 0;

        for (int i = (afterLogSize - beforeLogSize - 1); i >= 0; i--) {
            String message = logs[i].getMessage();
            if (message.contains("received = true")) {
                count++;
            }
        }

        Assert.assertEquals(200, count);
    }

    @AfterClass(alwaysRun = true)
    public void end() throws Exception {
        super.init();
        super.cleanup();
        logViewer = null;
    }
}
