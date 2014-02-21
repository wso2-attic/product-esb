package org.wso2.carbon.proxyservices.test.util;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.RampartMessageData;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.ws.security.WSPasswordCallback;
import org.wso2.carbon.integration.core.FrameworkSettings;


import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.CallbackHandler;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SecurityClient implements CallbackHandler {

    public OMElement runSecurityClient(int scenarioNo, String serviceName, String soapAction,
                                       String body) throws Exception {

        FrameworkSettings.getProperty();
        String clientRepo = null;
        String trustStore = null;
        String endpointHttpS = null;
        String endpointHttp = null;
        String securityPolicy = null;
        String clientKey = null;

        File filePath = new File("./");
        String relativePath = filePath.getCanonicalPath();
        File findFile = new File(relativePath + File.separator + "config" + File.separator + "framework.properties");
        if (!findFile.isFile()) {
            filePath = new File("./../");
            relativePath = filePath.getCanonicalPath();
        }


        /*Properties properties = new Properties();
        FileInputStream freader = new FileInputStream(relativePath + File.separator + "proxyservices" + File.separator + "src" + File.separator + "test" + File.separator + "resources"+ File.separator + "ClientSecurityFiles" + File.separator + "client.properties");
        properties.load(freader);*/
        if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            clientRepo = FrameworkSettings.TEST_FRAMEWORK_HOME + File.separator + "lib" + File.separator + "stratos-artifacts" + File.separator + "client_repo";
            endpointHttpS = "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/"+ serviceName;
            endpointHttp = "http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/" + serviceName;
            clientKey = FrameworkSettings.TEST_FRAMEWORK_HOME + File.separator + "lib" + File.separator + "stratos-artifacts" + File.separator + "wso2carbon.jks";
            trustStore = FrameworkSettings.TEST_FRAMEWORK_HOME + File.separator + "lib" + File.separator + "stratos-artifacts" + File.separator + "wso2carbon.jks";
            securityPolicy = relativePath + File.separator + "proxyservices" + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "ClientSecurityFiles";

        }
        else if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            clientRepo = FrameworkSettings.CARBON_HOME + File.separator + "samples" + File.separator + "axis2Client" + File.separator + "client_repo";
            endpointHttpS = "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/" + serviceName;
            endpointHttp = "http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + serviceName;
            clientKey = FrameworkSettings.CARBON_HOME + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
            trustStore = FrameworkSettings.CARBON_HOME + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
            securityPolicy = relativePath + File.separator + "proxyservices" + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "ClientSecurityFiles";

        }
        OMElement result;


        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(clientRepo, null);
        ServiceClient sc = new ServiceClient(ctx, null);
        sc.engageModule("rampart");
        sc.engageModule("addressing");

        Options opts = new Options();

        if (scenarioNo == 1) {
            opts.setTo(new EndpointReference(endpointHttpS));
        }
        else {
            opts.setTo(new EndpointReference(endpointHttp));
        }

        opts.setAction(soapAction);

        if (scenarioNo != 0) {
            try {
                String securityPolicyPath = securityPolicy + File.separator + "scenario" + scenarioNo + "-policy.xml";
                opts.setProperty(RampartMessageData.KEY_RAMPART_POLICY, loadPolicy(securityPolicyPath, clientKey));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        sc.setOptions(opts);
        result = sc.sendReceive(AXIOMUtil.stringToOM(body));
        System.out.println(result.getFirstElement().getText());
        return result;
    }

    public Policy loadPolicy(String xmlPath, String clientKey) throws Exception {

        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        Policy policy = PolicyEngine.getPolicy(builder.getDocumentElement());

        RampartConfig rc = new RampartConfig();

        rc.setUser("admin");
        rc.setUserCertAlias("wso2carbon");
        rc.setEncryptionUser("wso2carbon");
        rc.setPwCbClass(SecurityClient.class.getName());

        CryptoConfig sigCryptoConfig = new CryptoConfig();
        sigCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

        Properties prop1 = new Properties();
        prop1.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        prop1.put("org.apache.ws.security.crypto.merlin.file", clientKey);
        prop1.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");
        sigCryptoConfig.setProp(prop1);

        CryptoConfig encrCryptoConfig = new CryptoConfig();
        encrCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

        Properties prop2 = new Properties();
        prop2.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        prop2.put("org.apache.ws.security.crypto.merlin.file", clientKey);
        prop2.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");
        encrCryptoConfig.setProp(prop2);

        rc.setSigCryptoConfig(sigCryptoConfig);
        rc.setEncrCryptoConfig(encrCryptoConfig);

        policy.addAssertion(rc);
        return policy;
    }


    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[0];
        String id = pwcb.getIdentifier();
        int usage = pwcb.getUsage();

        if (usage == WSPasswordCallback.USERNAME_TOKEN) {

            if ("admin".equals(id)) {
                pwcb.setPassword("admin");
            }

        }
        else if (usage == WSPasswordCallback.SIGNATURE || usage == WSPasswordCallback.DECRYPT) {

            if ("wso2carbon".equals(id)) {
                pwcb.setPassword("wso2carbon");
            }
        }
    }
}