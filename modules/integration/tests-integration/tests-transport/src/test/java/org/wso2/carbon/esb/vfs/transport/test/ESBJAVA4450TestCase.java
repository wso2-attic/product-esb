package org.wso2.carbon.esb.vfs.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.automation.extensions.servers.ftpserver.FTPServerManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.esb.integration.common.utils.clients.SimpleHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Related to https://wso2.org/jira/browse/ESBJAVA-3430 This class tests whether
 * the null check for replyFile.getParent() in VFSTransportSender is available
 */
public class ESBJAVA4450TestCase extends ESBIntegrationTest {

    private FTPServerManager ftpServerManager;
    private String FTPUsername;
    private String FTPPassword;
    private File FTPFolder;
    private File inputFolder;
    private ServerConfigurationManager serverConfigurationManager;
    private LogViewerClient logViewerClient;
    private String pathToFtpDir;

    @BeforeClass(alwaysRun = true)
    public void runFTPServer() throws Exception {

        // Username password for the FTP server to be started
        FTPUsername = "admin";
        FTPPassword = "admin";
        String inputFolderName = "in";
        int FTPPort = 8085;

        pathToFtpDir = getClass().getResource(
                File.separator + "artifacts" + File.separator + "ESB" + File.separator + "synapseconfig" + File.separator
                        + "vfsTransport" + File.separator).getPath();

        // Local folder of the FTP server root
        FTPFolder = new File(pathToFtpDir + "FTP_Location" + File.separator);

        // create FTP server root folder if not exists
        if (FTPFolder.exists()) {
            FileUtils.deleteDirectory(FTPFolder);
        }
        Assert.assertTrue(FTPFolder.mkdir(), "FTP root file folder not created");

        // create a directory under FTP server root
        inputFolder = new File(FTPFolder.getAbsolutePath() + File.separator
                + inputFolderName);

        if (inputFolder.exists()) {
            inputFolder.delete();
        }
        inputFolder.mkdir();


        // start-up FTP server
        ftpServerManager = new FTPServerManager(FTPPort,
                FTPFolder.getAbsolutePath(), FTPUsername, FTPPassword);
        ftpServerManager.startFtpServer();

        super.init();
        // replace the axis2.xml enabled vfs transfer and restart the ESB server
        // gracefully
        serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.applyConfiguration(new File(getClass()
                .getResource(
                        File.separator + "artifacts" + File.separator + "ESB" + File.separator + "synapseconfig"
                                + File.separator + "vfsTransport" + File.separator + "axis2.xml").getPath()));
        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB" + File.separator +
                "synapseconfig" + File.separator + "vfsTransport" + File.separator + "vfs_file_type.xml");

        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(),
                getSessionCookie());

    }

    @AfterClass(alwaysRun = true)
    public void stopFTPServer() throws Exception {
        try {
            super.cleanup();
        } finally {
            Thread.sleep(3000);
            ftpServerManager.stop();
            log.info("FTP Server stopped successfully");
            serverConfigurationManager.restoreToLastConfiguration();

        }

    }

    @Test(groups = "wso2.esb", description = "VFS NPE in Creating a File in FTP directly in root directory")
    public void TestCreateFileInRoot() throws Exception {

        // To check the timed out exception happened
        boolean timeout = false;
        // To check whether the NPE happened
        boolean isError = false;

        try {
//            OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("VFSProxyBinary"), null,
//                    "WSO2");
            SimpleHttpClient httpClient = new SimpleHttpClient();
            String requestXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">\n" +
                    "   <soapenv:Header/>\n" +
                    "   <soapenv:Body>\n" +
                    "      <ser:getQuote>\n" +
                    "         <!--Optional:-->\n" +
                    "         <ser:request>\n" +
                    "            <!--Optional:-->\n" +
                    "            <xsd:symbol>IBM</xsd:symbol>\n" +
                    "         </ser:request>\n" +
                    "      </ser:getQuote>\n" +
                    "   </soapenv:Body>\n" +
                    "</soapenv:Envelope>";
            String requestContentType = "text/xml";
            HttpResponse httpResponse = httpClient.doPatch(contextUrls.getServiceUrl() + "/VFSProxyBinary", null, requestXML, requestContentType);
            //HttpResponse httpResponse = httpClient.doPost(contextUrls.getServiceUrl().replace("/services/VFSProxyBinary", "") + "/test", null,requestXML ,requestContentType);
        } catch (Exception axisFault) {
            //We expect this as no response is sent to the backend
        } finally {
            removeProxy("VFSProxyBinary");
        }

        try {
//            OMElement response = axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("VFSProxyASCII"), null,
//                    "WSO2");
            SimpleHttpClient httpClient = new SimpleHttpClient();
            String requestXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">\n" +
                    "   <soapenv:Header/>\n" +
                    "   <soapenv:Body>\n" +
                    "      <ser:getQuote>\n" +
                    "         <!--Optional:-->\n" +
                    "         <ser:request>\n" +
                    "            <!--Optional:-->\n" +
                    "            <xsd:symbol>IBM</xsd:symbol>\n" +
                    "         </ser:request>\n" +
                    "      </ser:getQuote>\n" +
                    "   </soapenv:Body>\n" +
                    "</soapenv:Envelope>";
            String requestContentType = "text/xml";
            HttpResponse httpResponse = httpClient.doPatch(contextUrls.getServiceUrl() + "/VFSProxyASCII", null, requestXML, requestContentType);
        } catch (Exception axisFault) {
            //We expect this as no response is sent to the backend
        } finally {
            removeProxy("VFSProxyASCII");
        }

        Path binaryFilePath = Paths.get(FTPFolder.getAbsolutePath() + File.separator + "in" + File.separator + "binary.xml");
        Path asciiFilePath = Paths.get(FTPFolder.getAbsolutePath() + File.separator + "in" + File.separator + "ascii.xml");

        try {
            byte[] binaryFileData = Files.readAllBytes(binaryFilePath);
            byte[] asciiFileData = Files.readAllBytes(asciiFilePath);

            Assert.assertEquals(binaryFileData.length, asciiFileData.length, "File size should be different, " +
                    "but found equal sizes");
        } catch (IOException e) {
            Assert.fail("Error while trying to read files written using vfs");
        }

    }

//    @Test(groups = "wso2.esb", description = "VFS NPE in Creating a File in FTP, in a directory under root")
//    public void TestCreateFileInDirectoryUnderRoot() throws Exception {
//
//        // To check the timed out exception happened
//        boolean timeout = false;
//        // To check whether the NPE happened
//        boolean isError = false;
//
//        try {
//            OMElement response = axis2Client.sendSimpleStockQuoteRequest(
//                    getProxyServiceURLHttp("VFSProxyFileCreateInDirectory"),
//                    null, "WSO2");
//        } catch (AxisFault axisFault) {
//            if (axisFault.getLocalizedMessage().contains("Read timed out")) {
//                timeout = true;
//            }
//        } finally {
//            removeProxy("VFSProxyFileCreateInDirectory");
//        }
//
//        LogEvent[] logs = logViewerClient.getAllSystemLogs();
//
//        for (LogEvent logEvent : logs) {
//            String message = logEvent.getMessage();
//            if (message.contains("Error creating file under the FTP root")) {
//                isError = true;
//                break;
//            }
//        }
//
//        Assert.assertFalse(
//                isError && timeout,
//                " The null check for the replyFile.getParent() in VFSTransportSender is not available");
//    }

    /**
     * @param proxyName - Name of the proxy to be removed
     * @throws Exception
     */
    private void removeProxy(String proxyName) throws Exception {
        deleteProxyService(proxyName);
    }

}
