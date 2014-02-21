package org.wso2.carbon.mediator.switchm.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.axis2.GetLogs;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class SwitchMediatorTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(SwitchMediatorTestCase.class);

    String searchWord = "<ns:symbol>IBM</ns:symbol>";

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing Switch Mediator")
    public void testSwitchMediator() throws IOException {

        StockQuoteClient axis2Client = new StockQuoteClient();

        OMElement result = null;

        updateESBConfiguration("/switch.xml");

        launchStockQuoteService();

        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL() + "/services/" +
                    FrameworkSettings.TENANT_NAME + "/", null, "IBM");
        }

        LogViewerStub logViewerStub = new LogViewerStub("https://" + FrameworkSettings.HOST_NAME +
                                                                        ":" + FrameworkSettings.HTTPS_PORT +
                                                                        "/services/LogViewer");
        authenticate(logViewerStub);

        GetLogs getLogs = new GetLogs();
        getLogs.setKeyword("mediator");

        LogMessage[] logMessages = logViewerStub.getLogs("ALL", getLogs.getKeyword());

        assertNotNull(logMessages);
        assertTrue(logMessages[logMessages.length - 1].getLogMessage().contains(searchWord));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }

}
package org.wso2.carbon.mediator.switchm.test;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.axis2.GetLogs;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.io.IOException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class SwitchMediatorTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(SwitchMediatorTestCase.class);

    String searchWord = "<ns:symbol>IBM</ns:symbol>";

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
        this.sessionCookie = util.login();
    }

    @Test(groups = "wso2.esb", description = "Testing Switch Mediator")
    public void testSwitchMediator() throws IOException {

        StockQuoteClient axis2Client = new StockQuoteClient();

        OMElement result = null;

        updateESBConfiguration("/switch.xml");

        launchStockQuoteService();

        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            result = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(), null, "IBM");
        } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            result = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL() + "/services/" +
                    FrameworkSettings.TENANT_NAME + "/", null, "IBM");
        }

        LogViewerStub logViewerStub = new LogViewerStub("https://" + FrameworkSettings.HOST_NAME +
                                                                        ":" + FrameworkSettings.HTTPS_PORT +
                                                                        "/services/LogViewer");
        authenticate(logViewerStub);

        GetLogs getLogs = new GetLogs();
        getLogs.setKeyword("mediator");

        LogMessage[] logMessages = logViewerStub.getLogs("ALL", getLogs.getKeyword());

        assertNotNull(logMessages);
        assertTrue(logMessages[logMessages.length - 1].getLogMessage().contains(searchWord));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        cleanup();
    }

}
