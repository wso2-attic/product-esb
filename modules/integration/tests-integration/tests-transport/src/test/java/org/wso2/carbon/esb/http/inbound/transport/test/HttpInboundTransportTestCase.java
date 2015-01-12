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
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;


public class HttpInboundTransportTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        addSequence(getArtifactConfig("TestIn.xml"));
        addSequence(getArtifactConfig("TestOut.xml"));
        addInboundEndpoint(getArtifactConfig("synapse.xml"));
    }

    @Test(groups = "wso2.esb", description = "Inbound Http  test case", enabled = false)
    public void inboundHttpTest() throws AxisFault {
        try {
            OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8081/services/StockQuote", null, "IBM");
            Assert.assertNotNull(response);
            Assert.assertEquals("getQuoteResponse", response.getLocalName());
        } catch (AxisFault axisFault) {
            throw new AxisFault("AxisFault occurred when sending SimpleStockQuoteService", axisFault);
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }


    private OMElement getArtifactConfig(String fileName) throws Exception {
        OMElement synapseConfig = null;
        String path = "artifacts" + File.separator + "ESB" + File.separator
                + "http.inbound.transport" + File.separator + fileName;
        try {
            synapseConfig = esbUtils.loadResource(path);
        } catch (FileNotFoundException e) {
            throw new Exception("File Location may be incorrect", e);
        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML Stream Exception while reading file stream", e);
        }
        return synapseConfig;
    }
}
