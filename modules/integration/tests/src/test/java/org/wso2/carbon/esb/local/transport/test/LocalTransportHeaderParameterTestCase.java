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

package org.wso2.carbon.esb.local.transport.test;


import java.io.File;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.http.CommonsTransportHeaders;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.util.Utils;

/**
 * This test class test the 'Content-Encoding = gzip' in http header. Compression and decompression in ESB is tested by this class
 * had to remove  Accept-Encoding in proxy services since simple axis2 service does not support compressed response
 */

public class LocalTransportHeaderParameterTestCase extends ESBIntegrationTest {

    private final String AXIS2_CONFIG_URI_BASED_DISPATCH =
        "/local/axis2.xml";
    private ServerConfigurationManager configurationManager;
    
    @BeforeClass(alwaysRun = true)
    public void deployProxyServices() throws Exception {
        super.init();

        configurationManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        File customAxisConfig = new File(getESBResourceLocation() + AXIS2_CONFIG_URI_BASED_DISPATCH);

        // restart the esb with new customized axis2 configuration
        configurationManager.applyConfiguration(customAxisConfig);
        super.init(); // After restarting, this will establish the sessions.
        
        loadESBConfigurationFromClasspath("/artifacts/ESB/local/local-transport-header.xml");        
    }

    @Test(groups = {"wso2.esb"}, description = "Check local transport parameters")
    public void tetLocaltransportParameters() throws Exception {
    	try{
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURL("inner"), null, "WSO2");
        Assert.assertTrue(response.toString().contains("404"));
    	}catch(Exception e ){
    		Assert.assertTrue(e.getMessage().contains("404"));
    	}
    }


    @AfterClass(alwaysRun = true)
    public void unDeployProxyServices() throws Exception {
        super.cleanup();
    }

    private ServiceClient getServiceClient(String trpUrl)
            throws AxisFault {

        ServiceClient serviceClient;
        Options options = new Options();
        serviceClient = new ServiceClient();

        if (trpUrl != null && !"null".equals(trpUrl)) {
            options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        options.setAction("urn:getQuote");

        serviceClient.setOptions(options);
        return serviceClient;
    }
}





