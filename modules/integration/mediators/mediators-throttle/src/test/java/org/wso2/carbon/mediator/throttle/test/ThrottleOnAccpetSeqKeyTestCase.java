package org.wso2.carbon.mediator.throttle.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ThrottleOnAccpetSeqKeyTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(ThrottleOnAccpetSeqKeyTestCase.class);

    private static final int THROTTLE_MAX_MSG_COUNT = 4;

    int throttleCounter = 0;

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing Throttle Mediator success scenario with onAccept key Sequence")
    public void testThrottleMediatorSequenceKeySuccess() throws Exception{

        OMElement result = null;

        updateESBConfiguration("/throttleOnAcceptSeqKey.xml");

        launchStockQuoteService();

        for (int i = 0; i < THROTTLE_MAX_MSG_COUNT; i++) {

            StockQuoteClient stockQuoteClient = new StockQuoteClient();

            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() + "/services/" +
                        FrameworkSettings.TENANT_NAME +
                        "/", null, "IBM");
            }

            assertNotNull(result);

            //A request can only succeed as long as the message count does not exceed the maximum allowed amount.
            assertTrue(i < THROTTLE_MAX_MSG_COUNT);
            //Whether the correct response is sent
            assertTrue(result.toString().contains("IBM Company"));

            throttleCounter++;
        }
    }

    /*Once the maximum number of messages are sent from the previous test method, test by sending one more message.
          ESB should throw an AxisFault saying "Access Denied"*/
    @Test(groups = {"wso2.esb"}, dependsOnMethods = "testThrottleMediatorSequenceKeySuccess",
            description = "Testing Throttle Mediator failure scenario with onAccept key Sequence",
            expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testThrottleMediatorSequenceKeyFailure() throws AxisFault {

        OMElement result = null;
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        try{
            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() + "/services/" +
                        FrameworkSettings.TENANT_NAME +
                        "/", null, "IBM");
            }
        }catch (AxisFault e){
            //The AxisFault can only occur once the message count exceeds the maximum allowed amount.
            assertTrue(throttleCounter >= THROTTLE_MAX_MSG_COUNT);
            //Whether the Exception occured due to the correct reason.
            assertTrue(e.getMessage().toLowerCase().contains("access denied"));
            throw e;
        }
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }
}
package org.wso2.carbon.mediator.throttle.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ThrottleOnAccpetSeqKeyTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(ThrottleOnAccpetSeqKeyTestCase.class);

    private static final int THROTTLE_MAX_MSG_COUNT = 4;

    int throttleCounter = 0;

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing Throttle Mediator success scenario with onAccept key Sequence")
    public void testThrottleMediatorSequenceKeySuccess() throws Exception{

        OMElement result = null;

        updateESBConfiguration("/throttleOnAcceptSeqKey.xml");

        launchStockQuoteService();

        for (int i = 0; i < THROTTLE_MAX_MSG_COUNT; i++) {

            StockQuoteClient stockQuoteClient = new StockQuoteClient();

            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() + "/services/" +
                        FrameworkSettings.TENANT_NAME +
                        "/", null, "IBM");
            }

            assertNotNull(result);

            //A request can only succeed as long as the message count does not exceed the maximum allowed amount.
            assertTrue(i < THROTTLE_MAX_MSG_COUNT);
            //Whether the correct response is sent
            assertTrue(result.toString().contains("IBM Company"));

            throttleCounter++;
        }
    }

    /*Once the maximum number of messages are sent from the previous test method, test by sending one more message.
          ESB should throw an AxisFault saying "Access Denied"*/
    @Test(groups = {"wso2.esb"}, dependsOnMethods = "testThrottleMediatorSequenceKeySuccess",
            description = "Testing Throttle Mediator failure scenario with onAccept key Sequence",
            expectedExceptions = org.apache.axis2.AxisFault.class)
    public void testThrottleMediatorSequenceKeyFailure() throws AxisFault {

        OMElement result = null;
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        try{
            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() + "/services/" +
                        FrameworkSettings.TENANT_NAME +
                        "/", null, "IBM");
            }
        }catch (AxisFault e){
            //The AxisFault can only occur once the message count exceeds the maximum allowed amount.
            assertTrue(throttleCounter >= THROTTLE_MAX_MSG_COUNT);
            //Whether the Exception occured due to the correct reason.
            assertTrue(e.getMessage().toLowerCase().contains("access denied"));
            throw e;
        }
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }
}
