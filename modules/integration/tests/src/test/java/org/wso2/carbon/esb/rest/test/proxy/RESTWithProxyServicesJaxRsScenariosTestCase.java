/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.rest.test.proxy;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.customservers.tomcat.TomcatServerManager;
import org.wso2.carbon.automation.core.customservers.tomcat.TomcatServerType;
import org.wso2.carbon.automation.core.utils.HttpResponse;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.automation.utils.httpclient.HttpURLConnectionClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.esb.integration.services.jaxrs.customersample.CustomerConfig;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test 'Using REST with a proxy service' test scenarios at,
 * http://docs.wso2.org/wiki/display/ESB470/Using+REST+with+a+Proxy+Service#UsingRESTwithaProxyService-RESTClientandRESTService
 * SOAP Client and REST Service , REST Client and REST Service
 */
public class RESTWithProxyServicesJaxRsScenariosTestCase extends ESBIntegrationTest {

    private TomcatServerManager tomcatServerManager;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();

        String basedirLocation = System.getProperty("basedir") + File.separator + "target";
        tomcatServerManager = new TomcatServerManager(CustomerConfig.class.getName(),
                TomcatServerType.jaxrs.name(), 8080);

        tomcatServerManager.startServer();  // starting tomcat server instance
        Thread.sleep(5000);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();

        tomcatServerManager.stop();
        if (tomcatServerManager.isRunning()) {
            log.info("Tomcat is running");
        } else {
            log.info("Tomcat is stopped");
        }
    }

    @Test(groups = "wso2.esb", description = "sending soap request to a rest service")
    public void soapClientRestService() throws Exception {

        //update esb configuration
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/rest/" +
                "customer-service-proxy.xml");

        AxisServiceClient axisServiceClient = new AxisServiceClient();

        OMElement response = axisServiceClient.sendReceive(createPayload(), esbServer.getServiceUrl()
                + "/CustomerServiceProxy", "getCustomer", "text/xml");

        assertEquals(response.toString(), "<Customer><id>123</id><name>John</name></Customer>");
    }

    @Test(groups = "wso2.esb", description = "sending rest request to a rest service",
            dependsOnMethods = "soapClientRestService")
    public void restClientRestService() throws Exception {

        //update esb configuration
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/rest/" +
                "rest-client-and-rest-service.xml");

        HttpResponse httpResponse = HttpURLConnectionClient.sendGetRequest
                (getProxyServiceURL("RestClientRestServiceProxy"), null);

        assertTrue(httpResponse.getData().
                contains("<Customer><id>123</id><name>John</name></Customer>"));
    }

    private OMElement createPayload() {   // creation of payload for getCustomer

        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();

        OMElement operation = fac.createOMElement("getCustomer", null);
        OMElement getId = fac.createOMElement("id", null);

        getId.setText("123");
        operation.addChild((getId));

        return operation;
    }
}
