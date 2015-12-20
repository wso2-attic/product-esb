package org.wso2.carbon.esb.file.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.clients.registry.ResourceAdminServiceClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

public class FileInboundWithDynamicSequenceTestCase extends ESBIntegrationTest {

	private LogViewerClient logViewerClient;
	private File InboundFileFolder;
	private String pathToDir;
	private ResourceAdminServiceClient resourceAdminServiceStub;

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		pathToDir = getClass().getResource(File.separator + "artifacts" + File.separator + "ESB"
						+ File.separator + "synapseconfig" + File.separator + "inboundEndpoint"
						+ File.separator).getPath();

		InboundFileFolder = new File(pathToDir + File.separator + "InboundInFileFolder");

		// create InboundFileFolder if not exists
		if (InboundFileFolder.exists()) {
			FileUtils.cleanDirectory(InboundFileFolder);
		} else {
			Assert.assertTrue(InboundFileFolder.mkdir(), "InboundFileFolder not created");
		}

		super.init();
		resourceAdminServiceStub = new ResourceAdminServiceClient(contextUrls.getBackEndUrl(), context.getContextTenant().getContextUser().getUserName()
				, context.getContextTenant().getContextUser().getPassword());
		uploadResourcesToGovernanceRegistry();

		loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB"
												  + File.separator + "synapseconfig" +
												  File.separator
												  + "inboundEndpoint" + File.separator +
												  "inboundFileWithDynamicSequence.xml");

		logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
	}

	@SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
	@Test(groups = "wso2.esb", description = "Tests sequences from  the governance registry with inbound endpoint")
	public void testSequence() throws Exception {
		addInboundEndpoint(addEndpoint1());
		// To check the file getting is read
		boolean isFileRead = false;

		File sourceFile = new File(pathToDir + File.separator + "test.xml");
		File targetFile = new File(InboundFileFolder + File.separator + "test.xml");
		if (!sourceFile.exists()) {
			sourceFile.createNewFile();
		}

		log.info("Copying files to the target folder");
		FileUtils.copyFile(sourceFile, targetFile);
		Thread.sleep(2000);


		LogEvent[] logs = logViewerClient.getAllRemoteSystemLogs();

		for (LogEvent logEvent : logs) {
			String message = logEvent.getMessage();
			if (message.contains("Working(Sequence Executed)")) {
				isFileRead = true;
			}
		}

		Assert.assertTrue(isFileRead, "The XML file is not getting read");

	}

	@AfterClass(alwaysRun = true)
	public void close() throws Exception {
		try{

			File sourceFile = new File(pathToDir + File.separator + "test.xml");
			if (sourceFile.exists()) {
				sourceFile.delete();
			}
			FileUtils.deleteDirectory(InboundFileFolder);
		}finally {
			super.cleanup();
		}


	}

	private OMElement addEndpoint1() throws Exception {
		OMElement synapseConfig = null;
		synapseConfig = AXIOMUtil
				.stringToOM(
						"<inboundEndpoint name=\"testFile1\" onError=\"inFault\" " + "protocol=\"file\"\n"
								+ " sequence=\"gov:/sendSequence\" suspend=\"false\" xmlns=\"http://ws.apache.org/ns/synapse\">\"\n"
								+ " <parameters>\n"
								+ " <parameter name=\"interval\">1000</parameter>\n"
								+ " <parameter name=\"transport.vfs.ActionAfterErrors\">DELETE</parameter>\n"
								+ " <parameter name=\"transport.vfs.Locking\">enable</parameter>\n"
								+ " <parameter name=\"transport.vfs.ContentType\">application/xml</parameter>\n"
								+ " <parameter name=\"transport.vfs.ActionAfterFailure\">DELETE</parameter>\n"
								+ " <parameter name=\"transport.vfs.ActionAfterProcess\">DELETE</parameter>\n"
								+ " <parameter name=\"transport.vfs.FileURI\">file://" + InboundFileFolder + "</parameter>\n"
								+ " </parameters>\n"
								+ "</inboundEndpoint>\n");

		return synapseConfig;
	}


	private void uploadResourcesToGovernanceRegistry() throws Exception {
		resourceAdminServiceStub.addResource("/_system/governance/sendSequence", "application/xml", "xml files",
				new DataHandler(new URL("file:///" + getClass().getResource(
						"/artifacts/ESB/synapseconfig/inboundEndpoint/inboundSequenceFile.xml").getPath())));
		Thread.sleep(2000);
	}


}
