package org.wso2.carbon.esb.mediator.test.smooks;

import java.io.File;
import java.net.URL;

import javax.activation.DataHandler;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.clients.registry.ResourceAdminServiceClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

/**
 * This class tests the XML to Xml transformation scenario via Smooks mediator
 * in WSO2 ESB.
 *
 */
public class ESBJAVA3608_SmooksMediatorXmlToXmlTransformationTestCase extends ESBIntegrationTest {
	private ServerConfigurationManager serverConfigurationManager;
	private ResourceAdminServiceClient resourceAdminServiceStub;
	private LogViewerClient logViewerClient;
	private final String COMMON_FILE_LOCATION = getClass().getResource(File.separator + "artifacts" + File.separator + "ESB" + File.separator + "synapseconfig" + File.separator + "vfsTransport" + File.separator).getPath();

	private boolean isProxyDeployed = false;

	 @BeforeTest(alwaysRun = true)
	protected void init() throws Exception {
		super.init();
		
		resourceAdminServiceStub = new ResourceAdminServiceClient(contextUrls.getBackEndUrl(), userInfo.getUserName(), userInfo.getPassword());
		serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.applyConfiguration(new File(COMMON_FILE_LOCATION + "axis2.xml"));
        super.init();
        uploadResourcesToConfigRegistry();
		loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/smooks/smooks_xml_to_xml_transformation_config.xml");
		addVFSProxy();
		logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());
	}
	
	private void uploadResourcesToConfigRegistry() throws Exception {
		resourceAdminServiceStub.deleteResource("/_system/config/smooks");
		resourceAdminServiceStub.addCollection("/_system/config/", "smooks", "",
		                                       "Contains smooks config files");
		resourceAdminServiceStub.addResource("/_system/config/smooks/smooks_config_xml_to_xml_test.xml",
		                                     "application/xml", "xml files",
		                                     new DataHandler(new URL("file:///" +
		                                                             COMMON_FILE_LOCATION +
		                                                             "smooks_config_xml_to_xml_test.xml")));
	}
	
    private void addVFSProxy() throws Exception {

        addProxyService(AXIOMUtil.stringToOM("<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"smook_proxy\" transports=\"vfs\">\n" +
                                             "        <target>\n" +
                                             "            <inSequence>\n" +
                                             "			      <property name=\"FORCE_SC_ACCEPTED\" value=\"true\" scope=\"axis2\" type=\"STRING\" />" +
                                             "				  <property name=\"OUT_ONLY\" value=\"true\" scope=\"default\" type=\"STRING\" />" +
                                             " 			  	  <log level=\"full\" />" +
                                             "                <smooks config-key=\"conf:/smooks/smooks_config_xml_to_xml_test.xml\">\n" +
                                             "                    <input type=\"xml\"/>\n" + 
                                             "                    <output type=\"xml\"/>\n" +
                                             "                </smooks>\n" +
                                             "                <log level=\"full\"/>\n" +
                                             "            </inSequence>\n" +
                                             "        </target>\n" +
                                             "        <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n" +
                                             "        <!--CHANGE-->\n" +
                                             "        <parameter name=\"transport.vfs.FileURI\">file://" + COMMON_FILE_LOCATION + "test" + File.separator + "in" + File.separator + "</parameter>\n" +
                                             "        <parameter name=\"transport.vfs.ContentType\">application/xml</parameter>\n" +
                                             "        <parameter name=\"transport.vfs.FileNamePattern\">.*\\.xml</parameter>\n" +
                                             "        <parameter name=\"transport.PollInterval\">5</parameter>\n" +
                                             "        <!--CHANGE-->\n" +
                                             "        <parameter name=\"transport.vfs.MoveAfterProcess\">file://" + COMMON_FILE_LOCATION + "test" + File.separator + "out" + File.separator + "</parameter>\n" +
                                             "        <!--CHANGE-->\n" +
                                             "        <parameter name=\"transport.vfs.MoveAfterFailure\">file://" + COMMON_FILE_LOCATION + "test" + File.separator + "out" + File.separator + "</parameter>\n" +
                                             "        <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n" +
                                             "        <parameter name=\"transport.vfs.ActionAfterFailure\">MOVE</parameter>\n" +
                                             "    </proxy>\n"));
        isProxyDeployed = true;
    }
    
	@Test(groups = { "wso2.esb" }, description = "XML to XML transformation using smooks mediator")
	public void testXMLtoXMLTransformationUsingSmooksMeidator() throws Exception {
		new File(COMMON_FILE_LOCATION + "test" + File.separator + "out" + File.separator).mkdir();
		new File(COMMON_FILE_LOCATION + "test" + File.separator + "xmlOut" + File.separator).mkdir();
		Thread.sleep(2000);
		File afile = new File(COMMON_FILE_LOCATION + File.separator + "input.xml");
		File bfile =
		             new File(COMMON_FILE_LOCATION + "test" + File.separator + "in" +
		                      File.separator + "input.xml");
		FileUtils.copyFile(afile, bfile);

		Thread.sleep(40000);

		LogEvent[] systemLogs = logViewerClient.getAllSystemLogs();

		boolean transformedSuccessfully = false;
		final String expectedContent =
		                               "<salesorder>";
		for (LogEvent logEvent : systemLogs) {
			if (logEvent.getMessage().contains(expectedContent)) {
				transformedSuccessfully = true;
				break;
			}
		}

		Assert.assertTrue(transformedSuccessfully, "Smooks xml to xml transformation error.");
	}
	
	@AfterClass(alwaysRun = true)
	public void restoreServerConfiguration() throws Exception {
		try {
			if (isProxyDeployed) {
				deleteProxyService("StockQuoteProxy");
			}
			resourceAdminServiceStub.deleteResource("/_system/config/smooks");
			super.cleanup();
		} finally {
			Thread.sleep(3000);
			serverConfigurationManager.restoreToLastConfiguration();
			resourceAdminServiceStub = null;
			serverConfigurationManager = null;

		}

	}

}
