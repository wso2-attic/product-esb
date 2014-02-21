/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.esb.integration.nhttp;

import org.apache.woden.wsdl20.Description;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class GETRequestHandlerTestCase extends ESBIntegrationTestCase {

    public void init() throws Exception {        
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"}, description = "Proxy Service WSDL Generation")
    public void testProxyServiceWSDL() throws Exception {
        loadSampleESBConfiguration(150);

        WSDLFactory fac = WSDLFactory.newInstance();
        WSDLReader reader = fac.newWSDLReader();
        Definition def = reader.readWSDL(getProxyServiceURL("StockQuoteProxy", false) + "?wsdl");
        assertNotNull(def);
        assertEquals(1, def.getAllServices().size());
        
        org.apache.woden.WSDLFactory fac2 = org.apache.woden.WSDLFactory.newInstance();
        org.apache.woden.WSDLReader reader2 = fac2.newWSDLReader();
        Description desc = reader2.readWSDL(getProxyServiceURL("StockQuoteProxy", false) + "?wsdl2");
        assertNotNull(desc);
        assertEquals(1, desc.getServices().length);
    }
}
