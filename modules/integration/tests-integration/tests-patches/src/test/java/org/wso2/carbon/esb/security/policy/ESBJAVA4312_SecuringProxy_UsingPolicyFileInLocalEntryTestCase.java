package org.wso2.carbon.esb.security.policy;

import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.servers.httpserver.SimpleHttpClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public final class ESBJAVA4312_SecuringProxy_UsingPolicyFileInLocalEntryTestCase extends
                                                                             ESBIntegrationTest {
    private static final String PROXY_SERVICE_NAME = "SecPolicyWithLocalEntryProxy";

    @BeforeClass
    protected void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/secure/proxy/SecuringProxyWithSecPolicyInLocalEntry.xml");
        isProxyDeployed(PROXY_SERVICE_NAME);
    }

    @Test(groups = "wso2.esb", description = "Verifies whether a Proxy service can be secured using a policy file stored as a local entry.")
    public void testPolicyReferenceInWSDLBindings() throws IOException, InterruptedException {
        String epr = "http://localhost:8280/services/SecPolicyWithLocalEntryProxy?wsdl";
        final SimpleHttpClient httpClient = new SimpleHttpClient();
        HttpResponse response = httpClient.doGet(epr, null);
        Thread.sleep(4000);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        response.getEntity().writeTo(bos);
        String wsdlResponse = new String(bos.toByteArray());

        CharSequence expectedTag = "PolicyReference";
        Assert.assertTrue(wsdlResponse.contains(expectedTag));
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
