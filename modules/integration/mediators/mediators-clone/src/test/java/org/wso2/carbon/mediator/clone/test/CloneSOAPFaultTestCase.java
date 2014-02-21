package org.wso2.carbon.mediator.clone.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.core.utils.ArtifactReader;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

public class CloneSOAPFaultTestCase extends ESBIntegrationTestCase{
    private static final Log log = LogFactory.getLog(CloneMediatorTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    private ConfigServiceAdminStub configServiceAdminStub;

    @BeforeMethod(groups = "wso2.esb")
    public void login() throws Exception{
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();

        AuthenticateStub authenticateStub = new AuthenticateStub();

        configServiceAdminStub = new ConfigServiceAdminStub("https://localhost:9443/services/ConfigServiceAdmin");

        ServiceClient client = configServiceAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);

        authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
    }

    @Test(groups = "wso2.esb")
    public void testCloneMediator() throws Exception {
        boolean resultForSOAPClone = false;
        ArtifactReader artifactReader = new ArtifactReader();
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        OMElement omElement = artifactReader.getOMElement(CloneMediatorTest.class.getResource("/cloneSOAPFault.xml").getPath());
        configServiceAdminStub.updateConfiguration(omElement);

        try {
            stockQuoteClient.sendSimpleStockQuoteRequest(null, getProxyServiceURL("proxyCastErr", false), null);
        } catch (AxisFault fault) {
            log.error(fault);
            if(fault.toString().contains("Read timed out")) {
                Assert.fail("SOAP fault couldn't clone, since exception thrown when cloning");
            } else if(fault.toString().contains("Connection timeout For")){
                resultForSOAPClone = true;
                assert resultForSOAPClone == true;
            } else {
                assert resultForSOAPClone == true;
            }
        }
    }
}
