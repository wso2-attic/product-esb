package org.wso2.esb.samples.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.LoadbalanceFailoverClient;
import org.wso2.esb.integration.axis2.SampleAxis2Server;

public class Sample53Test extends ESBIntegrationTestCase {

    private LoadbalanceFailoverClient loadbalanceFailoverClient;

    private SampleAxis2Server server1;
    private SampleAxis2Server server2;
    private SampleAxis2Server server3;

    private static final Log log = LogFactory.getLog(Sample53Test.class);

    public void init() throws Exception {
        loadbalanceFailoverClient = new LoadbalanceFailoverClient();
    }

    @Test(groups = {"wso2.esb"}, description = "Sample 53: Failover sending among 3 endpoints")
    public void testFOEndpoint() throws Exception {

        loadSampleESBConfiguration(53);

        server1 = new SampleAxis2Server("test_axis2_server_9001.xml");
        server1.deployService(SampleAxis2Server.LB_SERVICE_1);
        server1.start();
        loadbalanceFailoverClient.sendLoadBalanceRequests();
        server1.stop();

        server2 = new SampleAxis2Server("test_axis2_server_9002.xml");
        server2.deployService(SampleAxis2Server.LB_SERVICE_1);
        server2.start();
        loadbalanceFailoverClient.sendLoadBalanceRequests();
        server2.stop();

        server3 = new SampleAxis2Server("test_axis2_server_9003.xml");
        server3.deployService(SampleAxis2Server.LB_SERVICE_1);
        server3.start();
        loadbalanceFailoverClient.sendLoadBalanceRequests();
        server3.stop();

        log.info("Waiting 60 seconds for first endpoint to un-suspend");

        Thread.sleep(60000);

        server1 = new SampleAxis2Server("test_axis2_server_9001.xml");
        server1.deployService(SampleAxis2Server.LB_SERVICE_1);
        server1.start();
        loadbalanceFailoverClient.sendLoadBalanceRequests();
        server1.stop();

    }

    @Override
    protected void cleanup() {
        super.cleanup();
        if (server1 != null && server1.isStarted()) {
            server1.stop();
        }
        if (server2 != null && server2.isStarted()) {
            server2.stop();
        }
        if (server3 != null && server3.isStarted()) {
            server3.stop();
        }
    }
}
