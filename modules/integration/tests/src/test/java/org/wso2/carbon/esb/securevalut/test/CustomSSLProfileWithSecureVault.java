/**
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.securevalut.test;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.esb.ESBIntegrationTest;

import java.io.*;

import static java.io.File.separator;
import static org.testng.Assert.assertTrue;

public class CustomSSLProfileWithSecureVault extends ESBIntegrationTest {
    private ServerConfigurationManager serverConfigurationManagerAxis2;
    private ServerConfigurationManager serverConfigurationManagerCipherText;
    private ServerConfigurationManager serverConfigurationManagerCipherTool;
    private ServerConfigurationManager serverConfigurationManagerSecretConf;
    private ServerConfigurationManager serverConfigurationManagerKeyStore;
    private ServerConfigurationManager serverConfigurationManagerTrustStore;
    private ServerConfigurationManager serverConfigurationManagerPasswordTmp;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();

        serverConfigurationManagerCipherText = new ServerConfigurationManager(esbServer.getBackEndUrl());
        String sourceCText = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME) +
                separator + "synapseconfig" + separator + "customSSLprofileWithsecurevault" + separator
                + "cipher-text.properties";

        String targetCText = CarbonBaseUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "security" + File.separator + "cipher-text.properties";

        File sourceFileCText = new File(sourceCText);
        File targetFileCText = new File(targetCText);
        serverConfigurationManagerCipherText.applyConfigurationWithoutRestart(sourceFileCText, targetFileCText, true);

        serverConfigurationManagerCipherTool = new ServerConfigurationManager(esbServer.getBackEndUrl());
        String sourceCTool = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME) +
                separator + "synapseconfig" + separator + "customSSLprofileWithsecurevault" + separator
                + "cipher-tool.properties";

        String targetCTool = CarbonBaseUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "security" + File.separator + "cipher-tool.properties";

        File sourceFileCTool = new File(sourceCTool);
        File targetFileCTool = new File(targetCTool);
        serverConfigurationManagerCipherTool.applyConfigurationWithoutRestart(sourceFileCTool, targetFileCTool, true);


        serverConfigurationManagerSecretConf = new ServerConfigurationManager(esbServer.getBackEndUrl());
        String sourceSecret = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME) +
                separator + "synapseconfig" + separator + "customSSLprofileWithsecurevault" + separator
                + "secret-conf.properties";

        String targetSecret = CarbonBaseUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "security" + File.separator + "secret-conf.properties";

        File sourceFileSecret = new File(sourceSecret);
        File targetFileSecret = new File(targetSecret);
        serverConfigurationManagerSecretConf.applyConfigurationWithoutRestart(sourceFileSecret, targetFileSecret, true);

        serverConfigurationManagerTrustStore = new ServerConfigurationManager(esbServer.getBackEndUrl());
        String sourceTrust = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME) +
                separator + "synapseconfig" + separator + "customSSLprofileWithsecurevault" + separator
                + "client-truststore.jks";

        String targetTrust = CarbonBaseUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" + File.separator + "security" + File.separator + "client-truststore.jks";

        File sourceFileTrust = new File(sourceTrust);
        File targetFileTrust = new File(targetTrust);
        serverConfigurationManagerTrustStore.applyConfigurationWithoutRestart(sourceFileTrust, targetFileTrust, true);

        serverConfigurationManagerKeyStore = new ServerConfigurationManager(esbServer.getBackEndUrl());
        String sourceKey = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME) +
                separator + "synapseconfig" + separator + "customSSLprofileWithsecurevault" + separator
                + "wso2carbon.jks";

        String targetKey = CarbonBaseUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";

        File sourceFileKey = new File(sourceKey);
        File targetFileKey = new File(targetKey);
        serverConfigurationManagerKeyStore.applyConfigurationWithoutRestart(sourceFileKey, targetFileKey, true);

        serverConfigurationManagerAxis2 = new ServerConfigurationManager(esbServer.getBackEndUrl());
        String sourceAxis2 = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME) +
                separator + "synapseconfig" + separator + "customSSLprofileWithsecurevault" + separator
                + "axis2.xml";

        String targetAxis2 = CarbonBaseUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "conf" + File.separator + "axis2" + File.separator + "axis2.xml";

        File sourceFileAxis2 = new File(sourceAxis2);
        File targetFileAxis2 = new File(targetAxis2);
        serverConfigurationManagerAxis2.applyConfigurationWithoutRestart(sourceFileAxis2, targetFileAxis2, true);


        serverConfigurationManagerPasswordTmp = new ServerConfigurationManager(esbServer.getBackEndUrl());
        String sourcePassTmp = ProductConstant.getResourceLocations(ProductConstant.ESB_SERVER_NAME) +
                separator + "synapseconfig" + separator + "customSSLprofileWithsecurevault" + separator
                + "password-tmp";

        String targetPassTmp = CarbonBaseUtils.getCarbonHome() + separator + "password-tmp";

        File sourceFilePassTmp = new File(sourcePassTmp);
        File targetFilePassTmp = new File(targetPassTmp);
        serverConfigurationManagerPasswordTmp.applyConfigurationWithoutRestart(sourceFilePassTmp, targetFilePassTmp, false);
        serverConfigurationManagerPasswordTmp.restartGracefully();

        super.init();
    }


    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.esb", description = "Tests Secure Vault Protected Custom SSL Profiles")
    public void testCustomSSLProfileWithSecureVaultPasswords() throws InterruptedException {
        String logString = "java.io.IOException: Keystore was tampered with, or password was incorrect";

        try {
            BufferedReader bf = new BufferedReader(new FileReader(CarbonBaseUtils.getCarbonHome() + File.separator + "repository" + File.separator + "logs" + File.separator + "wso2carbon.log"));

            String line;
            boolean found = false;
            while (( line = bf.readLine()) != null) {
                int indexfound = line.indexOf(logString);
                if (indexfound > -1) {
                    found = true;
                    break;
                }
            }
            assertTrue(!found, "Server started successfully with Custom SSL Profile using Secure Vault.");

            bf.close();
        } catch (FileNotFoundException e) {
            assertTrue(true, "wso2carbon.log file not found.");
        } catch (IOException e) {
            assertTrue(true, "Error reading log file wso2carbon.log");
        }
    }



    /**
     * Replace custom files.
     *
     * @throws Exception
     */
    @AfterClass(alwaysRun = true)
    public void atEnd() throws Exception {
        try {
            super.cleanup();
        } finally {
            Thread.sleep(3000);
            serverConfigurationManagerCipherText.restoreToLastConfiguration();
            serverConfigurationManagerCipherText = null;

            serverConfigurationManagerCipherTool.restoreToLastConfiguration();
            serverConfigurationManagerCipherTool = null;

            serverConfigurationManagerSecretConf.restoreToLastConfiguration();
            serverConfigurationManagerSecretConf = null;

            serverConfigurationManagerKeyStore.restoreToLastConfiguration();
            serverConfigurationManagerKeyStore = null;

            serverConfigurationManagerTrustStore.restoreToLastConfiguration();
            serverConfigurationManagerTrustStore = null;

            serverConfigurationManagerPasswordTmp = null;

            serverConfigurationManagerAxis2.restoreToLastConfiguration();
            serverConfigurationManagerAxis2 = null;
        }
    }
}
