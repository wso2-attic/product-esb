/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.esb.passthru.transport.test;

import java.io.File;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * This test case was used to verify that the ESB never hangs after 5 connection
 * reset by peer errors when using a VFS proxy to send requests to an HTTP
 * back end. This scenario can only be reproduced when the payload is larger (>
 * 550 KB). This test needs to be run in <code>PLATFORM</code> mode since the
 * EchoServer that resets the connections needs to be running on a different
 * machine
 *
 */
public class ESBJAVA3660ConnectionResetByPeerThreadBlockTestCase extends ESBIntegrationTest {
	private ServerConfigurationManager serverConfigurationManager;
	private String COMMON_FILE_LOCATION = getClass().getResource(File.separator + "artifacts" +
	                                                                     File.separator + "ESB" +
	                                                                     File.separator +
	                                                                     "synapseconfig" +
	                                                                     File.separator +
	                                                                     "vfsTransport" +
	                                                                     File.separator).getPath();

	private boolean isProxyDeployed = false;

	final String carbonLogFile = CarbonBaseUtils.getCarbonHome() +
	                             "/repository/logs/wso2carbon.log";

	@BeforeTest(alwaysRun = true)
	protected void init() throws Exception {
		super.init();
		COMMON_FILE_LOCATION =
		                       System.getProperty("framework.resource.location") +
		                               "/artifacts/ESB/synapseconfig/vfsTransport/";

		serverConfigurationManager = new ServerConfigurationManager(context);
		serverConfigurationManager.applyConfiguration(new File(COMMON_FILE_LOCATION + "axis2.xml"));

		final String log4jSourceFile =
		                               System.getProperty("framework.resource.location") +
		                                       "/artifacts/ESB/synapseconfig/vfsTransport/connection/reset/log4j.properties";
		final String log4jDestinationFile =
		                                    CarbonBaseUtils.getCarbonHome() +
		                                            "/repository/conf/log4j.properties";

		serverConfigurationManager.applyConfiguration(new File(log4jSourceFile),
		                                              new File(log4jDestinationFile));
		super.init();
		addVFSProxy();
	}

	private void addVFSProxy() throws Exception {

		addProxyService(AXIOMUtil.stringToOM("<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"ConnectionResetvfserrortest\" transports=\"https http vfs\" startOnLoad=\"true\" trace=\"disable\">\n"
		                                     + "        <target>\n"
		                                     + "            <inSequence>\n"
		                                     + "			      <property name=\"Action\" value=\"urn:mediate\" scope=\"default\" type=\"STRING\"/>"
		                                     + " 			  	  <log level=\"full\" />"
		                                     + "                <send>\n"
		                                     + "                    <endpoint>\n"
		                                     + "                    <address uri=\"http://10.100.5.137:8290\">\n"
		                                     + "                    	<timeout>\n"
		                                     + "                			<duration>30000</duration>\n"
		                                     + "							<responseAction>fault</responseAction>\n"
		                                     + "						</timeout>\n"
		                                     + " 						<suspendOnFailure>\n"
		                                     + " 							<errorCodes>-1</errorCodes>\n"
		                                     + "  							<progressionFactor>1.0</progressionFactor>\n"
		                                     + " 						</suspendOnFailure>\n"
		                                     + " 						<markForSuspension>\n"
		                                     + "  							<errorCodes>-1</errorCodes>\n"
		                                     + "  						</markForSuspension>\n"
		                                     + "            		</address>\n"
		                                     + "   					</endpoint>\n"
		                                     + "   				</send>\n"
		                                     + "  			</inSequence>\n"
		                                     + "  			<outSequence>\n"
		                                     + "  				<drop/>\n"
		                                     + " 			</outSequence>\n"
		                                     + "        </target>\n"
		                                     + "        <!--CHANGE-->\n"
		                                     + "        <parameter name=\"transport.vfs.FileURI\">file://" +
		                                     COMMON_FILE_LOCATION +
		                                     File.separator +
		                                     "test" +
		                                     File.separator +
		                                     "in" +
		                                     File.separator +
		                                     "</parameter>\n" +
		                                     "        <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n" +
		                                     "        <parameter name=\"transport.vfs.FileNamePattern\">.*\\.*</parameter>\n" +
		                                     "        <parameter name=\"transport.PollInterval\">2</parameter>\n" +
		                                     "        <!--CHANGE-->\n" +
		                                     "        <parameter name=\"transport.vfs.MoveAfterProcess\">file://" +
		                                     COMMON_FILE_LOCATION +
		                                     "test" +
		                                     File.separator +
		                                     "out" +
		                                     File.separator +
		                                     "</parameter>\n" +
		                                     "        <!--CHANGE-->\n" +
		                                     "        <parameter name=\"transport.vfs.MoveAfterFailure\">file://" +
		                                     COMMON_FILE_LOCATION +
		                                     File.separator +
		                                     "test" +
		                                     File.separator +
		                                     "out" +
		                                     File.separator +
		                                     "</parameter>\n" +
		                                     "        <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n" +
		                                     "        <parameter name=\"transport.vfs.ActionAfterFailure\">MOVE</parameter>\n" +
		                                     "    </proxy>\n"));
		isProxyDeployed = true;
	}

	/**
	 * This method copies the large payload file <code>7</code> times to the
	 * location where vfs listens. If the ESB does not hang, we should be able
	 * to get <code>7</code> connection reset by peer errors in the log.
	 * 
	 * @throws Exception
	 */
	@SetEnvironment(executionEnvironments = {ExecutionEnvironment.PLATFORM})
	@Test(groups = { "wso2.esb" }, description = "Verify that the threads are not blocked after 5 connction resets.")
	public void testThreadBlockAfterFiveConnectionResets() throws Exception {
		Thread.sleep(2000);
		File afile = new File(COMMON_FILE_LOCATION + File.separator + "toto.edi");
		File bfile =
		             new File(COMMON_FILE_LOCATION + File.separator + "test" + File.separator +
		                      "in" + File.separator + "toto.edi");

		final String expectedContent = "java.io.IOException: Connection reset by peer";

		FileUtils.copyFile(afile, bfile);
		Thread.sleep(4000);
		FileUtils.copyFile(afile, bfile);
		Thread.sleep(4000);
		FileUtils.copyFile(afile, bfile);
		Thread.sleep(4000);
		FileUtils.copyFile(afile, bfile);
		Thread.sleep(4000);
		FileUtils.copyFile(afile, bfile);
		Thread.sleep(4000);
		FileUtils.copyFile(afile, bfile);
		Thread.sleep(4000);
		FileUtils.copyFile(afile, bfile);
		Thread.sleep(4000);

		Assert.assertTrue(FileUtil.containsInFile(carbonLogFile, expectedContent, 7),
		                  "The ESB hangs after 5 Connection reset by peer errors.");
	}

	@AfterClass(alwaysRun = true)
	public void restoreServerConfiguration() throws Exception {
		try {
			if (isProxyDeployed) {
				deleteProxyService("ConnectionResetvfserrortest");
			}
			super.cleanup();
		} finally {
			Thread.sleep(3000);
			// reverting the changes done to esb sever
			serverConfigurationManager.restoreToLastConfiguration();
			serverConfigurationManager = null;

		}

	}
}
