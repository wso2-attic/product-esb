/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.mediator.test.foreach;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.FixedSizeSymbolGenerator;

/* Tests sending different number of small messages through foreach mediator */

public class ForEachSmallMessageTestCase extends ESBIntegrationTest {

    private String symbol ;

    @BeforeClass
    public void setEnvironment() throws Exception {
        init();
        loadESBConfigurationFromClasspath(
                "/artifacts/ESB/mediatorconfig/foreach/simple_single_foreach.xml");
        symbol = FixedSizeSymbolGenerator.generateMessageKB(5);
    }

    @Test(groups = "wso2.esb", description = "Tests small message in small number ~20")
    public void testSmallNumbers() throws Exception {
        OMElement response = null;
        for (int i = 0; i < 20; i++) {
            response = axis2Client.sendCustomQuoteRequest(getMainSequenceURL(),
                                                          null, symbol);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("WSO2"));
            response = null;
        }
    }

    @Test(groups = "wso2.esb", description = "Tests small message in small number ~100")
    public void testLargeNumbers() throws Exception {
        OMElement response = null;
        for (int i = 0; i < 100; i++) {
            response =
                    axis2Client.sendSimpleStockQuoteRequest(getMainSequenceURL(),
                                                            null, symbol);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.toString().contains("WSO2"));
            response = null;
        }
    }

    @AfterClass
    public void close() throws Exception {
        symbol = null;
        super.cleanup();
    }

}
