package org.wso2.carbon.mediaotr.rule.test;

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
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;

public class RuleMediatorSimpleTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(RuleMediatorSimpleTest.class);

    @Override
    public void init() {
        log.info("Initializing Rule mediator Tests");
        log.debug("Rule mediator Tests Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running Rule mediator SuccessCase ");
        StockQuoteClient stockQuoteClient = new StockQuoteClient();
        OMElement result = null;

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();

            OMElement omElement = artifactReader.getOMElement(RuleMediatorSimpleTest.class.getResource("/rule_simple.xml").getPath());
            configServiceAdminStub.updateConfiguration(omElement);

            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT, null, "IBM");
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + "/services/" + FrameworkSettings.TENANT_NAME + "/", null, "IBM");
            }

            log.info(result);
            System.out.println(result);
            //<ns:getQuoteResponse xmlns:ns="http://services.samples"><ns:return xmlns:ax25="http://services.samples/xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ax25:GetQuoteResponse"><ax25:change>3.887818082079545</ax25:change><ax25:earnings>13.93967907679255</ax25:earnings><ax25:high>-87.40149738743696</ax25:high><ax25:last>87.63313088293049</ax25:last><ax25:lastTradeTimestamp>Wed Oct 27 14:39:07 IST 2010</ax25:lastTradeTimestamp><ax25:low>90.28778009504029</ax25:low><ax25:marketCap>6042860.565692762</ax25:marketCap><ax25:name>IBM Company</ax25:name><ax25:open>90.34233757313447</ax25:open><ax25:peRatio>23.104500706352912</ax25:peRatio><ax25:percentageChange>4.065205301144391</ax25:percentageChange><ax25:prevClose>95.63645115254303</ax25:prevClose><ax25:symbol>IBM</ax25:symbol><ax25:volume>9871</ax25:volume></ns:return></ns:getQuoteResponse>

            assert result != null;
            if (!result.toString().contains("IBM")) {
                Assert.fail("Rule  Mediator not invoked");
                log.error("Rule Mediator not invoked");
            }
        }
        catch (Exception
                e) {
            e.printStackTrace();
            log.error("Rule Mediator doesn't work : " + e.getMessage());

        }
    }


    @Override
    public void runFailureCase
            () {
    }

    @Override
    public void cleanup() {
    }
}
