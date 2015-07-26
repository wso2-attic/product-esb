/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.security;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.generic.MutualSSLClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ESBJAVA3857ClientSSLCertificateTestCase extends ESBIntegrationTest {

    private final String CUSTOM_MEDIATOR_FILE_NAME = "certmediator-1.0.jar";
    private final String CONFIG_LOCATION = FrameworkPathUtil.getSystemResourceLocation() + File.separator
                                           + "artifacts" + File.separator + "ESB" + File.separator + "security"
                                           + File.separator + "clientsslcert" + File.separator;
    private final String PROXY_SERVICE = "sslcert_validate_proxy";

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        ServerConfigurationManager serverManager = new ServerConfigurationManager(context);
        serverManager.applyConfigurationWithoutRestart(new File(CONFIG_LOCATION + "axis2.xml"));
        serverManager.copyToComponentLib(new File(CONFIG_LOCATION + CUSTOM_MEDIATOR_FILE_NAME));
        serverManager.restartGracefully();
        super.init();

        addProxyService(getArtifactConfig(PROXY_SERVICE + ".xml"));
        isProxyDeployed(PROXY_SERVICE);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = {"wso2.esb"}, description = "Send Soap Message to test client's SSL certificate")
    public void testMutualSSLClientCertificate() throws Exception {
        String trustStoreName = "client-truststore.jks";
        String keyStoreName = "wso2carbon.jks";
        String keyStorePassword = "wso2carbon";
        String trustStorePassword = "wso2carbon";

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

        //load key store file
        MutualSSLClient.loadKeyStore(CONFIG_LOCATION + keyStoreName, keyStorePassword);
        //load trust store file
        MutualSSLClient.loadTrustStore(CONFIG_LOCATION + trustStoreName, trustStorePassword);
        //create ssl socket factory instance with given key/trust stores
        MutualSSLClient.initMutualSSLConnection();

        Map<String, String> reqProps = new HashMap<>();
        reqProps.put("Content-type", "text/xml; charset=utf-8");
        reqProps.put("SOAPAction", "urn:echoString");
        String response;
        try {
            response = MutualSSLClient.sendPostRequest(getProxyServiceURLHttps(PROXY_SERVICE),
                                                       soapMessage, reqProps);
            log.info("Response received : " + response);
        } catch (IOException ioException) {
            log.error("Error sending Post request to proxy service", ioException);
            response = "";
        }
        Assert.assertEquals(response.contains("certs-true"), true);
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

    private OMElement getArtifactConfig(String fileName) throws Exception {
        OMElement synapseConfig;
        String path = File.separator
                      + "artifacts"
                      + File.separator + "ESB" + File.separator + "security"
                      + File.separator + "clientsslcert" + File.separator + fileName;
        try {
            synapseConfig = esbUtils.loadResource(path);
        } catch (FileNotFoundException e) {
            throw new Exception("File Location " + path + " may be incorrect", e);
        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML Stream Exception while reading file stream", e);
        }
        return synapseConfig;
    }
}
