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
 * This class can be used to test 'support POX messages with text/xml content type'
 * after applying security policy 01
 */
public class POXMassagesWithApplySecurityTextOrXmlContentType extends ESBIntegrationTest {

    private static final String SERVICE_NAME = "StudentServiceProxy";
    private String securedRestURL;    // StudentServiceProxy secured url

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();

        //update esb configuration
        updateESBConfiguration(RestEndpointSetter.setEndpoint(File.separator + "artifacts" +
                File.separator + "ESB" + File.separator + "synapseconfig" + File.separator + "rest"
                + File.separator + "student-service-synapse.xml"));

        //applying security policy 01 -user token
        applySecurity("StudentServiceProxy", 1, new String[]{"everyone"});

        securedRestURL = getProxyServiceSecuredURL(SERVICE_NAME) + "/students";
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

    @Test(groups = {"wso2.esb"}, description = "Sending request - content type - text/xml")
    public void contentTypeTextOrXml() throws Exception {

        String addStudentData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "   <p:addStudent xmlns:p=\"http://axis2.apache.org\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ns:student xmlns:ns=\"http://axis2.apache.org\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:age xmlns:xs=\"http://axis2.apache.org\">100</xs:age>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:name xmlns:xs=\"http://axis2.apache.org\">Wso2 Test Automation Student One POX</xs:name>\n" +
                "         <!--0 or more occurrences-->\n" +
                "         <xs:subjects xmlns:xs=\"http://axis2.apache.org\">Automation</xs:subjects>\n" +
                "      </ns:student>\n" +
                "   </p:addStudent>";


        HttpsResponse response = HttpsURLConnectionClient.postWithBasicAuth(securedRestURL,
                addStudentData, "text/xml", userInfo.getUserName(), userInfo.getPassword());
        // content type - text/xml

        assertTrue(response.getData().contains("Test Automation Student One POX"));
    }

    @Test(groups = {"wso2.esb"}, description = "Sending request - content type - application/xml",
            dependsOnMethods = "contentTypeTextOrXml")
    public void contentTypeApplicationOrXml() throws Exception {

        String addStudentData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "   <p:addStudent xmlns:p=\"http://axis2.apache.org\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ns:student xmlns:ns=\"http://axis2.apache.org\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:age xmlns:xs=\"http://axis2.apache.org\">100</xs:age>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:name xmlns:xs=\"http://axis2.apache.org\">Wso2 Test Automation Student Two POX</xs:name>\n" +
                "         <!--0 or more occurrences-->\n" +
                "         <xs:subjects xmlns:xs=\"http://axis2.apache.org\">Automation</xs:subjects>\n" +
                "      </ns:student>\n" +
                "   </p:addStudent>";

        HttpsResponse response = HttpsURLConnectionClient.postWithBasicAuth(securedRestURL,
                addStudentData, "application/xml", userInfo.getUserName(), userInfo.getPassword());
        // content type - application/xml

        assertTrue(response.getData().contains("Test Automation Student Two POX"));
    }

    @Test(groups = {"wso2.esb"}, description = "Sending request - content type - application/soap+xml",
            dependsOnMethods = "contentTypeApplicationOrXml", expectedExceptions = IOException.class)
    public void contentTypeApplicationOrSoapAndXml() throws Exception {

        String addStudentData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "   <p:addStudent xmlns:p=\"http://axis2.apache.org\">\n" +
                "      <!--0 to 1 occurrence-->\n" +
                "      <ns:student xmlns:ns=\"http://axis2.apache.org\">\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:age xmlns:xs=\"http://axis2.apache.org\">100</xs:age>\n" +
                "         <!--0 to 1 occurrence-->\n" +
                "         <xs:name xmlns:xs=\"http://axis2.apache.org\">Wso2 Test Automation Student Three POX</xs:name>\n" +
                "         <!--0 or more occurrences-->\n" +
                "         <xs:subjects xmlns:xs=\"http://axis2.apache.org\">Automation</xs:subjects>\n" +
                "      </ns:student>\n" +
                "   </p:addStudent>";

        HttpsResponse response = HttpsURLConnectionClient.postWithBasicAuth(securedRestURL,
                addStudentData, "application/soap+xml", userInfo.getUserName(),
                userInfo.getPassword()); // content type - application/soap+xml

        assertTrue(response.getData().contains("Test Automation Student Three POX"));
    }
}
