/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.dynamicssl.profiles.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.automation.test.utils.generic.MutualSSLClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.JMXClient;

import javax.management.MalformedObjectNameException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Integration TestCase for Dynamic SSL Profiles re-loading using Mutual SSL Client connection
 */
public class DynamicSSLProfilesPttListenerTestCase extends ESBIntegrationTest {

    private ServerConfigurationManager serverManager = null;

    private String trustStoreName = "client-truststore.jks";
    private String keyStoreName = "wso2carbon.jks";
    private String keyStorePassword = "wso2carbon";
    private String trustStorePassword = "wso2carbon";
    private String proxyService = "TestProxy";
    private String serviceName = "org.apache.synapse:Type=ListenerSSLProfileReloader,Name=PassThroughHttpMultiSSLListener";
    private String configLocation = FrameworkPathUtil.getSystemResourceLocation() + File.separator + "artifacts"
                                    + File.separator + "ESB" + File.separator + "dynamicsslprofiles"
                                    + File.separator + "pttlistener" + File.separator;


    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();

        serverManager = new ServerConfigurationManager(context);
        serverManager.applyConfigurationWithoutRestart(new File(configLocation + "axis2.xml"));
        serverManager.restartGracefully();

        super.init();

    }

    /**
     * add echo proxy , use https endpoint for connection
     *
     * @throws Exception
     */

    private void addTestProxy() throws Exception {

        addProxyService(AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                             "<proxy xmlns=\"http://ws.apache.org/ns/synapse\"\n" +
                                             "       name=\"TestProxy\"\n" +
                                             "       transports=\"https,http\"\n" +
                                             "       statistics=\"disable\"\n" +
                                             "       trace=\"disable\"\n" +
                                             "       startOnLoad=\"true\">\n" +
                                             "   <target>\n" +
                                             "      <outSequence>\n" +
                                             "         <send/>\n" +
                                             "      </outSequence>\n" +
                                             "      <endpoint>\n" +
                                             "         <address uri=\"https://localhost:8243/services/echo\"/>\n" +
                                             "      </endpoint>\n" +
                                             "   </target>\n" +
                                             "   <description/>\n" +
                                             "</proxy>"));
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = {"wso2.esb"}, description = "Create Mutual SSL Connection with TestProxy with dynamically loaded SSL configuration")
    public void testMutualSSLConnectionWithUpdatedProfile() throws Exception {

        String soapMessage = "<soapenv:Envelope " +
                             "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                             "xmlns:echo=\"http://echo.services.core.carbon.wso2.org\">\n" +
                             "   <soapenv:Header/>\n" +
                             "   <soapenv:Body>\n" +
                             "      <echo:echoString>\n" +
                             "         <in>WSO2 Stock</in>\n" +
                             "      </echo:echoString>\n" +
                             "   </soapenv:Body>\n" +
                             "</soapenv:Envelope>";

        addTestProxy();

        //load key store file
        MutualSSLClient.loadKeyStore(configLocation + keyStoreName, keyStorePassword);

        //load trust store file
        MutualSSLClient.loadTrustStore(configLocation + trustStoreName, trustStorePassword);

        //create ssl socket factory instance with given key/trust stores
        MutualSSLClient.initMutualSSLConnection();

        updateProfileConfigurationFiles();

        connectViaJMX();

        Map<String, String> reqProps = new HashMap<String, String>();

        reqProps.put("Content-type", "text/xml; charset=utf-8");
        reqProps.put("SOAPAction", "urn:echoString");

        String response;
        try {
            response = MutualSSLClient.sendPostRequest(getProxyServiceURLHttps(proxyService),
                                                       soapMessage, reqProps);
        } catch (Exception e) {
            log.info("Error sending Post request to proxy service", e);
            response = "";
        }

        rollbackProfileConfigurationFiles();

        assertEquals("Mutual SSL Error because of incorrect key", true, response.contains("WSO2 Stock"));
    }

    /**
     * Connect to the JMX service using JMXClient and provided credentials
     */
    private void connectViaJMX() {
        JMXClient jmxClient = null;

        try {
            jmxClient = new JMXClient(serviceName, "localhost", "11111", "9999", "admin", "admin");
        } catch (MalformedObjectNameException e) {
            log.error("Error creating JMXClient ", e);
        }

        if (jmxClient != null) {
            try {
                jmxClient.connect();
                jmxClient.invoke("notifyFileUpdate", null, null);
                log.info("Successfully invoked JMX service operation");
            } catch (Exception e) {
                log.info("JMX service operation invocation failed ", e);
            }

        } else {
            log.error("JMX service operation invocation failed due to JMXClient instance unavailable ");
        }
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @AfterTest(alwaysRun = true)
    public void destroy() throws Exception {

        deleteProxyService(proxyService);

        if (serverManager != null) {
            serverManager.restoreToLastConfiguration();
        }

        super.cleanup();
    }

    /**
     * Copy content of Dynamic SSL profiles configuration files to the file configured in axis2.xml
     *
     * @throws IOException
     */
    private void updateProfileConfigurationFiles() throws Exception {
        File sourceFile = new File(configLocation + "updatedlistenerprofiles.xml");

        FileManager.deleteFile(configLocation + "listenerprofiles.xml");

        try {
            FileManager.copyFile(sourceFile, (configLocation + "listenerprofiles.xml"));
        } catch (IOException e) {
            log.error("Error updating Dynamic SSL Profiles configuration ", e);
            throw new Exception(e);
        }
    }

    /**
     * Restore Dynamic SSL configuration files to its original content
     *
     * @throws IOException
     */
    private void rollbackProfileConfigurationFiles() throws Exception {
        File sourceFile = new File(configLocation + "restorelistenerprofiles.xml");

        FileManager.deleteFile(configLocation + "listenerprofiles.xml");

        try {
            FileManager.copyFile(sourceFile, (configLocation + "listenerprofiles.xml"));
        } catch (IOException e) {
            log.error("Error restoring Dynamic SSL Profiles configuration ", e);
            throw new Exception(e);
        }
    }
}
