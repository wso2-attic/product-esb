package org.wso2.carbon.esb.endpoint.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.LoadbalanceFailoverClient;
import org.wso2.esb.integration.common.utils.servers.axis2.SampleAxis2Server;
import java.io.File;

public class FailoverEndpointonHttpStatusCodesTestCase extends ESBIntegrationTest {

    private SampleAxis2Server axis2Server1;
    private SampleAxis2Server axis2Server2;
    private SampleAxis2Server axis2Server3;
    private LoadbalanceFailoverClient lbClient;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        loadESBConfigurationFromClasspath(File.separator + "artifacts" + File.separator + "ESB" + File.separator + "endpoint" + File.separator + "failoverEndpointConfig" + File.separator + "failoverOnHttpStatusCodes.xml");

        axis2Server1 = new SampleAxis2Server("test_axis2_server_9001.xml");
        axis2Server2 = new SampleAxis2Server("test_axis2_server_9002.xml");
        axis2Server3 = new SampleAxis2Server("test_axis2_server_9003.xml");

        axis2Server1.deployService(SampleAxis2Server.LB_SERVICE_5);
        axis2Server2.deployService(SampleAxis2Server.LB_SERVICE_1);
        axis2Server3.deployService(SampleAxis2Server.LB_SERVICE_1);

        axis2Server1.start();
        axis2Server2.start();
        axis2Server3.start();

        lbClient = new LoadbalanceFailoverClient();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = {"wso2.esb"}, description = "Testing Faiover Endpoints on http Status Codes")
    public void testFailoverEndpoint() throws Exception {

        boolean assertException = false;
        try {
            String response =
                    lbClient.sendLoadBalanceRequest(getMainSequenceURL(), null);
            Assert.assertNotNull(response, "Response is null");
            Assert.assertTrue(response.contains("Response from server: Server_1"), "Response server wrong");

            axis2Server2.stop();

            String response3 =
                    lbClient.sendLoadBalanceRequest(getMainSequenceURL(), null);
            Assert.assertNotNull(response3, "Response is null");
            Assert.assertTrue(response3.contains("Response from server: Server_1"), "Response server wrong");
        } catch (AxisFault ex) {
            assertException = ex.getMessage().contains("Response from server: Server_1");
        }


    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        super.cleanup();

        if (axis2Server1.isStarted()) {
            axis2Server1.stop();
        }
        if (axis2Server2.isStarted()) {
            axis2Server2.stop();
        }
        if (axis2Server3.isStarted()) {
            axis2Server3.stop();
        }
        axis2Server1 = null;
        axis2Server2 = null;
        axis2Server3 = null;
        lbClient = null;
    }
}

