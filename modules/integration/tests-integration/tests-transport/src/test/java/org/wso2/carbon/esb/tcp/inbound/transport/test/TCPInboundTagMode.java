/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.esb.tcp.inbound.transport.test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Integration test to TCP inbound endpoint.
 * TCP messages will be decoded by <foo> </foo> tag.
 * Message format : <foo>actual message</foo>
 * In ESB sending it to http endpoint and return the response back.
 */
public class TCPInboundTagMode  extends ESBIntegrationTest {

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("artifacts/ESB/tcp.inbound/tcpInboundConfig.xml");
        super.init();
    }

    /**
     * Send a TCP message to the inbound endpoint in port 9092.
     * retrive the response and assert.
     *
     * @throws Exception
     */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
    @Test(groups = "wso2.esb")
    public void decodeByTagModeTest() throws Exception {
        Socket socket = new Socket("localhost", 9092);
        socket.setKeepAlive(true);
        OutputStream out = socket.getOutputStream();

        String payload = "<foo><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.samples\" xmlns:xsd=\"http://services.samples/xsd\">\n" +
                         "   <soapenv:Header/>\n" +
                         "   <soapenv:Body>\n" +
                         "      <ser:getQuote>\n" +
                         "         <ser:request>\n" +
                         "            <xsd:symbol>WSO2</xsd:symbol>\n" +
                         "         </ser:request>\n" +
                         "      </ser:getQuote>\n" +
                         "   </soapenv:Body>\n" +
                         "</soapenv:Envelope></foo>";

        CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();
        byte[] tcpPayload = payload.getBytes(charsetDecoder.charset());

        //decode by enclosing tag
        out.write(tcpPayload);
        out.flush();

        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[1024];
        int read;
        int count = 1;
        String response = null;

        while ((read = in.read(buffer)) != -1) {
            response = new String(buffer, 0, read, charsetDecoder.charset());

            if (count == 1) {
                break;
            }
        }

        out.close();
        socket.close();

        org.testng.Assert.assertTrue(response.contains("getQuoteResponse"));
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

}
