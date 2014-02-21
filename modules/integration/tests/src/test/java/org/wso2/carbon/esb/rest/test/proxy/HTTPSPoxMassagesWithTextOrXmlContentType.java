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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.utils.httpclient.HttpsResponse;
import org.wso2.carbon.automation.utils.httpclient.HttpsURLConnectionClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.rest.test.security.util.RestEndpointSetter;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test 'support POX messages with text/xml content type' using https
 */
public class HTTPSPoxMassagesWithTextOrXmlContentType extends ESBIntegrationTest {

    private static String serviceURL;  // StudentServiceProxy url

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();

        //update esb configuration
        updateESBConfiguration(RestEndpointSetter.setEndpoint(File.separator + "artifacts" +
                File.separator + "ESB" + File.separator + "synapseconfig" + File.separator + "rest"
                + File.separator + "student-service-synapse.xml"));

        serviceURL = this.getProxyServiceSecuredURL("StudentServiceProxy");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

    @Test(groups = "wso2.esb", description = "Sending request - content type - text/xml")
    public void contentTypeTextOrXmlSecuredRequest() throws Exception {

        String addStudentDataPayload = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "   <p:addStudent xmlns:p=\"http://axis2.apache.org\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ns:student xmlns:ns=\"http://axis2.apache.org\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:age xmlns:xs=\"http://axis2.apache.org\">100</xs:age>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:name xmlns:xs=\"http://axis2.apache.org\">Wso2 Test Automation Student HTTPS One</xs:name>\n" +
                "         <!--0 or more occurrences-->\n" +
                "         <xs:subjects xmlns:xs=\"http://axis2.apache.org\">Automation</xs:subjects>\n" +
                "      </ns:student>\n" +
                "   </p:addStudent>";

        HttpsResponse response = HttpsURLConnectionClient.postWithBasicAuth(serviceURL,
                addStudentDataPayload, "text/xml", "admin", "admin");  // content type - text/xml

        log.info("Response " + response);

        assertTrue(response.getData().contains("Test Automation Student HTTPS One"));
    }

    @Test(groups = "wso2.esb", description = "Sending request - content type - application/xml",
            dependsOnMethods = "contentTypeTextOrXmlSecuredRequest")
    public void contentTypeApplicationOrXmlSecuredRequest() throws Exception {

        String addStudentDataPayload = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "   <p:addStudent xmlns:p=\"http://axis2.apache.org\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ns:student xmlns:ns=\"http://axis2.apache.org\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:age xmlns:xs=\"http://axis2.apache.org\">100</xs:age>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:name xmlns:xs=\"http://axis2.apache.org\">Wso2 Test Automation Student HTTPS Two</xs:name>\n" +
                "         <!--0 or more occurrences-->\n" +
                "         <xs:subjects xmlns:xs=\"http://axis2.apache.org\">Automation</xs:subjects>\n" +
                "      </ns:student>\n" +
                "   </p:addStudent>";

        HttpsResponse response = HttpsURLConnectionClient.postWithBasicAuth(serviceURL,
                addStudentDataPayload, "application/xml", "admin", "admin");
        // content type - application/xml

        log.info("Response " + response);

        assertTrue(response.getData().contains("Test Automation Student HTTPS Two"));
    }

    @Test(groups = "wso2.esb", description = "Sending request content type - application/soap+xml",
            dependsOnMethods = "contentTypeApplicationOrXmlSecuredRequest",
            expectedExceptions = IOException.class)
    public void contentTypeApplicationOrSoapAndXmlSecuredRequest() throws Exception {

        String addStudentDataPayload = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "   <p:addStudent xmlns:p=\"http://axis2.apache.org\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ns:student xmlns:ns=\"http://axis2.apache.org\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:age xmlns:xs=\"http://axis2.apache.org\">100</xs:age>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:name xmlns:xs=\"http://axis2.apache.org\">Wso2 Test Automation Student HTTPS Three</xs:name>\n" +
                "         <!--0 or more occurrences-->\n" +
                "         <xs:subjects xmlns:xs=\"http://axis2.apache.org\">Automation</xs:subjects>\n" +
                "      </ns:student>\n" +
                "   </p:addStudent>";

        HttpsResponse response = HttpsURLConnectionClient.postWithBasicAuth(serviceURL,
                addStudentDataPayload, "application/soap+xml", "admin", "admin");
        // content type - application/soap+xml

        log.info("Response " + response);
    }
}
