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

import javax.xml.namespace.QName;
import org.apache.axiom.om.OMElement;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestConstant;

/**
 * Sample 16: Introduction to Dynamic and Static Registry Keys
 */
public class Sample16TestCase extends ESBIntegrationTest {
    private String oldSynapseConfig;

    @BeforeClass(alwaysRun = true)
    public void uploadSynapseConfig() throws Exception {
        super.init();
        loadSampleESBConfiguration(16);
    }

    @Test(groups = { "wso2.esb" }, description = "Dynamic and Static Registry Keys", enabled = false)
    public void testDynamicAndStaticRegistryKeys() throws Exception {
        OMElement response = axis2Client.sendCustomQuoteRequest(getMainSequenceURL()
                , getBackEndServiceUrl(ESBTestConstant.SIMPLE_STOCK_QUOTE_SERVICE), "WSO2");
        assertNotNull(response, "Response message is null");
        assertEquals(response.getLocalName(), "CheckPriceResponse", "CheckPriceResponse not match");
        assertTrue(response.toString().contains("Price"), "No price tag in response");
        assertTrue(response.toString().contains("Code"), "No code tag in response");
        assertEquals(response.getFirstChildWithName
                         (new QName("http://services.samples/xsd", "Code")).getText(), "WSO2",
                     "Symbol not match"
        );
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }
}
