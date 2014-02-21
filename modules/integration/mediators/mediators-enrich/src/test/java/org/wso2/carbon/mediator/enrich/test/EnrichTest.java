package org.wso2.carbon.mediator.enrich.test;

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

/*
* Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*  http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

public class EnrichTest extends TestTemplate {

    private static final Log log = LogFactory.getLog(EnrichTest.class);

    @Override
    public void init() {
        log.info("Initializing Enrich Mediator Test class ");
        log.debug("Enrich Mediators Test Initialized");
    }


    @Override
    public void runSuccessCase() {
        log.debug("Running SuccessCase ");
        OMElement result = null;
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();
            LogViewerStub logViewerStub = new LogViewerStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/LogViewer");

            OMElement omElement = artifactReader.getOMElement(EnrichTest.class.getResource("/enrichSynapse.xml").getPath());
            System.out.println(omElement.getLocalName());

            configServiceAdminStub.updateConfiguration(omElement);


            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT, null, "IBM");
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/", null, "IBM");
            }
            System.out.println(result);
            GetLogs getLogs = new GetLogs();
            getLogs.setKeyword("mediator");
            //GetLogsResponse getLogsResponse = logViewerStub.getLogs(getLogs);
            //LogMessage[] logMessages = getLogsResponse.get_return();

            LogMessage[] logMessages = logViewerStub.getLogs(null, null);

            if ((logMessages[logMessages.length - 1].getLogMessage().contains("Direction: request, ENRICH_PROP = <?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><ns:getQuote xmlns:ns=\"http://services.samples\"><ns:request><ns:symbol>IBM</ns:symbol></ns:request></ns:getQuote></soapenv:Body></soapenv:Envelope>")) && (!result.toString().contains("IBM"))) {
                Assert.fail("Enrich mediator doesn't work");
                log.error("Enrich mediator doesn't work");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Enrich mediator doesn't work : " + e.getMessage());
        }
    }

    @Override
    public void runFailureCase() {


    }

    @Override
    public void cleanup() {

    }
}


