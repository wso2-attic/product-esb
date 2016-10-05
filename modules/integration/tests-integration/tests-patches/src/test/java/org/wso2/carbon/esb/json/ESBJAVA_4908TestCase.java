/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.json;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.File;


public class ESBJAVA_4908TestCase extends ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManager;
    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        serverConfigurationManager =
                new ServerConfigurationManager(new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
        serverConfigurationManager.applyConfiguration(new File(getESBResourceLocation() + File.separator + "json"
                + File.separator + "disableautoprimitivewithregex" + File.separator +"synapse.properties"));
        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB" + File.separator + "json"
                + File.separator + "disableautoprimitivewithregex" + File.separator
                + "disable-auto-primitive-with-regex.xml");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
    @Test(groups = "wso2.esb", description = "disabling auto primitive option with a given regex pattern in synapse " +
            "properties  ",  enabled = true)
    public void testDisablingAutoConversionToScientificNotationInJsonStreamFormatter() throws Exception {
        String payload = "<coordinates>\n" +
                "   <location>\n" +
                "       <name>Bermuda Triangle</name>\n" +
                "       <n>25e1</n>\n" +
                "       <w>7.1e1</w>\n" +
                "   </location>\n" +
                "   <location>\n" +
                "       <name>Eiffel Tower</name>\n" +
                "       <n>4.8e3</n>\n" +
                "       <e>1.8e2</e>\n" +
                "   </location>\n" +
                "</coordinates>";
        System.out.println("proxy port..."+ getProxyServiceURLHttp("disable-auto-primitive-with-regex"));
        HttpResponse response = httpClient.doPost(getProxyServiceURLHttp("disable-auto-primitive-with-regex")
                , null, payload, "application/xml");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        response.getEntity().writeTo(bos);
        String actualResult = new String(bos.toByteArray());
        String expectedPayload = "{\"coordinates\":{\"location\":[{\"name\":\"Bermuda Triangle\",\"n\":\"25e1\"" +
                ",\"w\":\"7.1e1\"},{\"name\":\"Eiffel Tower\",\"n\":\"4.8e3\",\"e\":\"1.8e2\"}]}}";
        Assert.assertEquals(actualResult, expectedPayload);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
        serverConfigurationManager.restoreToLastConfiguration();
    }
}
