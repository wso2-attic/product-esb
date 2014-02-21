/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.proxyservices.test;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.carbon.proxyservices.test.util.ProxyReader;
import org.wso2.carbon.proxyservices.test.util.StockQuoteClient;
import org.wso2.carbon.throttle.stub.ThrottleAdminServiceStub;
import org.wso2.carbon.throttle.stub.types.InternalData;
import org.wso2.carbon.throttle.stub.types.ThrottlePolicy;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class ThrottleTestCase extends ESBIntegrationTestCase {

    public ThrottleTestCase() {
        super("ProxyServiceAdmin");
    }

    @Test(groups = {"wso2.esb"}, description = "Test Throttling.")
    public void testThrottle() throws Exception {
        ProxyData proxyData;
        ProxyReader handler = new ProxyReader();
        int throttleCounter = 0;
        OMElement result = null;
        log.debug("Running AddProxy SuccessCase ");
        ProxyServiceAdminStub proxyServiceAdminStub = new ProxyServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ProxyServiceAdmin");
        authenticate(proxyServiceAdminStub);

        ThrottleAdminServiceStub throttleAdminServiceStub = new ThrottleAdminServiceStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ThrottleAdminService");
        authenticate(throttleAdminServiceStub);

        proxyData = handler.getProxy(ThrottleTestCase.class.getResource("/testdata/StockQuoteProxyTest.xml").getPath());

        //Add proxy Service test
        proxyServiceAdminStub.addProxy(proxyData);

        log.info("Proxy service added");


        ThrottlePolicy throttlePolicy = new ThrottlePolicy();

        InternalData[] internalDatas = new InternalData[1];

        InternalData data = new InternalData();

        data.setMaxRequestCount(6);
        data.setProhibitTimePeriod(10000);
        data.setRange("other");
        data.setAccessLevel(0);
        data.setRangeType("IP");
        data.setUnitTime(10000);

        internalDatas[0] = data;

        throttlePolicy.setInternalConfigs(internalDatas);
        throttleAdminServiceStub.enableThrottling("StockQuoteProxyTest", throttlePolicy);


        for (int i = 0; i <= 7; i++) {
            StockQuoteClient stockQuoteClient = new StockQuoteClient();
            if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/StockQuoteProxyTest", null, "IBM");
            } else if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
                log.info("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/StockQuoteProxyTest");
                result = stockQuoteClient.stockQuoteClientforProxy("http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + FrameworkSettings.TENANT_NAME + "/StockQuoteProxyTest", null, "IBM");
            }
            boolean isFound = result.getChildren().next().toString().contains("IBM");
            log.info(result.toString());
            if (!isFound) {
                log.error("Required response not found");
                Assert.fail("Required response not found");
            }
            Iterator iterator = result.getFirstElement().getChildrenWithName(new QName("http://services.samples/xsd", "name"));
            while (iterator.hasNext()) {
                OMElement element = (OMElement) iterator.next();
                log.info("The response is received : " + element.getText());
                Assert.assertEquals("IBM Company", element.getText());

            }
            throttleCounter++;
        }
        throttleAdminServiceStub.disableThrottling("StockQuoteProxyTest");
        log.info("throttleCounter: " + throttleCounter);
        if (throttleCounter == 6) {
            log.info("Throttlling Done.");
            log.info("Throttlling Done.");
        } else {
            log.error("Throttling response count unmatched");
            Assert.fail("Throttling response count unmatched");
        }
    }
}
