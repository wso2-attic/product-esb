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
package org.wso2.carbon.proxyservices.test.util;

import java.rmi.RemoteException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.integration.core.FrameworkSettings;


public class StockQuoteClient {


    public boolean stockQuoteClient(String trpUrl, String addUrl, String symbol) throws Exception {


        ConfigurationContext cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(FrameworkSettings.CARBON_HOME + "/samples/axis2Client/client_repo/", null);
        //ConfigurationContext cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath.getCanonicalPath(), null);
        OMElement payload = createPayLoad(symbol);
        boolean output = false;

        ServiceClient serviceclient = new ServiceClient(cc, null);
        //ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        if (trpUrl != null && !"null".equals(trpUrl)) {
            opts.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        if (addUrl != null && !"null".equals(addUrl)) {
            serviceclient.engageModule("addressing");
            opts.setTo(new EndpointReference(addUrl));
        }

        opts.setAction("urn:getQuote");
        serviceclient.setOptions(opts);

        try {
            OMElement res = serviceclient.sendReceive(payload);
            output = res.getChildren().next().toString().contains(symbol + " Company");

        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return output;

    }

    public OMElement stockQuoteClientforProxy(String trpUrl, String addUrl, String symbol) throws Exception {


        FrameworkSettings.getProperty();
        ConfigurationContext cc;
        if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(FrameworkSettings.CARBON_HOME + "/client_repo/", null);
        } else {
            cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(FrameworkSettings.CARBON_HOME + "/samples/axis2Client/client_repo/", null);
        }
        OMElement payload = createPayLoad(symbol);
        OMElement output = null;

        ServiceClient serviceclient = new ServiceClient(cc, null);
        Options opts = new Options();

        if (trpUrl != null && !"null".equals(trpUrl)) {
            opts.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        if (addUrl != null && !"null".equals(addUrl)) {
            serviceclient.engageModule("addressing");
            opts.setTo(new EndpointReference(addUrl));
        }

        opts.setAction("urn:getQuote");
        serviceclient.setOptions(opts);

        output = serviceclient.sendReceive(payload);

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * This will generate a client request with addressing headers
     */
    public boolean addressingStockQuoteClient(String epr, String symbol) throws Exception {

        FrameworkSettings.getProperty();
        // ConfigurationContext cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(esbCommon.getCarbonHome() + File.separator + "repository", null);
        ConfigurationContext cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(FrameworkSettings.CARBON_HOME + "/samples/axis2Client/client_repo/", null);
        OMElement payload = createPayLoad(symbol);
        boolean output = false;

        ServiceClient serviceclient = new ServiceClient(cc, null);
        serviceclient.engageModule("addressing");
        Options opts = new Options();
        opts.setTo(new EndpointReference(epr));
        opts.setAction("urn:getQuote");

        serviceclient.setOptions(opts);

        try {
            OMElement res = serviceclient.sendReceive(payload);
            System.out.println(res.getChildren().next());
            output = res.getChildren().next().toString().contains("IBM Company");

        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    /*
    This method will generate a custom quote request
     */
    public boolean customQuoteClient(String trpUrl, String addUrl, String symbol) throws Exception {

        FrameworkSettings.getProperty();
        //ConfigurationContext cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(esbCommon.getCarbonHome() + File.separator + "repository", null);
        ConfigurationContext cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(FrameworkSettings.CARBON_HOME + "/samples/axis2Client/client_repo/", null);
        OMElement payload = createCustomQuoteRequest(symbol);
        //System.out.println(payload.toString());
        boolean output = false;

        ServiceClient serviceclient = new ServiceClient(cc, null);

        Options opts = new Options();

        if (trpUrl != null && !"null".equals(trpUrl)) {
            opts.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        if (addUrl != null && !"null".equals(addUrl)) {
            serviceclient.engageModule("addressing");
            opts.setTo(new EndpointReference(addUrl));
        }

        opts.setAction("urn:getQuote");
        serviceclient.setOptions(opts);

        try {
            OMElement res = serviceclient.sendReceive(payload);
            System.out.println(res.getChildren().next());
            output = res.getChildren().next().toString().contains("IBM");
            output = res.getChildElements().next().toString().contains("IBM");
            System.out.println(output);
            //assertTrue((res.getChildren().next()).toString().indexOf("IBM")>0) ;
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("output_server "+output);
        return output;
    }

    /*
    This method will generate a custom quote request
     */
    public boolean placeOrderClient(String trpUrl, String addUrl) throws Exception {

        FrameworkSettings.getProperty();
        //ConfigurationContext cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(esbCommon.getCarbonHome() + File.separator + "repository", null);
        ConfigurationContext cc = ConfigurationContextFactory.createConfigurationContextFromFileSystem(FrameworkSettings.CARBON_HOME + "/samples/axis2Client/client_repo/", null);
        OMElement payload = createPlaceOrderRequest(10.00, 5, "IBM");
        boolean output = false;

        ServiceClient serviceclient = new ServiceClient(cc, null);

        Options opts = new Options();

        if (trpUrl != null && !"null".equals(trpUrl)) {
            opts.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        if (addUrl != null && !"null".equals(addUrl)) {
            serviceclient.engageModule("addressing");
            opts.setTo(new EndpointReference(addUrl));
        }

        opts.setAction("urn:placeOrder");
        serviceclient.setOptions(opts);

        try {
            serviceclient.fireAndForget(payload);
            Thread.sleep(5000);

        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    /*
    This method created the message payload
     */
    public static OMElement createPayLoad(String symbol) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement method = fac.createOMElement("getQuote", omNs);
        OMElement value1 = fac.createOMElement("request", omNs);
        OMElement value2 = fac.createOMElement("symbol", omNs);

        value2.addChild(fac.createOMText(value1, symbol));
        value1.addChild(value2);
        method.addChild(value1);

        return method;

//        <ns:getQuote xmlns:ns="http://services.samples/xsd">
//            <ns:request>
//              <ns:symbol>IBM</ns:symbol>
//            </ns:request>
//        </ns:getQuote>
    }


    /*
    This method will create a custom quote
     */
    public static OMElement createCustomQuoteRequest(String symbol) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://services.samples", "m0");
        OMElement chkPrice = factory.createOMElement("CheckPriceRequest", ns);
        OMElement code = factory.createOMElement("Code", ns);

        chkPrice.addChild(code);
        code.setText(symbol);
        return chkPrice;
    }

    /*
    This method will create a request required for place orders
     */
    public static OMElement createPlaceOrderRequest(double purchPrice, int qty, String symbol) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://services.samples", "m0");
        OMElement placeOrder = factory.createOMElement("placeOrder", ns);
        OMElement order = factory.createOMElement("order", ns);
        OMElement price = factory.createOMElement("price", ns);
        OMElement quantity = factory.createOMElement("quantity", ns);
        OMElement symb = factory.createOMElement("symbol", ns);
        price.setText(Double.toString(purchPrice));
        quantity.setText(Integer.toString(qty));
        symb.setText(symbol);
        order.addChild(price);
        order.addChild(quantity);
        order.addChild(symb);
        placeOrder.addChild(order);
        return placeOrder;
    }
}



