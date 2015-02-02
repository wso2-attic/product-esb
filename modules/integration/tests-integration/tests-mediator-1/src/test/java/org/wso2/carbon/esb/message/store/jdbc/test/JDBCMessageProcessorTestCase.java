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

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
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

        h2 = new H2DataBaseManager("jdbc:h2:~/test", "sa", "");//   jdbc:h2:repository/database/WSO2CARBON_DB

        h2.execute("CREATE TABLE IF NOT EXISTS jdbc_store_table(\n" +
                   "indexId BIGINT( 20 ) NOT NULL auto_increment ,\n" +
                   "msg_id VARCHAR( 200 ) NOT NULL ,\n" +
                   "message BLOB NOT NULL, \n" +
                   "PRIMARY KEY ( indexId )\n" +
                   ")");
        h2.disconnect();
        updateESBConfiguration(synapse);
    }

//    @Test(groups = {"wso2.esb"}, description = "Test proxy service with jdbc message store")
    public void testJDBCMessageStoreAndProcessor() throws Exception {

        AxisServiceClient client = new AxisServiceClient();
        for (int i = 0; i < 5; i++) {
            client.sendRobust(Utils.getStockQuoteRequest("JDBC"), getProxyServiceURLHttp("JDBCStoreAndProcessorTestCaseProxy"), "getQuote");
        }

        Thread.sleep(5000);

        ServerConfigurationManager serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.restartGracefully();

        H2DataBaseManager h3 = null;
        try {

            h3 = new H2DataBaseManager("jdbc:h2:~/test", "sa", "");
            ResultSet results = h3.executeQuery("SELECT * FROM jdbc_store_table");

            int a = results.getFetchSize(); /** Fails here! **/

            Assert.assertTrue("All messages sent to store", results.getFetchSize() == 5);

        } finally {
            if (h3 != null) {
                h3.disconnect();
            }
        }
    }


    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        H2DataBaseManager h4 = new H2DataBaseManager("jdbc:h2:~/test", "sa", "");

        h4.executeUpdate("DROP TABLE jdbc_store_table");

        h4.disconnect();

        super.cleanup();
    }
}
