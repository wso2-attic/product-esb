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

package org.wso2.carbon.esb.cxf.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.cxfclient.CXFClient;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CXFInboundTransportTestCase extends ESBIntegrationTest {

	private SampleAxis2Server axis2Server;
	private CXFClient cxfClient;

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		super.init();

		axis2Server = new SampleAxis2Server("test_axis2_server_9000.xml");
		axis2Server.deployService(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
		axis2Server.start();

		loadCXFSpringConfiguration();
		addSequence(getArtifactConfig("RMIn.xml"));
		addSequence(getArtifactConfig("receiveSeq.xml"));
		addInboundEndpoint(getArtifactConfig("cxf_inbound_http.xml"));

		cxfClient = new CXFClient("http://localhost:8091/wsdl",
		                                    "http://wsrm.cxf.protocol.endpoint.inbound.carbon.wso2.org/",
		                                    "InboundRMServiceImplPort", "InboundRMServiceImpl");
	}

	private void loadCXFSpringConfiguration() throws Exception {
		String path = "artifacts" + File.separator + "ESB" + File.separator
		              + "cxf.inbound.transport" + File.separator + "cxf_server.xml";
		try {
			FileUtils.copyFile(new File(path), new File("repository/conf/cxf/server.xml"));
		} catch (IOException e) {
			throw new Exception("Could not copy CXF Spring configuration file", e);
		}
	}

	@Test(groups = "wso2.esb", description = "Inbound CXF ws-rm test case")
	public void inboundHttpTest() throws IOException, SOAPException {

		String str =
				"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
				"xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">\n" +
				" <soapenv:Header/>\n" +
				" <soapenv:Body>\n" +
				" <ser:getQuote>\n" +
				" <!--Optional:-->\n" +
				" <ser:request>\n" +
				" <!--Optional:-->\n" +
				" <xsd:symbol>IBM</xsd:symbol>\n" +
				" </ser:request>\n" +
				" </ser:getQuote>\n" +
				" </soapenv:Body>\n" +
				"</soapenv:Envelope>";

		SOAPMessage soapMessage = cxfClient.sendMessage(str);
		Assert.assertNotNull(soapMessage);
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		if (axis2Server.isStarted()) {
			axis2Server.stop();
		}
		axis2Server = null;
		cxfClient.destroy();
		super.cleanup();
	}

	private OMElement getArtifactConfig(String fileName) throws Exception {
		OMElement synapseConfig = null;
		String path = "artifacts" + File.separator + "ESB" + File.separator
		              + "cxf.inbound.transport" + File.separator + fileName;
		try {
			synapseConfig = esbUtils.loadResource(path);
		} catch (FileNotFoundException e) {
			throw new Exception("File Location " + path + " may be incorrect", e);
		} catch (XMLStreamException e) {
			throw new XMLStreamException("XML Stream Exception while reading file stream", e);
		}
		return synapseConfig;
	}
}
