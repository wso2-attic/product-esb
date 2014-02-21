/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

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

package org.wso2.esb.samples.rest;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.testng.Assert.assertTrue;

public class Sample800Test extends ESBIntegrationTestCase {

    @Test(groups = {"wso2.esb"}, description = "Sample 800: Introduction to REST API")
    public void testBasicAPIMediation() throws Exception {
        loadSampleESBConfiguration(800);
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
        String response = doGET(new URL(getMainSequenceURL() + "/stockquote/view/WSO2"));
        System.out.println("Received payload: " + response);
        OMElement responseOM = AXIOMUtil.stringToOM(response);
        assertTrue(responseOM.toString().contains("WSO2"));
    }

    private String doGET(URL url) throws IOException {
        URLConnection conn = url.openConnection ();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        return sb.toString();
    }
}
