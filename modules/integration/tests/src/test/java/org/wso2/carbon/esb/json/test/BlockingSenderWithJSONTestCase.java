package org.wso2.carbon.esb.json.test;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.logging.LogViewerClient;
import org.wso2.carbon.automation.core.utils.httpserverutils.SimpleHttpClient;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class BlockingSenderWithJSONTestCase extends ESBIntegrationTest {
	private static final String url1 = "http://localhost:8280/services/tesforwardingprocessor";
	private static final String url2 = "http://localhost:8280/services/testcallout";
	private static final String logLine0 =
	                                       "[testcallout]REQUEST-JSON = {\"request\":{\"responsefield\":\"YES\",\"idfield\":\"10203040\"}}";
	private static final String logLine1 =
	                                       "[loganddrop]REPLY-JSON = {\"RESPONSE\":\"YES\", \"ID\":10203040, \"ID_STR\":\"10203040\"}";
	private static final String logLine2 =
	                                       "[testcallout]REPLY-JSON = {\"RESPONSE\":\"YES\", \"ID\":10203040, \"ID_STR\":\"10203040\"}";
	private final SimpleHttpClient httpClient = new SimpleHttpClient();
	private final Map<String, String> headers = new HashMap<String, String>(1);
	private final String payload =
	                               "{\"request\":{\"responsefield\":\"YES\",\"idfield\":\"10203040\"}}";

	private LogViewerClient logViewer;

	@BeforeClass(alwaysRun = true)
	protected void init() throws Exception {
		super.init();
		headers.put("Content-Type", "application/json");
		loadESBConfigurationFromClasspath("/artifacts/ESB/json/blockingSenderWithJson.xml");
		Thread.sleep(5000);
		logViewer = new LogViewerClient(esbServer.getBackEndUrl(), esbServer.getSessionCookie());
	}

	@Test(groups = "wso2.esb", description = "JSON with Blocking sender used in Forwarding message processor, and Callout.")
	public void testJsonWithForwardingProcessorAndCallout() throws Exception {
		HttpResponse response = httpClient.doPost(url2, headers, payload, "application/json");
		Thread.sleep(2000);
		assertEquals(response.getStatusLine().getStatusCode(), 200);
		String expectedPayload = "{\"RESPONSE\":\"YES\", \"ID\":10203040, \"ID_STR\":\"10203040\"}";
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		response.getEntity().writeTo(bos);
		String actualPayload = new String(bos.toByteArray());
		Assert.assertEquals(actualPayload, expectedPayload);
		response = httpClient.doPost(url1, headers, payload, "application/json");
		Thread.sleep(2000);
		assertEquals(response.getStatusLine().getStatusCode(), 202);

		LogEvent[] logs = logViewer.getAllSystemLogs();
		int i = 1;
		for (LogEvent log : logs) {
			if (log.getMessage().contains(logLine0)) {
				++i;
			}
			if (log.getMessage().contains(logLine1)) {
				++i;
			}
			if (log.getMessage().contains(logLine2)) {
				++i;
			}
		}
		Assert.assertEquals(i, 4);
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		super.cleanup();
	}
}