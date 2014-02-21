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
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class InLineLocalEntryXsltTransformationTestCase extends ESBIntegrationTestCase {
    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        String filePath = "/mediators/xslt/xslt_in_line_local_entry_synapse.xml";
        loadESBConfigurationFromClasspath(filePath);

        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"},
          description = "Do XSLT transformation by selecting the xslt inline local entry")
    public void xsltTransformationWithInlineLocalEntry() throws AxisFault {
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

}
