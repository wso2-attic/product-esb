/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.esb.integration.nhttp;

import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

public class HTTPRequestHostHeaderPropertyTestCase extends ESBIntegrationTestCase {
	
	private StockQuoteClient axis2Client;
	
	public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
        log.info("Verifying functionality of REQUEST_HOST_HEADER property");
    }
	
	@Test(groups = {"wso2.esb"}, description = "This test case verifies REQUEST_HOST_HEADER property functionality. This\n" +
            " makes sure that the REQUEST_HOST_HEADER property value will set the HTTP Host header value.")
	public void testHTTPRequestHostHeaderPropertyTestCase () throws IOException {
		loadESBConfigurationFromClasspath("/http_host_header_set.xml");
		OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                getProxyServiceURL("HttpHostHeaderSetProxy", false),
                "http://localhost:9000/services/SimpleStockQuoteService", "WSO2");
        assertTrue(response.toString().contains("WSO2"));
	}
	
	public void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }

}
