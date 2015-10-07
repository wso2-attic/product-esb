package org.wso2.carbon.esb.mediator.test.tracer;

import java.io.File;
import java.rmi.RemoteException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.clients.tracer.TracerAdminClient;
import org.wso2.carbon.mediation.tracer.stub.client.MediationTracerExceptionException;

public class TracerTestCase extends ESBIntegrationTest {

	private TracerAdminClient tracerAdminClient;
	private String tracerLogFilePath;

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {

		super.init();
		tracerAdminClient = new TracerAdminClient(contextUrls.getBackEndUrl(),
				getSessionCookie());
		loadESBConfigurationFromClasspath(File.separator + "artifacts"
				+ File.separator + "ESB" + File.separator + "synapseconfig"
				+ File.separator + "tracer" + File.separator + "tracer.xml");
		tracerLogFilePath = CarbonUtils.getCarbonHome() + File.separator
				+ "repository" + File.separator + "logs" + File.separator
				+ "wso2-esb-trace.log";
	}

	@AfterClass(groups = "wso2.esb", alwaysRun = true)
	public void close() throws Exception {
		super.cleanup();
	}

	@Test(groups = "wso2.esb", description = "Proxy TracerClient Test")
	public void testProxyTracerClient() throws AxisFault, RemoteException,
			XPathExpressionException, Exception {
		// To check whether tracer logged
		boolean isTracerLogged = false;
		try {
			OMElement response = axis2Client.sendSimpleStockQuoteRequest(
					getProxyServiceURLHttp("TracerProxy"), null, "WSO2");

			String[] traceLogs = tracerAdminClient.getTraceLogs();
			for (String log : traceLogs) {
				if (log.contains("**ProxyTracerLog**")) {
					isTracerLogged = true;
				}
			}
		} catch (AxisFault axisFault) {
			throw new AxisFault(
					"AxisFault occurred when sending SimpleStockQuoteService",
					axisFault);
		} catch (RemoteException remoteException) {
			throw new RemoteException(
					"RemoteException occurred when getting the trace log",
					remoteException);
		} catch (Exception e) {
			throw new Exception(
					"Exception occurred when getting the trace log", e);
		} finally {
			removeProxy("TracerProxy");
		}

		Assert.assertTrue(
				isTracerLogged,
				" The tracer is not logging the proxy, expected to log the property 'Check' with value '**ProxyTracerLog**'");
	}

	@Test(groups = "wso2.esb", dependsOnMethods = "testProxyTracerClient", description = "Create Tracer Log File Test")
	public void testTracerLogFileCreate() {

		File tacerFile = new File(tracerLogFilePath);
		Assert.assertTrue(tacerFile.exists(), "The Tracer file is not created");
	}

	@Test(groups = "wso2.esb", dependsOnMethods = "testTracerLogFileCreate", description = "Sequence TracerClient Test")
	public void testSequenceTracerClient() throws Exception {
		// To check whether tracer logged
		boolean isTracerLogged = false;
		try {

			OMElement response = axis2Client
					.sendSimpleStockQuoteRequest(
							getProxyServiceURLHttp("SequenceTracerProxy"),
							null, "WSO2");
			String[] traceLogs = tracerAdminClient.getTraceLogs();
			for (String log : traceLogs) {
				if (log.contains("**SequenceTracerLog**")) {
					isTracerLogged = true;
				}
			}
		} catch (Exception e) {
			throw new Exception(
					"Exception occurred when getting the trace log", e);
		} finally {
			removeProxy("SequenceTracerProxy");
		}

		Assert.assertTrue(
				isTracerLogged,
				" The tracer is not logging the sequence, expected to log the property 'Check' with value '**SequenceTracerLog**'");
	}

	@Test(groups = "wso2.esb", dependsOnMethods = "testSequenceTracerClient", description = "Search Tracer Log Test")
	public void testSearchTracerLog() throws RemoteException,
			MediationTracerExceptionException {
		try {
			boolean search = false;
			String[] traceLogs = tracerAdminClient.searchTraceLog(
					"**SequenceTracerLog**", true);
			for (String log : traceLogs) {
				if (log.contains("Check = **SequenceTracerLog**")) {
					search = true;
				}

				Assert.assertTrue(search,
						"Tracer is not having the search keyword '**SequenceTracerLog**'");

			}
		} catch (RemoteException remoteException) {
			throw new RemoteException(
					"RemoteException occurred when getting the trace log",
					remoteException);
		} catch (MediationTracerExceptionException e) {
			throw new MediationTracerExceptionException(
					"MediationTracerExceptionException occurred when getting the trace log",
					e);
		}

	}

	@Test(groups = "wso2.esb", dependsOnMethods = "testSearchTracerLog", description = "Clear Tracer Log Test")
	public void testClearTracerLog() throws RemoteException {
		boolean isClear = tracerAdminClient.clearTraceLogs();

		Assert.assertTrue(isClear, "Tracer Log is not clear");
	}

	@Test(groups = "wso2.esb", dependsOnMethods = "testSequenceTracerClient", description = "Search Tracer Log after clear the tracer Test")
	public void testSearchTracerLogAfterClear() throws RemoteException,
			MediationTracerExceptionException {
		try {
			boolean search = false;
			String[] traceLogs = tracerAdminClient.searchTraceLog(
					"**SequenceTracerLog**", true);
			for (String log : traceLogs) {
				if (log.contains("Check = **SequenceTracerLog**")) {
					search = true;
				}

				Assert.assertTrue(!search, "Tracer is not cleared");

			}
		} catch (RemoteException remoteException) {
			throw new RemoteException(
					"RemoteException occurred when getting the trace log",
					remoteException);
		} catch (MediationTracerExceptionException e) {
			throw new MediationTracerExceptionException(
					"MediationTracerExceptionException occurred when getting the trace log",
					e);
		}

	}

	@Test(groups = "wso2.esb", description = "Search Tracer Log with null keyword Test")
	public void testSearchTracerLogWithNullKeyword() throws RemoteException,
			MediationTracerExceptionException {
		boolean isError = false;
		try {
			String[] traceLogs = tracerAdminClient.searchTraceLog(null, true);
		} catch (RemoteException e1) {
			isError = true;
		} catch (MediationTracerExceptionException e) {
		}
		Assert.assertTrue(isError);

	}

	private void removeProxy(String proxyName) throws Exception {
		deleteProxyService(proxyName);
	}
}
