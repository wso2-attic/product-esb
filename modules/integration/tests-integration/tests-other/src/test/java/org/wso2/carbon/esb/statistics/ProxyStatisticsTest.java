/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.esb.statistics;

import junit.framework.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.clients.StatisticsEnableAdminClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestCaseUtils;
import org.wso2.esb.integration.common.utils.servers.ThriftServer;

import java.io.File;

public class ProxyStatisticsTest extends ESBIntegrationTest {
	ThriftServer thriftServer;
	private ServerConfigurationManager serverConfigurationManager;
	StatisticsEnableAdminClient statisticsEnableAdminClient;

	@BeforeClass(alwaysRun = true) protected void initialize() throws Exception {
		//Starting the thrift port to listen to statistics events
		thriftServer = new ThriftServer("Wso2EventTestCase", 8461, true);
		thriftServer.start(8462);
		log.info("Thrift Server is Started on port 8462");

		//Changing synapse configuration to enable statistics and tracing
		serverConfigurationManager =
				new ServerConfigurationManager(new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN));
		serverConfigurationManager.applyConfiguration(
				new File(getESBResourceLocation() + File.separator + "StatisticTestResources" + File.separator +
				         "synapse.properties"));
		super.init();

		//Configuring thrift server configuration in ESB
		statisticsEnableAdminClient = new StatisticsEnableAdminClient(contextUrls.getBackEndUrl(), getSessionCookie());
		statisticsEnableAdminClient.addStatisticsConfiguration();

		//load esb configuration to the server
		loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/statistics/synapseconfigproxy.xml");
		Thread.sleep(20000); //waiting for esb to send artifact config data to the thriftserver

