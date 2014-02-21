package org.wso2.esb.samples.endpoint;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.LoadbalanceFailoverClient;
import org.wso2.esb.integration.axis2.SampleAxis2Server;

import static org.testng.Assert.assertTrue;

public class Sample52Test extends ESBIntegrationTestCase {

    private  LoadbalanceFailoverClient loadbalanceFailoverClient;

    private SampleAxis2Server server1;
    private SampleAxis2Server server2;
    private SampleAxis2Server server3;

    public void init() throws Exception {
        loadbalanceFailoverClient = new LoadbalanceFailoverClient();

        server1 = new SampleAxis2Server("test_axis2_server_9001.xml");
        server1.deployService(SampleAxis2Server.LB_SERVICE_1);
        server1.start();

        server2 = new SampleAxis2Server("test_axis2_server_9002.xml");
        server2.deployService(SampleAxis2Server.LB_SERVICE_1);
        server2.start();

        server3 = new SampleAxis2Server("test_axis2_server_9003.xml");
        server3.deployService(SampleAxis2Server.LB_SERVICE_1);
        server3.start();
    }

    @Test(groups = {"wso2.esb"}, description = "Sample 52: Session less load balancing between 3 endpoints")
    public void testLBEndpoint() throws Exception {
        loadSampleESBConfiguration(52);
        loadbalanceFailoverClient.sendLoadBalanceRequests();
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        server1.stop();
        server2.stop();
        server3.stop();
    }
}
