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
package org.wso2.carbon.mediator.tests.xslt;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import javax.activation.DataHandler;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DynamicKeyXsltTransformationTestCase extends ESBIntegrationTestCase {
    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        String filePath = "/mediators/xslt/xslt_dynamic_key_synapse.xml";
        uploadResourcesToRegistry();
        loadESBConfigurationFromClasspath(filePath);

        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"},
          description = "Do XSLT transformation by Select the key type as dynamic key and retrieve" +
                        " the transformation from that.")
    public void xsltTransformationFromDynamicKey() throws AxisFault {
        OMElement response;

        response = axis2Client.sendCustomQuoteRequest(
                getMainSequenceURL(),
                null,
                "IBM");
        assertNotNull(response, "Response message null");
        assertTrue(response.toString().contains("Code"));
        assertTrue(response.toString().contains("IBM"));

    }

    @Override
    protected void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }

    private void uploadResourcesToRegistry() throws Exception {
        ResourceAdminServiceStub resourceAdminServiceStub =
                new ResourceAdminServiceStub(getAdminServiceURL("ResourceAdminService"));
        PropertiesAdminServiceStub propertiesAdminServiceStub =
                new PropertiesAdminServiceStub(getAdminServiceURL("PropertiesAdminService"));
        authenticate(resourceAdminServiceStub);
        authenticate(propertiesAdminServiceStub);

        resourceAdminServiceStub.delete("/_system/config/localEntries");
        resourceAdminServiceStub.addCollection("/_system/config/", "localEntries", "",
                                               "Contains dynamic sequence request entry");

        resourceAdminServiceStub.addResource(
                "/_system/config/localEntries/request_transformation.txt", "text/plain", "text files",
                new DataHandler("Dynamic Sequence request transformation".getBytes(), "application/text"), null);
        propertiesAdminServiceStub.setProperty("/_system/config/localEntries/request_transformation.txt",
                                               "resourceName", "request_transform.xslt");
        Thread.sleep(1000);

        resourceAdminServiceStub.delete("/_system/governance/localEntries");
        resourceAdminServiceStub.addCollection("/_system/governance/", "localEntries", "",
                                               "Contains dynamic sequence response entry");
        resourceAdminServiceStub.addResource(
                "/_system/governance/localEntries/response_transformation_back.txt", "text/plain", "text files",
                new DataHandler("Dynamic Sequence response transformation".getBytes(), "application/text"), null);
        propertiesAdminServiceStub.setProperty("/_system/governance/localEntries/response_transformation_back.txt",
                                               "resourceName", "response_transform.xslt");


    }
}
