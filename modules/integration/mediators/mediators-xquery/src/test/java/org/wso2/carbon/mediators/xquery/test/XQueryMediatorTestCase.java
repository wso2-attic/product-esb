package org.wso2.carbon.mediators.xquery.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.esb.integration.axis2.StockQuoteClient;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class XQueryMediatorTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(XQueryMediatorTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing XQuery Mediation")
    public void testXQueryMediation() throws Exception {

        StockQuoteClient stockQuoteClient = new StockQuoteClient();
        OMElement result = null;

        updateESBConfiguration("/xquery.xml");

        launchStockQuoteService();

        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() +
                                                                  "/services/StockQuoteProxy", null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() +
                                                                  "/services/" + FrameworkSettings.TENANT_NAME +
                                                                  "/services/StockQuoteProxy", null, "IBM");
        }
        log.info(result);

        assertNotNull(result);
        assertTrue(result.toString().contains("IBM"));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }

}
package org.wso2.carbon.mediators.xquery.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.esb.integration.axis2.StockQuoteClient;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class XQueryMediatorTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(XQueryMediatorTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing XQuery Mediation")
    public void testXQueryMediation() throws Exception {

        StockQuoteClient stockQuoteClient = new StockQuoteClient();
        OMElement result = null;

        updateESBConfiguration("/xquery.xml");

        launchStockQuoteService();

        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() +
                                                                  "/services/StockQuoteProxy", null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = stockQuoteClient.sendSimpleStockQuoteRequest(getMainSequenceURL() +
                                                                  "/services/" + FrameworkSettings.TENANT_NAME +
                                                                  "/services/StockQuoteProxy", null, "IBM");
        }
        log.info(result);

        assertNotNull(result);
        assertTrue(result.toString().contains("IBM"));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }

}
