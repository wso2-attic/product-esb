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

package org.wso2.carbon.esb.test.servers;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.MultipleServersManager;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewInstanceTestCase extends CarbonTestServerManager {
    public MultipleServersManager manager = new MultipleServersManager();
    public Map<String, String> startupParameterMap1 = new HashMap<String, String>();
    public Map<String, String> startupParameterMap2 = new HashMap<String, String>();

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @BeforeClass(groups = {"esb.multi.server"})
    public void testStartServers() throws IOException {
        startupParameterMap1.put("-DportOffset", "10");
        CarbonTestServerManager server1 = new CarbonTestServerManager(System.getProperty("carbon.zip"),
                                                                      startupParameterMap1);
        startupParameterMap2.put("-DportOffset", "20");
        CarbonTestServerManager server2 = new CarbonTestServerManager(System.getProperty("carbon.zip"),
                                                                      startupParameterMap2);
        manager.startServers(server1, server2);
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = {"esb.multi.server"})
    public void test() {
        System.out.println("Test server startup with system properties");
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @AfterClass
    public void clean() throws Exception {
        manager.stopAllServers();
    }
}

