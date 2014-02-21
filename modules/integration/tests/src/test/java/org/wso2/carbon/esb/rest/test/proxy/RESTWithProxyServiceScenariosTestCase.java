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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.esb.ESBIntegrationTest;

import java.io.File;

import static org.testng.Assert.assertEquals;

/**
 * This class can be used to test 'Using REST with a proxy service' test scenarios at,
 * http://docs.wso2.org/wiki/display/ESB470/Using+REST+with+a+Proxy+Service#UsingRESTwithaProxyService-RESTClientandRESTService
 */
public class RESTWithProxyServiceScenariosTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();

        //update esb configuration
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/messagewithoutcontent/" +
                "synapse.xml");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

    @Test(groups = {"wso2.esb"}, description = "Sending REST request backend expects SOAP 1.1")
    public void contentTypeApplicationOrXml() throws Exception {

        String strURL = getMainSequenceURL();

        PostMethod post = new PostMethod(strURL);

        String strXMLFilename = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                File.separator + "ESB" + File.separator + "synapseconfig" + File.separator
                + "messagewithoutcontent" + File.separator + "xmlrequest.xml";


        File input = new File(strXMLFilename);

        RequestEntity entity = new FileRequestEntity(input, "application/xml");
        post.setRequestEntity(entity);

        HttpClient httpclient = new HttpClient();
        int result = httpclient.executeMethod(post);

        assertEquals(result, 200);
    }
}
