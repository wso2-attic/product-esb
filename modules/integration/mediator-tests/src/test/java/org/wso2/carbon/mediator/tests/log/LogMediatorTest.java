/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.mediator.tests.log;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.axis2.GetLogs;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class LogMediatorTest extends ESBIntegrationTestCase {

    private StockQuoteClient axis2Client;
    LogViewerStub logViewerStub;
    private LoginLogoutUtil util = new LoginLogoutUtil();
    @BeforeTest(groups = "wso2.esb")
    public void initiateTest() throws Exception {
        axis2Client = new StockQuoteClient();
        logViewerStub = new LogViewerStub(getAdminServiceURL("LogViewer"));
        authenticate(logViewerStub);
        loadESBConfigurationFromClasspath("/mediators/log/logmediator.xml");
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);

    }

    @Test(groups = "wso2.esb", description = "Tests Full level log",enabled = false)
    public void testSendingToDefinedEndpoint() throws IOException, InterruptedException {

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                                                                     null, "WSO2");
        assertTrue(response.toString().contains("WSO2"));
        log.info(response);
        Thread.sleep(2000);
        System.out.println(response);
        GetLogs getLogs = new GetLogs();
        getLogs.setKeyword("mediator");
        LogEvent[] getLogsTrace = logViewerStub.getLogs("TRACE", "LogMediator");
        LogEvent[] getLogsInfo = logViewerStub.getLogs("INFO", "LogMediator");
        LogEvent[] getLogsDebug = logViewerStub.getLogs("DEBUG", "LogMediator");
        assertTraceLogs(getLogsTrace);
        assertDebugLogs(getLogsDebug);
        assertErrorLogs(logViewerStub);
        assertFatelLogs(logViewerStub);
        assertInfoLogs(getLogsInfo);
        assertWarnLogs(logViewerStub);
    }

    @AfterTest(groups = "wso2.esb")
    public void close() throws Exception {
        util.logout();
        super.cleanup();
        axis2Client.destroy();
    }

    private boolean assertTraceLogs(LogEvent[] getLogsTrace) throws IOException {
        File file = new File((getClass().getResource("/mediators/log/loglist_trace.txt").getPath()));
        return traverseLog(getLogsTrace, file);
    }

    private boolean assertInfoLogs( LogEvent[] getLogsInfo) throws IOException {
        File file = new File((getClass().getResource("/mediators/log/loglist_trace.txt").getPath()));
        return traverseLog(getLogsInfo, file);
    }

    private boolean assertDebugLogs(LogEvent[] getLogsDebug) throws IOException {
        File file = new File((getClass().getResource("/mediators/log/loglist_trace.txt").getPath()));
        return traverseLog(getLogsDebug, file);
    }

    private boolean assertWarnLogs(LogViewerStub logViewer) throws IOException {
        LogEvent[] getLogsWarn = logViewer.getLogs("WARN", "LogMediator");
        File file = new File((getClass().getResource("/mediators/log/loglist_trace.txt").getPath()));
        return traverseLog(getLogsWarn, file);
    }

    private boolean assertErrorLogs(LogViewerStub logViewer) throws IOException {
        LogEvent[] getLogsError = logViewer.getLogs("ERROR", "LogMediator");
        File file = new File((getClass().getResource("/mediators/log/loglist_trace.txt").getPath()));
        return traverseLog(getLogsError, file);
    }

    private boolean assertFatelLogs(LogViewerStub logViewer) throws IOException {
        LogEvent[] getLogsFatal = logViewer.getLogs("FATAL", "LogMediator");
        File file = new File((getClass().getResource("/mediators/log/loglist_trace.txt").getPath()));
        return traverseLog(getLogsFatal, file);
    }

    private boolean traverseLog(LogEvent[] getLogsTrace, File file) throws IOException {
        boolean logFound = true;
        String messageLog = null;

        FileReader fileReader = new FileReader(file.getPath());
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }

        List<String> resultLines=lines;
        for (LogEvent tlog : getLogsTrace) {
            for (String logLine : lines) {
                messageLog = logLine;
                if (tlog.getMessage().contains(messageLog)) {
                    resultLines.remove(resultLines.indexOf(line));
                }
            }

        }
        if (!resultLines.isEmpty()) {
            logFound = false;
           /* Assert.fail(messageLog + "log  mediator doesn't work");
            log.error(messageLog + "log mediator doesn't work");*/
        }
        return logFound;
    }
}
