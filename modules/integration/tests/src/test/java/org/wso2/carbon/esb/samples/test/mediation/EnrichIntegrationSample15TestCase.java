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
package org.wso2.carbon.esb.samples.test.mediation;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.ESBIntegrationTest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

public class EnrichIntegrationSample15TestCase extends ESBIntegrationTest {
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadSampleESBConfiguration(15);
    }

    @Test(groups = {"wso2.esb"}, description =
            "Enrich mediatoe :Replace the specified property with the body of source message :Test using sample 15")
    public void testXSLTTransformationWithTemplates() throws IOException, XMLStreamException {
        OMElement response=axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
        assertNotNull(response,"Response message is null");
        assertEquals(response.getLocalName(),"getQuoteResponse","getQuoteResponse not match");
        OMElement omElement=response.getFirstElement();
        String symbolResponse=omElement.getFirstChildWithName(new QName("http://services.samples/" +
                                                                        "xsd","symbol")).getText();
        assertEquals(symbolResponse,"MSFT","Request symbol not changed by enrich meidator ");
        assertNotSame(symbolResponse,"IBM","Request symbol not changed by enrich mediator");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        cleanup();
    }
}
