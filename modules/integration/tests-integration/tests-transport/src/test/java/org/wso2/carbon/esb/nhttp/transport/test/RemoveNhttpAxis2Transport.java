/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.esb.nhttp.transport.test;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * Removes the nhttp axis2 and restores pt axis2 after nhttp tests are completed
 */
public class RemoveNhttpAxis2Transport extends ESBIntegrationTest {

	@BeforeClass(alwaysRun = true)
	public void init() throws Exception {

		super.init();
		AutomationContext autoCtx = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
		ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(autoCtx);
		serverConfigurationManager.restoreToLastConfiguration();
	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
	@Test(groups = "wso2.esb")
	public void dummyTestClass() throws Exception {
		//Without a test method, the init() will not be executed
	}
}
