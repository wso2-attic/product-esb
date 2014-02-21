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

package org.wso2.carbon.esb.endpoint.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.endpoint.EndPointAdminClient;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.HttpResponse;
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.automation.utils.httpclient.HttpURLConnectionClient;
import org.wso2.carbon.automation.utils.httpclient.HttpsResponse;
import org.wso2.carbon.automation.utils.httpclient.HttpsURLConnectionClient;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.ESBTestConstant;
import org.wso2.carbon.esb.util.ServiceDeploymentUtil;
import org.wso2.carbon.esb.util.WireMonitorServer;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class HttpEndpointTestCase extends ESBIntegrationTest {

    private SampleAxis2Server axis2Server1 = null;
    EnvironmentBuilder builder = null;

    private final String ENDPOINT_NAME = "HTTPEndpointTest";
    private EndPointAdminClient endPointAdminClient;

    private static final String studentName = "automationStudent";
    private static final String updateStudentName = "automationStudent2";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        if (FrameworkFactory.getFrameworkProperties(ProductConstant.ESB_SERVER_NAME).getEnvironmentSettings().is_builderEnabled()) {
            axis2Server1 = new SampleAxis2Server("test_axis2_server_9009.xml");
            axis2Server1.start();
            axis2Server1.deployService(ESBTestConstant.STUDENT_REST_SERVICE);
        } else {

            builder = new EnvironmentBuilder().as(ProductConstant.ADMIN_USER_ID);

            EnvironmentVariables appServer = builder.build().getAs();

            int deploymentDelay = builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
            ServiceDeploymentUtil deployer = new ServiceDeploymentUtil();
            String serviceFilePath = ProductConstant.getResourceLocations(ProductConstant.AXIS2_SERVER_NAME)
                    + File.separator + "aar" + File.separator + ESBTestConstant.STUDENT_REST_SERVICE + ".aar";

            deployer.deployArrService(appServer.getBackEndUrl(), appServer.getSessionCookie()
                    , ESBTestConstant.STUDENT_REST_SERVICE, serviceFilePath, deploymentDelay);
        }

        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB" + File.separator + "endpoint" + File.separator + "httpEndpointConfig" + File.separator + "synapse.xml");

        endPointAdminClient = new EndPointAdminClient(esbServer.getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
    }

    @AfterClass(groups = "wso2.esb")
    public void close() throws Exception {
        //resourceAdminServiceClient.deleteResource("/_system/config/test_ep_config");
        if (axis2Server1 != null && axis2Server1.isStarted()) {
            axis2Server1.stop();
        } else {
            if (builder != null) {
                EnvironmentVariables appServer = builder.build().getAs();
                int deploymentDelay = builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();

                ServiceDeploymentUtil deployer = new ServiceDeploymentUtil();
                deployer.unDeployArrService(appServer.getBackEndUrl(), appServer.getSessionCookie()
                        , ESBTestConstant.STUDENT_REST_SERVICE, deploymentDelay);
            }
        }

        endPointAdminClient = null;
        super.cleanup();
    }

    @Test(groups = {"wso2.esb"}, description = "Test HTTP Endpoint addition, deletion & stats", priority = 1)
    public void testHttpEndpoint() throws Exception {
        cleanupEndpoints();
        endpointAdditionScenario();
        endpointStatisticsScenario();
        endpointDeletionScenario();
    }

    @Test(groups = {"wso2.esb"}, description = "HTTP Endpoint POST Test: RESTful", priority = 5)
    public void testToPost() throws IOException, Exception {
        String addStudentData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "   <p:addStudent xmlns:p=\"http://axis2.apache.org\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ns:student xmlns:ns=\"http://axis2.apache.org\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:age xmlns:xs=\"http://axis2.apache.org\">100</xs:age>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:name xmlns:xs=\"http://axis2.apache.org\">" + studentName + "</xs:name>\n" +
                "         <!--0 or more occurrences-->\n" +
                "         <xs:subjects xmlns:xs=\"http://axis2.apache.org\">testAutomation</xs:subjects>\n" +
                "      </ns:student>\n" +
                "   </p:addStudent>";

        StringReader sendData = new StringReader(addStudentData);
        StringWriter responseData = new StringWriter();
        URL restURL = new URL((getProxyServiceURL("postEPProxy")) + "/students/");
        HttpURLConnectionClient.sendPostRequest(sendData, restURL, responseData, "application/xml");

        assertTrue(responseData.toString().contains(studentName), "response doesn't contain the expected output");
    }

    @Test(groups = {"wso2.esb"}, description = "HTTP Endpoint GET test: RESTful", priority = 6)
    public void testToGet() throws IOException {
        //check whether the student is added.
        String studentGetUri = getProxyServiceURL("getEPProxy") + "/student/" + studentName;
        HttpResponse getResponse = HttpURLConnectionClient.sendGetRequest(studentGetUri, null);

        assertTrue(getResponse.getData().contains("<ns:getStudentResponse xmlns:ns=\"http://axis2.apache.org\"><ns:return>" +
                "<ns:age>100</ns:age>" +
                "<ns:name>" + studentName + "</ns:name>" +
                "<ns:subjects>testAutomation</ns:subjects>" +
                "</ns:return></ns:getStudentResponse>"));
    }

    @Test(groups = {"wso2.esb"}, description = "HTTP Endpoint PUT Test: RESTful", priority = 7)
    public void testToPut() throws MalformedURLException, Exception {
        String updateStudentData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "   <p:addStudent xmlns:p=\"http://axis2.apache.org\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ns:student xmlns:ns=\"http://axis2.apache.org\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:age xmlns:xs=\"http://axis2.apache.org\">100</xs:age>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:name xmlns:xs=\"http://axis2.apache.org\">" + updateStudentName + "</xs:name>\n" +
                "         <!--0 or more occurrences-->\n" +
                "         <xs:subjects xmlns:xs=\"http://axis2.apache.org\">testAutomation</xs:subjects>\n" +
                "      </ns:student>\n" +
                "   </p:addStudent>";

        StringReader sendData = new StringReader(updateStudentData);
        StringWriter responseData = new StringWriter();
        URL restURL = new URL((getProxyServiceURL("putEPProxy")) + "/student/" + studentName);
        HttpURLConnectionClient.sendPutRequest(sendData, restURL, responseData, "application/xml");

        assertTrue(responseData.toString().contains(updateStudentName), "response doesn't contain the expected output");
    }


//    @Test(groups = {"wso2.esb"}, description = "HTTP Method X to HEAD HTTP Endpoint Test", priority = 8)
//    public void testToHead() {
//
//    }

    @Test(groups = {"wso2.esb"}, description = "HTTP Endpoint DELETE Test: RESTful", expectedExceptions = IOException.class, priority = 9)
    public void testToDelete() throws IOException {
        StringWriter responseData = new StringWriter();
        try {
            URL restURL = new URL((getProxyServiceURL("deleteEPProxy")) + "/student/" + updateStudentName);
            HttpURLConnectionClient.sendDeleteRequest(restURL, null);
        } catch (Exception e) {
            assertTrue(e instanceof Exception, "Failed to complete DELETE request.");
        }

        String studentGetUri = getProxyServiceURL("getEPProxy") + "/student/" + updateStudentName;
        HttpResponse getResponse = HttpURLConnectionClient.sendGetRequest(studentGetUri, null);
    }

    @Test(groups = {"wso2.esb"}, description = "HTTP endpoint POST test: SOAP", priority = 2)
    public void testSendingToHttpEndpoint()
            throws IOException, EndpointAdminEndpointAdminException,
            LoginAuthenticationExceptionException,
            XMLStreamException {
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("httpEndPoint")
                , getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        Assert.assertNotNull(response);
        Assert.assertTrue(response.toString().contains("WSO2 Company"));
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a Message to HTTP Endpoint with invalid URI", priority = 3)
    public void testSendingToInvalidHttpEndpoint()
            throws IOException, EndpointAdminEndpointAdminException,
            LoginAuthenticationExceptionException,
            XMLStreamException {
        try {
            OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("invalidHttpEndPoint"),
                    getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof org.apache.axis2.AxisFault);
        }
    }

    @Test(groups = {"wso2.esb"}, description = "Sending a Message to a HTTP endpoint with missing uri.var variable", priority = 4)
    public void testSendingToNoVarHttpEndpoint()
            throws XMLStreamException, FileNotFoundException, AxisFault {
        try {
            OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("missingVariableEndPoint"),
                    getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof org.apache.axis2.AxisFault);
        }
    }

    private void cleanupEndpoints()
            throws RemoteException, EndpointAdminEndpointAdminException {
        String[] endpointNames = endPointAdminClient.getEndpointNames();
        List endpointList;
        if (endpointNames != null && endpointNames.length > 0 && endpointNames[0] != null) {
            endpointList = Arrays.asList(endpointNames);
            if (endpointList.contains(ENDPOINT_NAME)) {
                endPointAdminClient.deleteEndpoint(ENDPOINT_NAME);
            }
        }
    }

    private void endpointAdditionScenario()
            throws Exception {
        int beforeCount = endPointAdminClient.getEndpointCount();

        addEndpoint(AXIOMUtil.stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<endpoint xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" + ENDPOINT_NAME + "\">\n" +
                "<http method=\"POST\"\n" +
                "              uri-template=\"http://localhost:9000/services/SimpleStockQuoteService\"/>" +
                "</endpoint>"));
        int afterCount = endPointAdminClient.getEndpointCount();
        assertEquals(1, afterCount - beforeCount);

        String[] endpoints = endPointAdminClient.getEndpointNames();
        if (endpoints != null && endpoints.length > 0 && endpoints[0] != null) {
            List endpointList = Arrays.asList(endpoints);
            assertTrue(endpointList.contains(ENDPOINT_NAME));
        } else {
            fail("Endpoint has not been added to the system properly");
        }
    }

    private void endpointStatisticsScenario()
            throws RemoteException, EndpointAdminEndpointAdminException {
        endPointAdminClient.enableEndpointStatistics(ENDPOINT_NAME);
        String endpoint = endPointAdminClient.getEndpointConfiguration(ENDPOINT_NAME);
        assertTrue(endpoint.contains("statistics=\"enable"));
    }

    private void endpointDeletionScenario()
            throws RemoteException, EndpointAdminEndpointAdminException {
        int beforeCount = endPointAdminClient.getEndpointCount();
        endPointAdminClient.deleteEndpoint(ENDPOINT_NAME);
        int afterCount = endPointAdminClient.getEndpointCount();
        assertEquals(1, beforeCount - afterCount);
    }

}

