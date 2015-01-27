/**
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.esb.message.store.jdbc.test;

import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.esb.integration.common.utils.clients.axis2client.AxisServiceClient;

import java.sql.ResultSet;

public class JDBCMessageProcessorTestCase extends ESBIntegrationTest {

    private H2DataBaseManager h2;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        OMElement synapse = esbUtils.loadResource("/artifacts/ESB/jdbc/jdbc_message_store_and_processor_service.xml");

        h2 = new H2DataBaseManager("jdbc:h2:repository/database/WSO2CARBON_DB", "wso2carbon", "wso2carbon");

        h2.execute("CREATE TABLE 'jdbc_store_table' (\n" +
                   "'indexId' BIGINT( 20 ) NOT NULL ,\n" +
                   "'msg_id' VARCHAR( 200 ) NOT NULL ,\n" +
                   "'message' BLOB NOT NULL ,\n" +
                   "PRIMARY KEY ( 'indexId' )\n" +
                   ")");

        updateESBConfiguration(synapse);
    }

    @Test(groups = {"wso2.esb"}, description = "Test proxy service with jdbc message store")
    public void testJDBCMessageStoreAndProcessor() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            client.sendRobust(Utils.getStockQuoteRequest("JDBC"), getProxyServiceURLHttp("JDBCStoreAndProcessorTestCaseProxy"), "getQuote");
        }

        Thread.sleep(10000);

        try {

            ResultSet results = h2.executeQuery("SELECT * FROM jdbc_store_table");

            if (results.getFetchSize() <= 5) {

            } else {

            }

//            for (int i = 0; i < 10; i++) {
//                if (i < 5) {
//                    //first 5 messages should be in the queue
//                    Assert.assertNotNull(consumer.popMessage(), "JMS Message Processor not send message to endpoint");
//
//                } else {
//                    //after 5 no messages should be in the queue
//                    Assert.assertNull(consumer.popMessage(), "JMS Message Processor sends same message more than once ");
//                }
//            }
        } finally {
        }
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {

        h2.executeUpdate("DROP TABLE jdbc_store_table");

        super.cleanup();
    }
}
