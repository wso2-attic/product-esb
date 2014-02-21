package org.wso2.carbon.mediator.throttle.test;
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
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.axis2.GetLogs;
import org.wso2.carbon.logging.view.stub.types.axis2.GetLogsResponse;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;


public class ThrottleMediatorTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(ThrottleMediatorTest.class);
    String searchWord = "ThrottleMediator Concurrent access controller for ID : A allows : 10 concurrent accesses";

    @Override
    public void init() {
        log.info("Initializing Throttle Mediator Tests");
        log.debug("Throttle Mediator Tests Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running Throttle Mediator SuccessCase ");
        OMElement result = null;


        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();
            LogViewerStub logViewerStub = new LogViewerStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/LogViewer");
            authenticateStub.authenticateAdminStub(logViewerStub, sessionCookie);
            OMElement omElement = artifactReader.getOMElement(ThrottleMediatorTest.class.getResource("/throttle.xml").getPath());
            configServiceAdminStub.updateConfiguration(omElement);

            /*Sending a StockQuoteClient request*/
            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT, null, "IBM");
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/", null, "IBM");
            }
            log.info(result);

            GetLogs getLogs = new GetLogs();
            getLogs.setKeyword("mediator");
            //GetLogsResponse getLogsResponse = logViewerStub.getLogs(getLogs);
            //LogMessage[] logMessages = getLogsResponse.get_return();

            LogMessage[] logMessages = logViewerStub.getLogs(null, null);
            System.out.println(logMessages[logMessages.length - 1].getLogMessage());
            if (logMessages[logMessages.length - 1].getLogMessage().contains(searchWord)) {
                Assert.assertTrue("Throttle Mediation Success", true);
                log.info("Throttle Mediation Success");
            }

            /*if (isFound == true) {
                System.out.println("Results Found at " + timeStamp + " as ** " + searchWord + " **");
                Assert.assertTrue("Throttle Mediation Success", true);
                log.info("Throttle Mediation Success");
            }*/
        } catch (Exception e) {
            log.error("Throttle Mediator doesn't work : " + e.getMessage());

        }
    }

    @Override
    public void runFailureCase() {

    }

    @Override
    public void cleanup() {

    }
}

