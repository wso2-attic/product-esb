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

package org.wso2.esb.integration.nhttp;

import org.testng.annotations.Test;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class WSDLRequestTestCase extends ESBIntegrationTestCase {

    @Override
    protected void init() throws Exception {
        launchBackendHttpServer(null);
    }

    @Test(groups = {"wso2.esb"})
    public void testWSDLRequest() throws IOException {
        loadESBConfigurationFromClasspath("/pass_thru.xml");
        makeGET(new URL(getMainSequenceURL() + "/foo/bar/service?wsdl"));
    }

    private void makeGET(URL url) throws IOException {
        URLConnection conn = url.openConnection ();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        String response = sb.toString();
        log.info("Response received: " + response);
        log.error(response != null && response.length() > 0);
    }
}
