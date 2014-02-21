package org.wso2.carbon.mediator.throttle.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.esb.integration.axis2.StockQuoteClient;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import static org.testng.Assert.assertTrue;

public class ThrottleMediatorTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(ThrottleMediatorTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing Throttle Mediator")
    public void testThrottleMediator() throws Exception{

        log.debug("Running Throttle Mediator SuccessCase ");
        OMElement result = null;

        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        updateESBConfiguration("/throttle.xml");

        launchStockQuoteService();

        /*Sending a StockQuoteClient request*/
        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() + "/services/" +
                    FrameworkSettings.TENANT_NAME + "/", null, "IBM");
        }
        log.info(result);

        assertTrue(result.toString().contains("IBM Company"));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }


}
package org.wso2.carbon.mediator.throttle.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.esb.integration.axis2.StockQuoteClient;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import static org.testng.Assert.assertTrue;

public class ThrottleMediatorTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(ThrottleMediatorTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing Throttle Mediator")
    public void testThrottleMediator() throws Exception{

        log.debug("Running Throttle Mediator SuccessCase ");
        OMElement result = null;

        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        updateESBConfiguration("/throttle.xml");

        launchStockQuoteService();

        /*Sending a StockQuoteClient request*/
        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() + "/services/" +
                    FrameworkSettings.TENANT_NAME + "/", null, "IBM");
        }
        log.info(result);

        assertTrue(result.toString().contains("IBM Company"));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }


}
