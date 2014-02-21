package org.wso2.carbon.mediator.command.test;

/*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.core.utils.ArtifactReader;
import org.wso2.carbon.integration.core.utils.StockQuoteClient;
import org.wso2.carbon.logging.view.LogViewerStub;
import org.wso2.carbon.logging.view.types.axis2.GetLogs;
import org.wso2.carbon.logging.view.types.axis2.GetLogsResponse;
import org.wso2.carbon.logging.view.types.carbon.LogMessage;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;

import java.io.File;

public class CmdWithPropertyTest extends TestTemplate {

    private static final Log log = LogFactory.getLog(CmdWithPropertyTest.class);

    @Override
    public void init() {
        log.info("Initializing Command Mediator Test class ");
        log.debug("Command Mediators Test Initialized");
    }


    @Override
    public void runSuccessCase() {
        log.debug("Running SuccessCase ");
        OMElement result = null;
        boolean isFound = false;
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            LogViewerStub logViewerStub = new LogViewerStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/LogViewer");
            authenticateStub.authenticateAdminStub(logViewerStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();

            OMElement omElement = artifactReader.getOMElement(CmdWithPropertyTest.class.getResource("/CmdWithProp.xml").getPath());

            configServiceAdminStub.updateConfiguration(omElement);

            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT, null, "IBM");
            }
            if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/", null, "IBM");
            }
            GetLogsResponse getLogsResponse = new GetLogsResponse();
            GetLogs getLogs = new GetLogs();
            getLogs.setKeyword("mediator");
            getLogsResponse = logViewerStub.getLogs(getLogs);
            LogMessage[] logMessages = getLogsResponse.get_return();
            int removeMessageLength = 0;
            if (logMessages.length > 4) {
                removeMessageLength = logMessages.length - 3;
            } else {
                removeMessageLength = 0;
            }
            for (int i = removeMessageLength; i <= logMessages.length - 1; i++) {
                System.out.println(logMessages[i].getLogMessage());
                if (logMessages[i].getLogMessage().contains("MSFT")) {
                    isFound = true;
                }
            }
            System.out.println(result);

            log.info(result);
            if (!isFound) {

                Assert.fail("Command mediator doesn't work");
                log.error("Command mediator doesn't work");
            }

        } catch (Exception e) {
            log.error("Command mediator doesn't work : " + e.getMessage());

        }

    }

    @Override
    public void runFailureCase() {


    }

    @Override
    public void cleanup() {
    }
}
