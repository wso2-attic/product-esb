package org.wso2.carbon.esb.json;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.esb.util.FileUtil;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class ESBJAVA_3698_MessageBuildingWithDifferentPayloadAndContentTypeTestCase extends
                                                                                   ESBIntegrationTest {
    private final DefaultHttpClient httpClient = new DefaultHttpClient();
    // private LogViewerClient logViewerClient;
    final String carbonLogFile = CarbonBaseUtils.getCarbonHome() +
                                 "/repository/logs/wso2carbon.log";
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();

        serverConfigurationManager = new ServerConfigurationManager(context);
        loadESBConfigurationFromClasspath("artifacts" + File.separator + "ESB" + File.separator +
                                          "json" + File.separator + "StockQuoteAPI.xml");
        // logViewerClient =
        // new LogViewerClient(esbServer.getBackEndUrl(),
        // esbServer.getSessionCookie());

        final String log4jSourceFile =
                                       System.getProperty("framework.resource.location") +
                                               "/artifacts/ESB/json/log4j.properties";
        final String log4jDestinationFile =
                                            CarbonBaseUtils.getCarbonHome() +
                                                    "/repository/conf/log4j.properties";

        serverConfigurationManager.applyConfiguration(new File(log4jSourceFile),
                                                      new File(log4jDestinationFile));
        super.init();
    }

    @Test(groups = { "wso2.esb" }, description = "Check for Axis Fault when xml payload is sent with application/json content type")
    public void testAxisFaultWithXmlPayloadAndJSONContentType() throws ClientProtocolException,
                                                               IOException, InterruptedException {
        final Map<String, String> headers = new HashMap<String, String>();

        final HttpPost post = new HttpPost("http://localhost:8280/stockquote/test");
        post.addHeader("Content-Type", "application/json");
        post.addHeader("SOAPAction", "urn:getQuote");
        StringEntity se = new StringEntity(getPayload());

        post.setEntity(se);
        HttpResponse response = httpClient.execute(post);

        Thread.sleep(2000);

        // boolean responseInLog = false;
        // LogEvent[] logs = logViewerClient.getAllSystemLogs();
        //
        // for (LogEvent logEvent : logs) {
        // String message = logEvent.getMessage();
        // if (message.contains("No JSON payload provided")) {
        // responseInLog = true;
        // break;
        // }
        // }

        // Asserting the results here.
        Assert.assertTrue(FileUtil.containsInFile(carbonLogFile, "org.apache.axis2.AxisFault: No JSON payload provided"),
                          "Expected SOAP Response was NOT found in the LOG stream.");

    }

    private String getPayload() {
        final String payload =
                               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://services.samples/xsd\" xmlns:ser=\"http://services.samples\">"
                                       + "<soapenv:Header/>"
                                       + "<soapenv:Body>"
                                       + "<ser:getQuote>"
                                       + "<ser:request>"
                                       + "<xsd:symbol>IBM</xsd:symbol>"
                                       + "</ser:request>"
                                       + "</ser:getQuote>"
                                       + "</soapenv:Body>"
                                       + "</soapenv:Envelope>";
        return payload;
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
        // reverting the changes done to esb sever
        serverConfigurationManager.restoreToLastConfiguration();
        serverConfigurationManager = null;
    }

}
