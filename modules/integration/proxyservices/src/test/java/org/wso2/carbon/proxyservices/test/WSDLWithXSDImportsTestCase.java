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

package org.wso2.carbon.proxyservices.test;

import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.ESBTestServerManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.assertTrue;

public class WSDLWithXSDImportsTestCase extends ESBIntegrationTestCase {

    private ESBTestServerManager serverManager = new ESBTestServerManager();

    public WSDLWithXSDImportsTestCase() {
        super("ProxyServiceAdmin") ;
    }

    public void init() throws Exception {
        serverManager.copyArtifacts();
    }

    @Test(groups = {"wso2.esb"}, description = "Test WSDL with import XSDs.")
    public void testWSDLWithXSDImports() throws IOException {
        loadESBConfigurationFromClasspath("/import_wsdl_proxy.xml");

        String relativeWSDL = getWSDL(new URL(
                "http://localhost:8280/services/RelativePathProxy?wsdl"));
        assertTrue(relativeWSDL.contains("<xsd:import namespace=\"http://mycompany.com/hr/schemas\" " +
                "schemaLocation=\"RelativePathProxy?xsd=hr.xsd\" />"));

        String absoluteWSDL = getWSDL(new URL(
                "http://localhost:8280/services/AbsolutePathProxy?wsdl"));
        assertTrue(absoluteWSDL.contains("<xsd:import namespace=\"http://mycompany.com/hr/schemas\" " +
                "schemaLocation=\"hr.xsd\" />"));
    }

    private String getWSDL(URL url) throws IOException {
        InputStream in = url.openStream();
        StringBuffer buffer = new StringBuffer();
        byte[] data = new byte[1024];
        int len;
        while ((len = in.read(data)) != -1) {
            buffer.append(new String(data, 0, len));
        }
        return buffer.toString();
    }
}
