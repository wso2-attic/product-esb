/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.esb.mediator.test.call;

import org.apache.axis2.AxisFault;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for calling the endpoint which returns No Content with blocking external calls
 */
public class CallMediatorBlockingNoContentTestCase extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB"
                + File.separator + "mediatorconfig" + File.separator + "call" + File.separator
                + "CallMediatorBlockingNoContentTest.xml");
    }

    @Test(groups = {"wso2.esb"},
            description = "Call the endpoint which responds NO CONTENT with blocking external calls")
    public void callMediatorBlockingNoContentTest() throws AxisFault, AutomationFrameworkException,
            MalformedURLException {
        Map<String, String> headers = new HashMap<>();
        String messageBody = "";
        HttpResponse response = HttpRequestUtil.doPost(new URL(getProxyServiceURLHttp("TestCallNCProxy"))
                , messageBody, headers);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_NO_CONTENT, "Response code should be 204");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
