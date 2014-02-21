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
package org.wso2.carbon.esb.tcp.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.automation.utils.esb.ESBTestCaseUtils;
import org.wso2.carbon.esb.ESBIntegrationTest;
import org.wso2.carbon.esb.tcp.transport.test.util.TcpClient;
import org.wso2.carbon.esb.util.ESBTestConstant;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CARBON14021MessageFormatterWithTCPTestCase extends ESBIntegrationTest{
    int portNo=9447;
    Socket port;
    ServerSocket listener;
    
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    protected void init() throws Exception {
        super.init();
        serverConfigurationManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        serverConfigurationManager.applyConfiguration(new File(getESBResourceLocation() + File.separator
                + "tcp" + File.separator + "transport" + File.separator + "axis2.xml"));
        super.init();
        loadESBConfigurationFromClasspath("/artifacts/ESB/proxyconfig/proxy/proxyservice/tcpProxy.xml");



    }

    @Test(groups = "wso2.esb", description = "proxy service to invoke a TCPEndpoint")
    public void testContentTypeSendToTCPEndpoint() throws Exception {
        final CARBON14021MessageFormatterWithTCPTestCase tcpTestCase = new CARBON14021MessageFormatterWithTCPTestCase();
        Thread t1 = new Thread(){
            public void run(){
                try {
                    log.info("starting tcp server--------------------------------------------------");
                    tcpTestCase.startTCPServer();
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        };

        Thread t2 = new Thread(){
            public void run(){
                try {
                    log.info("Executing proxy service------------------------------------------------");
                    TcpClient tcpClient = new TcpClient();
                    String tcProxyUrl;
                    if (isRunningOnStratos()) {
                        tcProxyUrl = "tcp://localhost:8290/services/t/" + userInfo.getDomain() + "/tcpProxy/tcpProxy?contentType=application/soap+xml";
                    } else {
                        tcProxyUrl = "tcp://localhost:8290/services/tcpProxy/tcpProxy?contentType=application/soap+xml";
                    }
                    tcpClient.sendSimpleStockQuote12(tcProxyUrl, "TCPPROXY", tcpClient.CONTENT_TYPE_APPLICATIONS_SOAP_XML);

                } catch (AxisFault axisFault) {
                   log.error(axisFault);
                    try {
                        throw axisFault;
                    } catch (AxisFault axisFault1) {
                        axisFault1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

            }
        };

        t1.start();
        t2.start();


    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        super.cleanup();
    }

    public boolean startTCPServer() throws Exception{
        ServerSocket listner = null;
        while(true) {
            try {
                listner = new ServerSocket(10991);
                Socket port = listner.accept();
                DataInputStream in = new DataInputStream(port.getInputStream());
                System.out.println("connected to----------------------------------- " + port.getRemoteSocketAddress());
                Thread.sleep(500);
                byte[] b = new byte[in.available()];
                in.read(b);
                String values = new String(b);
                System.out.println("sent value-------------------------"+values);
                Assert.assertTrue(values.equals("test1,test2"), "The content type text/plain has not preserved in TCPSender");

                DataOutputStream out = new DataOutputStream(port.getOutputStream());

                out.writeUTF("test !");
                port.close();

            } catch (IOException e) {
              log.error(e.getMessage(),e);
            } finally {
                try {
                    listner.close();
                } catch (IOException e) {
                  log.error(e.getMessage(),e);
                }
            }
        }

    }
}
