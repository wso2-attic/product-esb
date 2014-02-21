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

package org.wso2.carbon.mediator.throttle.test;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.core.utils.ArtifactReader;
import org.wso2.carbon.integration.core.utils.StockQuoteClient;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;

public class ThrottleOnAccpetSeqKeyTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(ThrottleMediatorTest.class);
    private static final int THROTTLE_MAX_MSG_COUNT = 4;


    @Override
    public void init() {
        log.info("Initializing Throttle Mediator Tests");
        log.debug("Throttle Mediator Tests Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running Throttle Mediator SuccessCase ");
        int throttleCounter = 0;
        OMElement result = null;

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();
            OMElement omElement = artifactReader.getOMElement(ThrottleOnAccpetSeqKeyTest.class.getResource("/throttleOnAcceptSeqKey.xml").getPath());
            configServiceAdminStub.updateConfiguration(omElement);

            /*Sending StockQuoteClient requests */
            for (int i = 0; i <= THROTTLE_MAX_MSG_COUNT; i++) {
                StockQuoteClient stockQuoteClient = new StockQuoteClient();
                if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                    result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT, null, "IBM");
                } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                    result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/", null, "IBM");
                }
                System.out.println(result);
                if (!result.toString().contains("IBM")) {
                    log.error("Required response not found");
                    Assert.fail("Required response not found");
                }
                log.info(result);
                throttleCounter++;

            }
            if (throttleCounter > THROTTLE_MAX_MSG_COUNT) {
                if (result.toString().contains("IBM")) {
                    log.error("Required response not found.replying service");
                    Assert.fail("Required response not found.replying service");
                }
            } else {
                log.error("Throttling response count does not match or max response count error");
                Assert.fail("Throttling response count does not match");
            }
        } catch (Exception e) {
            if (!e.getMessage().contains("**Access Denied**")) {
                log.error("Throttle Mediator doesn't work : " + e.getMessage());
            }

        }
    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {

    }
}
