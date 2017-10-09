package org.wso2.carbon.esb.compression.test.gzip;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;

import static org.testng.Assert.assertEquals;

public class ESBJAVA2575_ContentEncodingPostTest extends ESBIntegrationTest {
    private LogViewerClient logViewerClient;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void deployProxyServices() throws Exception {
        super.init();
        AutomationContext autoCtx = new AutomationContext("ESB", TestUserMode.SUPER_TENANT_ADMIN);
        serverConfigurationManager = new ServerConfigurationManager(autoCtx);
        serverConfigurationManager.applyConfiguration(new File(getClass().getResource("/artifacts/ESB/compression/gzip/axis2.xml").getPath()));
        super.init();
        loadESBConfigurationFromClasspath("artifacts/ESB/compression/gzip/content_encoding_gzip.xml");
        logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(),
                getSessionCookie());

    }

    @AfterClass(alwaysRun = true)
    public void unDeployProxyServices() throws Exception {
        // undeploying deployed artifact
        super.cleanup();
        serverConfigurationManager.restoreToLastConfiguration();
        serverConfigurationManager = null;
    }

    @Test(groups = {"wso2.esb"}, description = "POST request send without message body with content-encoding header set to gzip")
    public void sendingPOSTRequestWithContentEncodingGzipTest() throws Exception {
        String url = getApiInvocationURL("contentapi");
        DefaultHttpClient httpclient = new DefaultHttpClient();
        // Create a method instance.
        HttpPost post = new HttpPost(url);
        // Set request header
        post.addHeader("Content-type", "text/xml");
        post.addHeader("Content-Encoding", "gzip");
        boolean gzipException = false;
        org.apache.http.HttpResponse response = httpclient.execute(post);
        assertEquals(response.getStatusLine().getStatusCode(), 202, "response code doesn't match");
    }
}
