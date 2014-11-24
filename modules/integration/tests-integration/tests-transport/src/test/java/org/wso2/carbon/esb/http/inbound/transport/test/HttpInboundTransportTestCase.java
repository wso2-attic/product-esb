/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.http.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;


public class HttpInboundTransportTestCase extends ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManager;
    private LogViewerClient logViewer;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
          addSequence(getOM("artifacts" + File.separator + "ESB" + File.separator
                  + "http.inbound.transport" + File.separator + "TestIn.xml"));
          addSequence(getOM("artifacts"+ File.separator+"ESB" + File.separator
                + "http.inbound.transport" + File.separator + "TestOut.xml"));
          addInboundEndpoint(getOM("artifacts"+ File.separator+"ESB" + File.separator
                   + "http.inbound.transport" + File.separator + "synapse.xml"));
    }

    @Test(groups = "wso2.esb", description = "Inbound Http  test case")
    public void inboundHttpTest() throws Exception {
        try {
            OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8081/services/StockQuote", null, "IBM");
            Assert.assertNotNull(response);
            Assert.assertEquals("getQuoteResponse",response.getLocalName());
    } catch (AxisFault expected) {
            String msg ="AxisFault occurred when sending Simple Stock Quote Service";
            throw new Exception(msg,expected);
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }


    private OMElement getOM(String relativeFilePath) throws Exception {
        OMElement synapseConfig =null;
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));
        try {
           synapseConfig = esbUtils.loadResource(relativeFilePath);
        } catch (FileNotFoundException e) {
            String msg = "File Location may be incorrect";
            throw new Exception(msg,e);
        } catch (XMLStreamException e) {
            String msg ="XML Stream Exception while reading file stream";
            throw new Exception(msg,e);
        }
       return synapseConfig;
    }
}
