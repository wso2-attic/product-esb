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
package org.wso2.carbon.esb.mediator.test.script;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.automation.utils.esb.JSONClient;
import org.wso2.carbon.esb.ESBIntegrationTest;

import javax.xml.namespace.QName;
import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class GroovyScriptSupportTestCase extends ESBIntegrationTest {

    private final String GROOVY_JAR = "groovy-all-1.1-rc-1.jar";
    private String GROOVY_JAR_LOCATION = File.separator + "jar" + File.separator + GROOVY_JAR;

    private ServerConfigurationManager serverManager;
    private final String proxyName = "groovyProxy";
    private JSONClient jsonclient;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void setEnvironment() throws Exception {
        super.init(1);
        serverManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        serverManager.copyToComponentLib(new File(getESBResourceLocation() + GROOVY_JAR_LOCATION));
        serverManager.applyConfiguration(new File(ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME)
                + File.separator + "synapseconfig" + File.separator + "messageStore" + File.separator + "scriptMediator"
                + File.separator + "axis2.xml"));
        super.init(1);
        jsonclient = new JSONClient();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = {"wso2.esb", "localonly"}, description = "Testing the groovy support in a proxy")
    public void GroovySupportWithinProxyTest() throws Exception {

        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/groovy/synapse_without_groovy.xml");
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL(proxyName), null, "WSO2");
        assertNotNull(response, "Response is null");
        assertEquals(response.getLocalName(), "getQuoteResponse", "getQuoteResponse mismatch");
        OMElement omElement = response.getFirstElement();
        String symbolResponse = omElement.getFirstChildWithName
                (new QName("http://services.samples/xsd", "symbol")).getText();
        assertEquals(symbolResponse, "WSO2", "Symbol is not match");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = {"wso2.esb", "localOnly"}, description = "Script Mediator -Run a Groovy script with the mediator")
    public void testGroovyScriptMediation() throws Exception {
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/script_mediator/groovy_script_with_the_mediator.xml");

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL(proxyName), null, "IBM");

        String lastPrice = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "last"))
                .getText();
        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        String symbol = response.getFirstElement().getFirstChildWithName(new QName("http://services.samples/xsd", "symbol"))
                .getText();
        assertEquals(symbol, "IBM", "Fault: value 'symbol' mismatched");
    }

    @AfterClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    public void destroy() throws Exception {
        try {
            super.cleanup();
            Thread.sleep(5000);
        } finally {

            serverManager.removeFromComponentLib(GROOVY_JAR);
            serverManager.restartGracefully();
            serverManager.restoreToLastConfiguration();
            serverManager = null;
        }

    }

}
