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
package org.wso2.carbon.esb.samples.test.messaging;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;

/**
 * class tests sending two-way messages using JMS
 */
public class Sample264TestCase extends ESBIntegrationTest {

	private ServerConfigurationManager serverConfigurationManager;

	@BeforeClass(alwaysRun = true)
	public void init() throws Exception {
		super.init();
		context = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
		serverConfigurationManager = new ServerConfigurationManager(context);
		File sourceFile = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator +
		                           "ESB" + File.separator + "sample_config" + File.separator + "sample264" +
		                           File.separator + "axis2.xml");

		File targetFile = new File(CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf"
		                           + File.separator + "axis2" + File.separator + "axis2.xml");

		File sourceAxis2ServerFile = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File
				.separator +"ESB" + File.separator + "sample_config" + File.separator + "axis2.xml");

		File targetAxis2ServerFile = new File(CarbonUtils.getCarbonHome() + File.separator + "samples" + File
				.separator + "axis2Server" + File.separator + "repository" + File.separator + "conf" + File
				                                      .separator + "axis2.xml");

		serverConfigurationManager.applyConfigurationWithoutRestart(sourceFile, targetFile, true);
		serverConfigurationManager.applyConfigurationWithoutRestart(sourceAxis2ServerFile, targetAxis2ServerFile,true);
		loadSampleESBConfiguration(264);


	}


	@AfterClass(alwaysRun = true)
	public void deleteService() throws Exception {
		try {
			super.cleanup();
		}catch (Exception e){
			log.error("Error while cleaning up "+e.getMessage(),e);
			throw new Exception("Error while cleaning up "+e.getMessage(),e);
		}finally {
			if (serverConfigurationManager != null) {
				serverConfigurationManager.restoreToLastConfiguration();
			}
		}

	}

	@SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE})
	@Test(groups = {"wso2.esb"}, description = "Test JMS two way transport ")
	public void testJMSProxy() throws Exception {

		OMElement response = axis2Client.sendSimpleStockQuoteRequest("http://localhost:8280/"
		        , "http://localhost:9000/services/SimpleStockQuoteService", "Sample264");
		System.out.println(response.toString());
		Assert.assertTrue(response.toString().contains("Sample264"), "Invalid response message");


	}


}