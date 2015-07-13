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

package org.wso2.carbon.esb.mqtt.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.inbound.stub.types.carbon.InboundEndpointDTO;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.clients.inbound.endpoint.InboundAdminClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.JMSEndpointManager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class MQTTInboundTransportTestCase extends ESBIntegrationTest{

    private InboundAdminClient inboundAdminClient;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {

        super.init();
        serverConfigurationManager =
                new ServerConfigurationManager(new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
        inboundAdminClient = new InboundAdminClient(context.getContextUrls().getBackEndUrl(),getSessionCookie());
    }

    @Test(groups = { "wso2.esb" }, enabled = true,description = "Adding New MQTT Inbound End point")
    public void testAddingNewMQTTInboundEndpoint() throws Exception {

        int beforeCount = 0;
        addInboundEndpoint(addEndpoint1());
        int afterCount = inboundAdminClient.getAllInboundEndpointNames().length;
        log.info("afterCount Adding : "+afterCount);
        assertEquals(1, afterCount - beforeCount);
        deleteInboundEndpoints();

    }

    @Test(groups = { "wso2.esb" }, enabled = true,description = "Deleting an MQTT Inbound End point")
    public void testDeletingMQTTInboundEndpoint() throws Exception {
        addInboundEndpoint(addEndpoint1());
        int beforeCount = inboundAdminClient.getAllInboundEndpointNames().length;
        log.info("Before Adding : "+beforeCount);
        deleteInboundEndpointFromName("Test");
        int afterCount = 0;
        log.info("afterCount adding : "+afterCount);
        assertEquals(1, beforeCount - afterCount);
        deleteInboundEndpoints();

    }

    private OMElement addEndpoint1() throws Exception {
        OMElement synapseConfig = null;
        synapseConfig = AXIOMUtil
                .stringToOM("<inboundEndpoint xmlns=\"http://ws.apache.org/ns/synapse\"\n" +
                        "                 name=\"Test\"\n" +
                        "                 sequence=\"Testln\"\n" +
                        "                 onError=\"Testln\"\n" +
                        "                 protocol=\"mqtt\"\n" +
                        "                 suspend=\"false\">\n" +
                        "    <parameters>\n" +
                        "        <parameter name=\"interval\">10000</parameter>\n" +
                        "        <parameter name=\"mqtt.server.port\">1833</parameter>\n" +
                        "        <parameter name=\"coordination\">false</parameter>\n" +
                        "        <parameter name=\"mqtt.connection.factory\">mqttConFactory</parameter>\n" +
                        "        <parameter name=\"mqtt.subscription.username\">elil</parameter>\n" +
                        "        <parameter name=\"mqtt.subscription.qos\">2</parameter>\n" +
                        "        <parameter name=\"mqtt.session.clean\">false</parameter>\n" +
                        "        <parameter name=\"mqtt.temporary.store.directory\">td</parameter>\n" +
                        "        <parameter name=\"mqtt.ssl.enable\">false</parameter>\n" +
                        "        <parameter name=\"sequential\">false</parameter>\n" +
                        "        <parameter name=\"mqtt.topic.name\">esb.test2</parameter>\n" +
                        "        <parameter name=\"mqtt.blocking.sender\">true</parameter>\n" +
                        "        <parameter name=\"mqtt.server.host.name\">localhost</parameter>\n" +
                        "    </parameters>\n" +
                        "</inboundEndpoint>");

        return synapseConfig;
    }

    private OMElement addEndpoint2() throws Exception {
        OMElement synapseConfig = null;
        synapseConfig = AXIOMUtil
                .stringToOM("<inboundEndpoint xmlns=\"http://ws.apache.org/ns/synapse\"\n" +
                        "                 name=\"Test1\"\n" +
                        "                 sequence=\"Testln\"\n" +
                        "                 onError=\"Testln\"\n" +
                        "                 protocol=\"mqtt\"\n" +
                        "                 suspend=\"false\">\n" +
                        "    <parameters>\n" +
                        "        <parameter name=\"interval\">10000</parameter>\n" +
                        "        <parameter name=\"mqtt.server.port\">1833</parameter>\n" +
                        "        <parameter name=\"coordination\">false</parameter>\n" +
                        "        <parameter name=\"mqtt.connection.factory\">mqttConFactory</parameter>\n" +
                        "        <parameter name=\"mqtt.subscription.username\">elil</parameter>\n" +
                        "        <parameter name=\"mqtt.subscription.qos\">2</parameter>\n" +
                        "        <parameter name=\"mqtt.session.clean\">false</parameter>\n" +
                        "        <parameter name=\"mqtt.temporary.store.directory\">td</parameter>\n" +
                        "        <parameter name=\"mqtt.ssl.enable\">false</parameter>\n" +
                        "        <parameter name=\"sequential\">false</parameter>\n" +
                        "        <parameter name=\"mqtt.topic.name\">esb.test2</parameter>\n" +
                        "        <parameter name=\"mqtt.blocking.sender\">true</parameter>\n" +
                        "        <parameter name=\"mqtt.server.host.name\">localhost</parameter>\n" +
                        "    </parameters>\n" +
                        "</inboundEndpoint>");

        return synapseConfig;
    }
    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}