package org.wso2.carbon.esb.passthru.transport.test;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.util.SimpleSocketServer;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.JSONClient;

public class ESBJAVA_4325_SettingMessageBuilderInvokedWhenContentTypeEmpty extends
                                                                          ESBIntegrationTest {
    private static final String EXPECTED_ERROR_MESSAGE =
                                                         "Could not save JSON payload. Invalid input stream found";
    private JSONClient jsonclient;
    private LogViewerClient logViewerClient;
    private SimpleSocketServer simpleSocketServer;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        jsonclient = new JSONClient();
        loadESBConfigurationFromClasspath("/artifacts/ESB/synapseconfig/emptycontenttype/TryReplay.xml");
    }

    @Test(groups = { "wso2.esb" }, description = "Test whether the msg builder invoked property is set when the content type is empty")
    public void testMsgBuilderInvokedPropertyWhenContentTypeisEmpty() throws InterruptedException,
                                                                     RemoteException {
        boolean isErrorFound = false;
        try {
            int port = 8090;
            String expectedResponse =
                    "HTTP/1.0 200 OK\r\n" +
                            "Server: CERN/3.0 libwww/2.17\r\n" +
                            "Date: Tue, 16 Nov 1994 08:12:31 GMT\r\n" +
                            "\r\n" + "<HTML>\n" + "<!DOCTYPE HTML PUBLIC " +
                            "\"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
                            "<HEAD>\n" + " <TITLE>Test Server Results</TITLE>\n" +
                            "</HEAD>\n" + "\n" + "<BODY BGCOLOR=\"#FDF5E6\">\n" +
                            "<H1 ALIGN=\"CENTER\"> Results</H1>\n" +
                            "Here is the request line and request headers\n" +
                            "sent by your browser:\n" + "<PRE>";
            simpleSocketServer = new SimpleSocketServer(port, expectedResponse);
            simpleSocketServer.start();
            
            
            final String jsonPayload = "{\"album\":\"Hello\",\"singer\":\"Peter\"}";
            String apiEp = getApiInvocationURL("orders/sources/ops");
            jsonclient.sendUserDefineRequest(apiEp, jsonPayload.trim()).toString();

        } catch (AxisFault e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            logViewerClient = new LogViewerClient(contextUrls.getBackEndUrl(), getSessionCookie());

            // Wait till the log appears
            Thread.sleep(20000);
            LogEvent[] logs = logViewerClient.getAllSystemLogs();
            for (LogEvent logEvent : logs) {
                String message = logEvent.getMessage();
                if (message.contains(EXPECTED_ERROR_MESSAGE)) {
                    isErrorFound = true;
                }
            }
            Assert.assertTrue(!isErrorFound);
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
