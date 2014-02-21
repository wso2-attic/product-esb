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

package org.wso2.carbon.mediator.tests.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageAnalyzerUtils {
    public String listenAndForward(int listenport, int forwardPort) throws IOException {
        String message = null;
        ServerSocket entrance = new ServerSocket(listenport);
        Socket s = new Socket();
        s = entrance.accept();
        InputStream inputData = s.getInputStream();

        String msg = new String();
        int a = 0;
        while ( (a = inputData.read()) != -1  )
        {
            byte i = (byte) a;
            String st = new Character((char)i).toString();
            msg += st;
        }
        System.out.println(msg+"\n of length "+msg.length());

        inputData.close();
        s.close();
        entrance.close();
        return msg;
    }
}
