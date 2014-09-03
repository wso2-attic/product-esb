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
package org.wso2.carbon.esb.email.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.util.base64.Base64Utils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.automation.utils.axis2client.AxisServiceClient;
import org.wso2.carbon.esb.ESBIntegrationTest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class AttachmentAndBodyTest extends ESBIntegrationTest {

    private static final String GMAIL_USER_NAME = "test.automation.dummy";
    private static final String GMAIL_PASSWORD = "automation.test";
    public static final String GMAIL_FEED_URL = "https://mail.google.com/mail/feed/atom";
    private final String AXIS2_CONFIG_URI_BASED_DISPATCH = "/email/transport/axis2.xml";
    private ServerConfigurationManager configManager;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        File customAxisConfig = new File(getESBResourceLocation() + AXIS2_CONFIG_URI_BASED_DISPATCH);
        configManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        configManager.applyConfiguration(customAxisConfig);
        super.init();
        loadESBConfigurationFromClasspath( "/artifacts/ESB/email/transport/send_attachment_and_body.xml");
    }

    @Test(groups = "wso2.esb", description = "- send email with attachment and a body")
    public void testCustomProxy() throws Exception {
        String title = "test_mail_" + new BigInteger(256, new Random());
        callMailProxy(title);
        Thread.sleep(30000);
        OMElement atomFeed = getAtomFeedContent(GMAIL_FEED_URL);
        OMElement entry = getEntry(atomFeed, title);
        assertNotNull(entry, "email not available in the receivers feed.");
        OMElement summaryOm = (OMElement) entry.getChildrenWithName(new QName("summary")).next();
        assertEquals(summaryOm.getText(),"This text will appear in body");
    }

    private void callMailProxy(String title) throws Exception {
        AxisServiceClient client = new AxisServiceClient();
        String payload = "<?xml version='1.0' encoding='UTF-8'?><subject>" + title + "</subject>";

        AXIOMUtil.stringToOM(payload);
        client.sendReceive(AXIOMUtil.stringToOM(payload), esbServer.getServiceUrl() + "/MailProxy", "mediate");
    }

    private static OMElement getEntry(OMElement mailFeed, String title) throws XMLStreamException, IOException {
        Iterator itr = mailFeed.getChildrenWithName(new QName("entry"));
        while (itr.hasNext()) {
            OMElement entry = (OMElement) itr.next();
            for (Iterator itrTitle = entry.getChildrenWithName(new QName("title")); itrTitle.hasNext(); ) {
                OMElement titleOm = (OMElement) itrTitle.next();
                if (titleOm.getText().equals(title)) {
                    return entry;
                }

            }
        }
        return null;
    }


    private static OMElement getAtomFeedContent(String atomURL) throws IOException, XMLStreamException {
        StringBuilder sb;
        InputStream inputStream = null;
        URL url = new URL(atomURL);
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            String userPassword = GMAIL_USER_NAME + ":" + GMAIL_PASSWORD;
            String encodedAuthorization = Base64Utils.encode(userPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " +
                    encodedAuthorization);
            connection.connect();

            inputStream = connection.getInputStream();
            sb = new StringBuilder();
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            assert inputStream != null;
            inputStream.close();
        }

        return AXIOMUtil.stringToOM(sb.toString());

    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        try {
            super.cleanup();
        } finally {
            configManager.restoreToLastConfiguration();
        }
    }
}

