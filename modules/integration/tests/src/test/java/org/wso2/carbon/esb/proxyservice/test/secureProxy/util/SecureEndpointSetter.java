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
package org.wso2.carbon.esb.proxyservice.test.secureProxy.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.esb.util.EndpointGenerator;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;

public class SecureEndpointSetter {
    public static OMElement setEndpoint(String relativePath)
            throws XMLStreamException, FileNotFoundException {
        ESBTestCaseUtils util = new ESBTestCaseUtils();
        relativePath = relativePath.replaceAll("[\\\\/]", File.separator);
        OMElement synapse = util.loadClasspathResource(relativePath);
        //if builder is enable, keep current configuration
        if (FrameworkFactory.getFrameworkProperties(ProductConstant.ESB_SERVER_NAME).getEnvironmentSettings().is_builderEnabled()) {
            return synapse;
        }

        String config = synapse.toString();
        String secureStockQuoteService = EndpointGenerator.getEndpointServiceUrl("");

        config = config.replace("http://localhost:9007/services/"
                , secureStockQuoteService);
        config = config.replace("http://127.0.0.1:9007/services/"
                , secureStockQuoteService);
        return AXIOMUtil.stringToOM(config);
    }
}
