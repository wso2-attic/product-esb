package org.wso2.carbon.esb.sequence.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.sequences.SequenceAdminServiceClient;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class SequenceXpathFunctionTestCase extends ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManager;
    private SequenceAdminServiceClient sequenceAdminServiceClient;
    private boolean isXpathFunctionSequenceExist = false;
    private final String XPATH_FUNC_SEQ_NAME = "testXpathFunctionSequence";


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {

        super.init();
        serverConfigurationManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        sequenceAdminServiceClient = new SequenceAdminServiceClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);

        File synapseProperties = new File(carbonHome + File.separator + "repository" + File.separator + "conf" +
                File.separator + "synapse.properties");

        applyProperty(synapseProperties, "synapse.streaming.xpath.enabled", "true");
        serverConfigurationManager.restartGracefully();
        super.init();
        sequenceAdminServiceClient = new SequenceAdminServiceClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());

    }

    @Test(groups = {"wso2.esb"}, description = "Test Xpath Function Sequence with streaming xpath enabled")
    public void xpathFunctionSequenceTest() throws Exception {

        OMElement sequence = AXIOMUtil.stringToOM("<sequence xmlns=\"http://ws.apache.org/ns/synapse\" name=\"" + XPATH_FUNC_SEQ_NAME + "\">" +
                "<filter xpath=\"fn:exists($body/test)\">" +
                "<drop/>" +
                "</filter>" +
                "</sequence>");

        sequenceAdminServiceClient.addSequence(sequence);
        isXpathFunctionSequenceExist = true;
        String[] s = sequenceAdminServiceClient.getSequences();
        List<String> list = new ArrayList(Arrays.asList(s));

        Assert.assertEquals(list.contains(XPATH_FUNC_SEQ_NAME), true, "Sequence has not been deployed");
    }

    /**
     * Apply the given property and restart the server to
     *
     * @param srcFile
     * @param key
     * @param value
     * @throws Exception
     */
    private void applyProperty(File srcFile, String key, String value) throws Exception {
        File destinationFile = new File(srcFile.getName());
        Properties properties = new Properties();
        properties.load(new FileInputStream(srcFile));
        properties.setProperty(key, value);
        properties.store(new FileOutputStream(destinationFile), null);
        serverConfigurationManager.applyConfigurationWithoutRestart(destinationFile);
    }

    /**
     * Remove the given property and restart the server to
     *
     * @param srcFile
     * @param key
     * @throws Exception
     */
    private void removeProperty(File srcFile, String key) throws Exception {
        File destinationFile = new File(srcFile.getName());
        Properties properties = new Properties();
        properties.load(new FileInputStream(srcFile));
        properties.remove(key);
        properties.store(new FileOutputStream(destinationFile), null);
        serverConfigurationManager.applyConfigurationWithoutRestart(destinationFile);
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        if (isXpathFunctionSequenceExist) {
            sequenceAdminServiceClient.deleteSequence(XPATH_FUNC_SEQ_NAME);
        }
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);

        File synapseProperties = new File(carbonHome + File.separator + "repository" + File.separator + "conf" +
                File.separator + "synapse.properties");

        removeProperty(synapseProperties, "synapse.streaming.xpath.enabled");
        super.cleanup();
        serverConfigurationManager.restoreToLastConfiguration();
    }
}
