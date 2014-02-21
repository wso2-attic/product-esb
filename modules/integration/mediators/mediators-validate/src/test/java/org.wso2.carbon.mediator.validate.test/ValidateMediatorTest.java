package org.wso2.carbon.mediator.validate.test;

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

public class ValidateMediatorTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(ValidateMediatorTest.class);
    String searchString = "<ax21:symbol>IBM</ax21:symbol>";

    @Override
    public void init() {
        log.info("Initializing Validate mediator Tests");
        log.debug("Validate mediator Tests Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running Validate mediator SuccessCase ");

        StockQuoteClient stockQuoteClient = new StockQuoteClient();
        OMElement result = null;

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();
            LogViewerStub logViewerStub = new LogViewerStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/LogViewer");
            authenticateStub.authenticateAdminStub(logViewerStub, sessionCookie);
            OMElement omElement = artifactReader.getOMElement(ValidateMediatorTest.class.getResource("/validate.xml").getPath());
            configServiceAdminStub.updateConfiguration(omElement);


            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT, null, "IBM");
                System.out.println(result);
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/", null, "IBM");
            }
            log.info(result);
            GetLogs getLogs = new GetLogs();
            getLogs.setKeyword("mediator");
            //GetLogsResponse getLogsResponse = logViewerStub.getLogs(getLogs);
            //LogMessage[] logMessages = getLogsResponse.get_return();

            LogMessage[] logMessages = logViewerStub.getLogs(null, null);

            if ((!logMessages[logMessages.length - 1].getLogMessage().contains(searchString)) && (!result.toString().contains("IBM Company"))) {
                Assert.fail("Validate mediator doesn't work");
                log.error("Validate mediator doesn't work");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Validate mediator doesn't work : " + e.getMessage());

        }


    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }
}
