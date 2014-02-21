/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.deployment.test;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.HttpRequestUtil;
import org.wso2.carbon.automation.core.utils.HttpResponse;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.rest.test.security.Axis2ServerStartupTestCase;
import org.wso2.carbon.esb.util.ESBTestConstant;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.task.stub.TaskManagementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.*;

/**
 * This class can be used for test synapse config deployment - from file system scenarios
 */
public class DeploySynapseConfigsArtifactsFromFileSystemTestCase extends ESBIntegrationTest {

    private LogViewerClient logViewer;
    private int beforeLogSize;
    private Axis2ServerStartupTestCase axis2ServerStartupTestCase = new Axis2ServerStartupTestCase();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();

        //deploying axis2 sever services
        axis2ServerStartupTestCase.deployServices();

        log.info("Deploying synapse artifacts from the given file system location .....");

        logViewer = new LogViewerClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());

        beforeLogSize = logViewer.getAllSystemLogs().length;

        // deploying synapse artifacts inside the folder located in the file system
        esbUtils.deploySynapseArtifactsFromFileSystem(ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION
                + "artifacts/ESB/synapseconfig/default",
                esbServer.getBackEndUrl(), esbServer.getSessionCookie());

        log.info("Deployment of synapse artifacts from the given file system location is completed");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {

        //undeploying axis2 sever services
        axis2ServerStartupTestCase.unDeployServices();

        deleteApi("StockQuoteAPI");
        deleteApi("StockQuoteAPI2");
        deleteProxyServices("SampleProxy");
        deleteProxyServices("addressEndPoint");
        deleteProxyServices("StockQuoteProxy");
        deleteLocalEntry("local-entry-sequence-key");
        deleteEndpoint("addressEpTest1");
        deleteScheduledTask("SampleInjectToProxyTask", "synapse.simple.quartz");
    }

    @Test(groups = "wso2.esb", description = "Test Api Scenario")
    public void testApi() throws Exception {

        HttpResponse response = HttpRequestUtil.sendGetRequest(getApiInvocationURL
                ("stockquote") + "/view/IBM", null);

        log.info("Response for Test Api Scenario " + response);

        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatch");
        Assert.assertTrue(response.getData().contains("IBM"), "Response message is not as expected.");
        Assert.assertTrue(response.getData().contains
                ("IBM Company"), "Response message is not as expected");
    }

    @Test(groups = "wso2.esb", description = "Test proxy service scenario",
            dependsOnMethods = "testApi")
    public void testProxyService() throws Exception {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest
                (getProxyServiceSecuredURL("StockQuoteProxy"), null, "WSO2");

        log.info("Test proxy service scenario " + response);

        String lastPrice = response.getFirstElement().getFirstChildWithName
                (new QName("http://services.samples/xsd", "last"))
                .getText();

        log.info("Last price is " + lastPrice);

        assertNotNull(lastPrice, "Fault: response message 'last' price null");

        String symbol = response.getFirstElement().getFirstChildWithName
                (new QName("http://services.samples/xsd", "symbol"))
                .getText();

        log.info("Symbol is " + symbol);

        assertEquals(symbol, "WSO2", "Fault: value 'symbol' mismatched");

    }

    @Test(groups = {"wso2.esb"}, description = "Test local entry scenario",
            dependsOnMethods = "testProxyService")
    public void testLocalEntry() throws Exception {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL()
                , getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "IBM");

        log.info("Test local entry scenario " + response);

        assertNotNull(response, "Response is null");
        assertEquals(response.getLocalName(), "getQuoteResponse", "getQuoteResponse mismatch");

        OMElement omElement = response.getFirstElement();

        String symbolResponse = omElement.getFirstChildWithName
                (new QName("http://services.samples/xsd", "symbol")).getText();

        log.info(" Symbol Response " + symbolResponse);

        assertEquals(symbolResponse, "IBM", "Symbol is not match");

    }

    @Test(groups = {"wso2.esb"}, description = "Test Address endpoint scenario",
            dependsOnMethods = "testLocalEntry")
    public void testEndpoint()
            throws IOException, EndpointAdminEndpointAdminException,
            LoginAuthenticationExceptionException,
            XMLStreamException {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest
                (getProxyServiceURL("addressEndPoint")
                        , getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");

        log.info("Test Address endpoint scenario " + response);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
    }

    @Test(groups = {"wso2.esb"}, description = "Test scheduled task scenario",
            dependsOnMethods = "testEndpoint")
    public void testScheduledTask() throws Exception {

        LogEvent[] logs = logViewer.getAllSystemLogs();
        int afterLogSize = logs.length;

        boolean invokedLogFound = false;
        for (int i = 0; i < (afterLogSize - beforeLogSize); i++) {
            if (logs[i].getMessage().contains("PROXY INVOKED")) {
                invokedLogFound = true;
                break;
            }
        }

        log.info("Invoked Log Found status " + invokedLogFound);

        assertTrue(invokedLogFound);
    }

    private void deleteProxyServices(String proxyServiceName) throws Exception {
        if (esbUtils.isProxyServiceExist(esbServer.getBackEndUrl(), esbServer.getSessionCookie(),
                proxyServiceName)) {
            esbUtils.deleteProxyService(esbServer.getBackEndUrl(), esbServer.getSessionCookie(),
                    proxyServiceName);
            Assert.assertTrue(esbUtils.isProxyUnDeployed(esbServer.getBackEndUrl(),
                    esbServer.getSessionCookie(),
                    proxyServiceName), "Proxy Deletion failed or time out");
        }
    }

    private void deleteApi(String apiName) throws Exception {
        if (esbUtils.isApiExist(esbServer.getBackEndUrl(), esbServer.getSessionCookie(), apiName)) {
            esbUtils.deleteApi(esbServer.getBackEndUrl(), esbServer.getSessionCookie(), apiName);
            Assert.assertTrue(esbUtils.isApiUnDeployed(esbServer.getBackEndUrl(),
                    esbServer.getSessionCookie(),
                    apiName), "API Deletion failed or time out");
        }
    }

    private void deleteLocalEntry(String localEntryName)
            throws RemoteException, LocalEntryAdminException {
        if (esbUtils.isLocalEntryExist(esbServer.getBackEndUrl(), esbServer.getSessionCookie(),
                localEntryName)) {
            esbUtils.deleteLocalEntry(esbServer.getBackEndUrl(), esbServer.getSessionCookie(),
                    localEntryName);
            Assert.assertTrue(esbUtils.isLocalEntryUnDeployed(esbServer.getBackEndUrl(),
                    esbServer.getSessionCookie(), localEntryName),
                    "Local Entry Deletion failed or time out");
        }
    }

    private void deleteEndpoint(String endpointName)
            throws EndpointAdminEndpointAdminException, RemoteException {
        if (esbUtils.isEndpointExist(esbServer.getBackEndUrl(), esbServer.getSessionCookie(),
                endpointName)) {
            esbUtils.deleteEndpoint(esbServer.getBackEndUrl(), esbServer.getSessionCookie(),
                    endpointName);
            Assert.assertTrue(esbUtils.isEndpointUnDeployed(esbServer.getBackEndUrl(),
                    esbServer.getSessionCookie(),
                    endpointName), "Endpoint Deletion failed or time out");
        }
    }

    private void deleteScheduledTask(String taskName, String taskGroup)
            throws TaskManagementException, RemoteException {
        if (esbUtils.isScheduleTaskExist(esbServer.getBackEndUrl(), esbServer.getSessionCookie(),
                taskName)) {
            esbUtils.deleteScheduleTask(esbServer.getBackEndUrl(), esbServer.getSessionCookie(),
                    taskName, taskGroup);
            Assert.assertTrue(esbUtils.isScheduleTaskUnDeployed(esbServer.getBackEndUrl(),
                    esbServer.getSessionCookie(), taskName), "Scheduled Task Deletion failed or time out");
        }
    }
}