		//Checking whether all the artifact configuration events are received
		Assert.assertEquals("Three configuration events are required", 4, thriftServer.getMsgCount());
	}

	@Test(groups = { "wso2.esb" }, description = "Proxy statistics message count check.")
	public void statisticsCollectionCountTest()
			throws Exception {
		thriftServer.resetMsgCount();
		thriftServer.resetPreservedEventList();

		for (int i = 0; i < 100; i++) {
			axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("StockQuoteProxy"), null, "WSO2");
		}
		Thread.sleep(10000); //wait to esb for asynchronously send statistics events to the backend
		Assert.assertEquals("Hundred statistics events are required, but different number is found", 100,
		                    thriftServer.getMsgCount());
	}

	@Test(groups = { "wso2.esb" }, description = "Proxy statistics statistics event data check")
	public void statisticsEventDataTest()
			throws Exception {
		thriftServer.resetMsgCount();
		thriftServer.resetPreservedEventList();

		axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("StockQuoteProxy"), null, "WSO2");
		Thread.sleep(10000);//wait to esb for asynchronously send statistics events to the backend
		Assert.assertEquals("Statistics event is received", 1, thriftServer.getMsgCount());
		String jsonString =
				ESBTestCaseUtils.decompress((String) thriftServer.getPreservedEventList().get(0).getPayloadData()[1]);
		Assert.assertNotNull("Payload of the reported statistics event is null", jsonString);

		/* Mediator list in the StockQuoteProxy
		StockQuoteProxy@0:StockQuoteProxy
		StockQuoteProxy@1:AnonymousEndpoint
		StockQuoteProxy@2:PROXY_OUTSEQ
		StockQuoteProxy@3:SendMediator
		 */
		Assert.assertTrue("Entry StockQuoteProxy@0:StockQuoteProxy not exists in statistics event",
		                  jsonString.contains("StockQuoteProxy@0:StockQuoteProxy"));
		Assert.assertTrue("Entry StockQuoteProxy@1:AnonymousEndpoint not exists in statistics event",
		                  jsonString.contains("StockQuoteProxy@1:AnonymousEndpoint"));
		Assert.assertTrue("Entry StockQuoteProxy@2:PROXY_OUTSEQ not exists in statistics event",
		                  jsonString.contains("StockQuoteProxy@2:PROXY_OUTSEQ"));
		Assert.assertTrue("Entry StockQuoteProxy@3:SendMediator not exists in statistics event",
		                  jsonString.contains("StockQuoteProxy@3:SendMediator"));
	}

	@Test(groups = {
			"wso2.esb" }, description = "Proxy Spilt Aggregate scenario statistics message count check.") public void statisticsSpiltAggregateProxyCollectionCountTest()
			throws Exception {
		thriftServer.resetMsgCount();
		thriftServer.resetPreservedEventList();

		for (int i = 0; i < 100; i++) {
			axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("StockQuoteProxy"), null, "WSO2");
		}
		Thread.sleep(10000); //wait to esb for asynchronously send statistics events to the backend
		Assert.assertEquals("Hundred statistics events are required, but different number is found", 100,
		                    thriftServer.getMsgCount());
	}

	@Test(groups = { "wso2.esb" }, description = "Proxy SpiltAggregate statistics event data check")
	public void spiltAggregatesStatisticsEventDataTest() throws Exception {
		thriftServer.resetMsgCount();
		thriftServer.resetPreservedEventList();

		axis2Client.sendMultipleQuoteRequest(getProxyServiceURLHttp("SplitAggregateProxy"), null, "WSO2", 4);
		Thread.sleep(10000);//wait to esb for asynchronously send statistics events to the backend
		Assert.assertEquals("Statistics event is received", 1, thriftServer.getMsgCount());
		String jsonString =
				ESBTestCaseUtils.decompress((String) thriftServer.getPreservedEventList().get(0).getPayloadData()[1]);
		Assert.assertNotNull("Payload of the reported statistics event is null", jsonString);

		/* Mediator list in the proxy
		SplitAggregateProxy@0:SplitAggregateProxy
		SplitAggregateProxy@1:PROXY_INSEQ
		SplitAggregateProxy@2:IterateMediator
		SplitAggregateProxy@3:SendMediator
		SplitAggregateProxy@4:AnonymousEndpoint
		SplitAggregateProxy@5:PROXY_OUTSEQ
		SplitAggregateProxy@6:AggregateMediator
		SplitAggregateProxy@7:SendMediator
		 */
		Assert.assertTrue("Entry SplitAggregateProxy@0:SplitAggregateProxy not exists in statistics event",
		                  jsonString.contains("SplitAggregateProxy@0:SplitAggregateProxy"));
		Assert.assertTrue("Entry plitAggregateProxy@1:PROXY_INSEQ not exists in statistics event",
		                  jsonString.contains("plitAggregateProxy@1:PROXY_INSEQ"));
		Assert.assertTrue("Entry SplitAggregateProxy@2:IterateMediator not exists in statistics event",
		                  jsonString.contains("SplitAggregateProxy@2:IterateMediator"));
		Assert.assertTrue("Entry SplitAggregateProxy@3:SendMediator not exists in statistics event",
		                  jsonString.contains("SplitAggregateProxy@3:SendMediator"));
		Assert.assertTrue("Entry SplitAggregateProxy@4:AnonymousEndpoint not exists in statistics event",
		                  jsonString.contains("SplitAggregateProxy@4:AnonymousEndpoint"));
		Assert.assertTrue("Entry SplitAggregateProxy@5:PROXY_OUTSEQ not exists in statistics event",
		                  jsonString.contains("SplitAggregateProxy@5:PROXY_OUTSEQ"));
		Assert.assertTrue("Entry SplitAggregateProxy@6:AggregateMediator not exists in statistics event",
		                  jsonString.contains("SplitAggregateProxy@6:AggregateMediator"));
		Assert.assertTrue("Entry SplitAggregateProxy@7:SendMediator not exists in statistics event",
		                  jsonString.contains("SplitAggregateProxy@7:SendMediator"));
	}

	@AfterClass(alwaysRun = true) public void cleanupArtifactsIfExist() throws Exception {
		statisticsEnableAdminClient = new StatisticsEnableAdminClient(contextUrls.getBackEndUrl(), getSessionCookie());
		statisticsEnableAdminClient.removeAllStatisticsConfiguration();
		thriftServer.stop();
		super.cleanup();
		serverConfigurationManager.restoreToLastConfiguration();
	}
}
