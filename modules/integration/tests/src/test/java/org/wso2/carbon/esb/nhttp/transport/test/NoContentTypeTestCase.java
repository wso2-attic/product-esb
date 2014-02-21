/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.esb.nhttp.transport.test;


import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * To ensure that the body of the message is not get dropped when,
 * Content-Type of the message is not mentioned
 */

public class NoContentTypeTestCase extends ESBIntegrationTest {


    private ServerConfigurationManager serverManager = null;
    final String payload = "<Customer>\n" +
            "   <id>123</id>\n" +
            "   <name>TestName</name>\n" +
            "</Customer>";

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        serverManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        serverManager.applyConfiguration(new File(getClass().getResource("/artifacts/ESB/nhttp/transport/axis2.xml").getPath()));
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/nhttp/transport/synapse.xml");
    }

    /**
     * Sending a message without mentioning Content Type and check the body part at the listening port
     * <p/>
     * Public JIRA:    WSO2 Carbon/CARBON-6029
     * Responses With No Content-Type Header not handled properly
     * <p/>
     * Test Artifacts: ESB Sample 0
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.integration_all})
    @Test(groups = "wso2.esb")
    public void testNoContentTypeHeader() throws Exception {

        /**
         * Creating a new HttpClient to send POX message without Content-Type header
         */

        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
        httpclient.getParams().setParameter("http.socket.timeout", new Integer(1000));
        HttpPost postRequest = new HttpPost(getMainSequenceURL());

        HttpEntityEnclosingRequest entityEncReq = postRequest;
        EntityTemplate ent = new EntityTemplate(new ContentProducer() {
            public void writeTo(OutputStream outputStream) throws IOException {
                outputStream.write(payload.getBytes());
                outputStream.flush();
            }
        });
        entityEncReq.setEntity(ent);

        HttpResponse response = httpclient.execute(postRequest);
        String responseString = getResponse(response);
        httpclient.getConnectionManager().shutdown();

        /**
         * Assert for the payload and for its contents
         */
        Assert.assertTrue(responseString.contains("TestName"), "Didn't receive the message from the ESB");

    }

    private String getResponse(HttpResponse response) throws Exception {

        StringBuffer buffer = new StringBuffer();
        if (response.getEntity() != null) {
            InputStream in = response.getEntity().getContent();
            int length;
            byte[] tmp = new byte[2048];
            while ((length = in.read(tmp)) != -1) {
                buffer.append(new String(tmp, 0, length));
            }
        }

        return buffer.toString();
    }


    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        super.cleanup();
        serverManager.restoreToLastConfiguration();
        serverManager = null;
        // wireMonitorServer = null;

    }


}
