/**
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.connector.integration.test.twitter;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import javax.activation.DataHandler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.context.ConfigurationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.proxy.admin.ProxyServiceAdminClient;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.automation.utils.axis2client.ConfigurationContextProvider;
import org.wso2.carbon.connector.integration.test.common.ConnectorIntegrationUtil;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.mediation.library.stub.MediationLibraryAdminServiceStub;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;

public class TwitterConnectorIntegrationTest extends ESBIntegrationTest {

	private MediationLibraryUploaderStub mediationLibUploadStub = null;

	private MediationLibraryAdminServiceStub adminServiceStub = null;

	private ProxyServiceAdminClient proxyAdmin;

	private String repoLocation = null;

	private Properties twtitterConnectorProperties = null;

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		super.init();

		ConfigurationContextProvider configurationContextProvider =
		                                                            ConfigurationContextProvider.getInstance();
		ConfigurationContext cc = configurationContextProvider.getConfigurationContext();
		mediationLibUploadStub = new MediationLibraryUploaderStub(cc, esbServer.getBackEndUrl() +
		                                                              "MediationLibraryUploader");
		AuthenticateStub.authenticateStub("admin", "admin", mediationLibUploadStub);

		adminServiceStub = new MediationLibraryAdminServiceStub(cc, esbServer.getBackEndUrl() +
		                                                            "MediationLibraryAdminService");

		AuthenticateStub.authenticateStub("admin", "admin", adminServiceStub);

		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			repoLocation = System.getProperty("connector_repo").replace("/", "\\");
		} else {
			repoLocation = System.getProperty("connector_repo").replace("/", "/");
		}

		proxyAdmin =  new ProxyServiceAdminClient(esbServer.getBackEndUrl(),
		                                         esbServer.getSessionCookie());

		// this.uploadConnector();
		ConnectorIntegrationUtil.uploadConnector(repoLocation, mediationLibUploadStub,
		                                         "twitter-connector.zip");
		Thread.sleep(30000);
		adminServiceStub.updateStatus("{org.wso2.carbon.connectors}twitter", "twitter",
		                              "org.wso2.carbon.connectors", "enabled");

		twtitterConnectorProperties = ConnectorIntegrationUtil.getConnectorConfigProperties("twitter");
	}

	@Override
	protected void cleanup() {
		axis2Client.destroy();
	}

	@Test(groups = { "wso2.esb" }, description = "Twitter {Search} integration test.")
	public void testTwitterSearch() throws Exception {

		AxisServiceClient axisServiceClient = new AxisServiceClient();

		proxyAdmin.addProxyService(new DataHandler(
		                                           new URL(
		                                                   "file:" +
		                                                           File.separator +
		                                                           File.separator +
		                                                           ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION +
		                                                           "artifacts/ESB/config/TwitterSearchProxy.xml")));

		OMElement getRequest =
		                       AXIOMUtil.stringToOM("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:per=\"http://connector.esb.wso2.org\">\n" +
		                                            "   <soapenv:Header/>\n" +
		                                            "   <soapenv:Body>\n" +
		                                            "   <per:conifg>\n" +
		                                            "   <per:consumerSecret>" +
		                                            twtitterConnectorProperties.get("consumerSecret") +
		                                            "</per:consumerSecret>\n" +
		                                            "   <per:accessTokenSecret>" +
		                                            twtitterConnectorProperties.get("accessTokenSecret") +
		                                            "</per:accessTokenSecret>\n" +
		                                            "   <per:accessToken>" +
		                                            twtitterConnectorProperties.get("accessToken") +
		                                            "</per:accessToken>\n" +
		                                            "   <per:consumerKey>" +
		                                            twtitterConnectorProperties.get("consumerKey") +
		                                            "</per:consumerKey>\n" +
		                                            "   </per:conifg>\n" +
		                                            "   </soapenv:Body>\n" + "</soapenv:Envelope>");
		OMElement response =
		                     axisServiceClient.sendReceive(getRequest,"http://localhost:8281/services/TwitterProxy2",
		                                                   //getProxyServiceURL("TwitterProxy2"),
		                                                   "mediate");
		System.out.println(twtitterConnectorProperties.get("consumerSecret"));
		Assert.assertTrue(response.toString().contains("<jsonObject><statuses><metadata><result_type>"));

	}

}
