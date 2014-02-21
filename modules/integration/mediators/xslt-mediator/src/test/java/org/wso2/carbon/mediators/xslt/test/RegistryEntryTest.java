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


package org.wso2.carbon.mediators.xslt.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.registry.resource.stub.ExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.esb.integration.ESBIntegrationTest;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import javax.activation.DataHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

public class RegistryEntryTest extends ESBIntegrationTest {

    private StockQuoteClient axis2Client;

    @Override
    public void init() {
        super.init();
        axis2Client = new StockQuoteClient();
    }

    @Override
    public void successfulScenario() throws RemoteException {
        uploadResources();
        updateESBConfiguration("/registryentry.xml");

        launchStockQuoteService();

        try {
            OMElement response = axis2Client.sendCustomQuoteRequest(getMainSequenceURL(),
                    null, "WSO2");
            String str = response.toString();
            log.info("Response received: " + str);
            assertTrue(str.contains("CheckPriceResponse"));
            assertTrue(str.contains("WSO2"));
        } catch (AxisFault axisFault) {
            handleError("Error while invoking the ESB endpoint", axisFault);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        axis2Client.destroy();
    }

    private void uploadResources() throws RemoteException {
        ResourceAdminServiceStub resourceAdminServiceStub =
                new ResourceAdminServiceStub(getAdminServiceURL("ResourceAdminService"));
        authenticate(resourceAdminServiceStub);

        try {
            resourceAdminServiceStub.delete("/_system/config/xslt");
            resourceAdminServiceStub.addCollection("/_system/config/", "xslt", "",
                    "Contains test XSLT files");
            resourceAdminServiceStub.addResource(
                    "/_system/config/xslt/transform_back.xslt", "application/xml", "xslt files",
                    new DataHandler(new URL("file:///" + getClass().getResource("/transform_back.xslt").getPath())), null);
            Thread.sleep(1000);
            resourceAdminServiceStub.addResource(
                    "/_system/config/xslt/transform.xslt", "application/xml", "xslt files",
                    new DataHandler(new URL("file:///" + getClass().getResource("/transform.xslt").getPath())), null);
        } catch (MalformedURLException e) {
            handleError("Malformed URL", e);
        } catch (InterruptedException e) {
            handleError("Unexpected interrupt event", e);
        } catch (ExceptionException e) {
            handleError("Error while invoking the resource admin service", e);
        }
    }
}