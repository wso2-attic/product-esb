/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.esb.integration.common.utils.clients.stockquoteclient;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.esb.integration.common.utils.clients.axis2client.ConfigurationContextProvider;

import java.util.ArrayList;
import java.util.List;

public class StockQuoteClient {

    private static final Log log = LogFactory.getLog(StockQuoteClient.class);

    private List<Header> httpHeaders = new ArrayList<Header>();

    public StockQuoteClient() {

    }

/*    public void setHeader(String localName, String ns, String value) throws AxisFault {
//        serviceClient.addStringHeader(new QName(ns, localName), value);
    }*/

    public void addHttpHeader(String name, String value) {
        httpHeaders.add(new Header(name, value));
    }

    public void clearHttpHeader() {
        httpHeaders.clear();
    }

    public OMElement sendSimpleStockQuoteRequest(String trpUrl, String addUrl, String symbol)
            throws AxisFault {

        ServiceClient sc = new ServiceClient();
        sc=getServiceClient(trpUrl, addUrl);

        try {
            return buildResponse(sc.sendReceive(createStandardRequest(symbol)));
        } finally {
            sc.cleanupTransport();
        }
    }

    public OMElement sendSimpleStockQuoteRequest_REST(String trpUrl, String addUrl, String symbol)
            throws AxisFault {
        ServiceClient sc = getRESTEnabledServiceClient(trpUrl, addUrl);
        try {
            return buildResponse(sc.sendReceive(createStandardRequest(symbol)));
        } finally {
            sc.cleanupTransport();
        }

    }

    public OMElement sendSimpleQuoteRequest(String trpUrl, String addUrl, String symbol)
            throws AxisFault {

        ServiceClient sc = getServiceClient(trpUrl, addUrl, "getSimpleQuote");
        try {
            return buildResponse(sc.sendReceive(createStandardSimpleRequest(symbol)));
        } finally {
            sc.cleanupTransport();
        }
    }

    public OMElement sendSimpleQuoteRequest_REST(String trpUrl, String addUrl, String symbol)
            throws AxisFault {

        ServiceClient serviceClient = getRESTEnabledServiceClient(trpUrl, addUrl, "getSimpleQuote");
        try {
            return buildResponse(serviceClient.sendReceive(createStandardSimpleRequest(symbol)));
        } finally {
            serviceClient.cleanupTransport();
        }
    }

    public OMElement sendSimpleStockQuoteSoap11(String trpUrl, String addUrl, String symbol)
            throws AxisFault {

        ServiceClient serviceClient = getServiceClient(trpUrl, addUrl);
        serviceClient.getOptions()
                .setSoapVersionURI(org.apache.axiom.soap.SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        try {
            return buildResponse(serviceClient.sendReceive(createStandardRequest(symbol)));
        } finally {
            serviceClient.cleanupTransport();
        }
    }


    public OMElement sendSimpleStockQuoteSoap12(String trpUrl, String addUrl, String symbol)
            throws AxisFault {

        ServiceClient serviceClient = getServiceClient(trpUrl, addUrl);
        serviceClient.getOptions()
                .setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        try {
            return buildResponse(serviceClient.sendReceive(createStandardRequest(symbol)));
        } finally {
            serviceClient.cleanupTransport();
        }
    }

    public OMElement sendSimpleStockQuoteRequest(String trpUrl, String addUrl, OMElement payload)
            throws AxisFault {

        ServiceClient serviceClient = getServiceClient(trpUrl, addUrl);
        try {
            return buildResponse(serviceClient.sendReceive(payload));
        } finally {
            serviceClient.cleanupTransport();
        }
    }

    public OMElement sendSimpleStockQuoteRequest_REST(String trpUrl, String addUrl,
                                                      OMElement payload)
            throws AxisFault {

        ServiceClient serviceClient = getRESTEnabledServiceClient(trpUrl, addUrl);
        try {
            return buildResponse(serviceClient.sendReceive(payload));
        } finally {
            serviceClient.cleanupTransport();
        }
    }


    public OMElement sendCustomQuoteRequest(String trpUrl, String addUrl, String symbol)
            throws AxisFault {

        ServiceClient serviceClient = getServiceClient(trpUrl, addUrl);
        try {
            return buildResponse(serviceClient.sendReceive(createCustomQuoteRequest(symbol)));
        } finally {
            serviceClient.cleanupTransport();
        }
    }

    public OMElement send(String trpUrl, String addUrl, String action, OMElement payload)
            throws AxisFault {

        ServiceClient serviceClient = getServiceClient(trpUrl, addUrl, action);
        try {
            return buildResponse(serviceClient.sendReceive(payload));
        } finally {
            serviceClient.cleanupTransport();
        }
    }


    public OMElement sendCustomQuoteRequest_REST(String trpUrl, String addUrl, String symbol)
            throws AxisFault {

        ServiceClient serviceClient = getRESTEnabledServiceClient(trpUrl, addUrl);
        try {
            return buildResponse(serviceClient.sendReceive(createCustomQuoteRequest(symbol)));
        } finally {
            serviceClient.cleanupTransport();
        }
    }

    public OMElement sendMultipleQuoteRequest(String trpUrl, String addUrl, String symbol, int n)
            throws AxisFault {

        ServiceClient serviceClient = getServiceClient(trpUrl, addUrl);
        try {
            return buildResponse(serviceClient.sendReceive(createMultipleQuoteRequest(symbol, n)));
        } finally {
            serviceClient.cleanupTransport();
        }
    }

    public OMElement sendMultipleQuoteRequest_REST(String trpUrl, String addUrl, String symbol,
                                                   int n)
            throws AxisFault {

        ServiceClient serviceClient = getRESTEnabledServiceClient(trpUrl, addUrl);
        try {
            return buildResponse(serviceClient.sendReceive(createMultipleQuoteRequest(symbol, n)));
        } finally {
            serviceClient.cleanupTransport();
        }
    }

    private ServiceClient getServiceClient(String trpUrl, String addUrl) throws AxisFault {

        return getServiceClient(trpUrl, addUrl, "getQuote");
    }

    private ServiceClient getRESTEnabledServiceClient(String trpUrl, String addUrl)
            throws AxisFault {

        ServiceClient serviceClient = getServiceClient(trpUrl, addUrl);
        serviceClient.getOptions().setProperty("enableREST", "true");
        return serviceClient;
    }

    private ServiceClient getServiceClient(String trpUrl, String addUrl, String operation)
            throws AxisFault {

        ServiceClient serviceClient;
        Options options = new Options();

        if (addUrl != null && !"null".equals(addUrl)) {
            serviceClient = new ServiceClient(ConfigurationContextProvider.getInstance().getConfigurationContext(), null);
            serviceClient.engageModule("addressing");
            options.setTo(new EndpointReference(addUrl));
        } else {
            //otherwise it will engage addressing all the time once addressing is engaged by ConfigurationContext to service client
            serviceClient = new ServiceClient();
        }

        if (trpUrl != null && !"null".equals(trpUrl)) {
            options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        options.setAction("urn:" + operation);
        if (httpHeaders.size() > 0) {
            options.setProperty(HTTPConstants.HTTP_HEADERS, httpHeaders);
        }
      /*  options.setProperty(HTTPConstants.CHUNKED, Constants.VALUE_FALSE);
        options.setProperty(Constants.Configuration.MESSAGE_TYPE,HTTPConstants.MEDIA_TYPE_APPLICATION_ECHO_XML);
        options.setProperty(Constants.Configuration.DISABLE_SOAP_ACTION,Boolean.TRUE);*/
        serviceClient.setOptions(options);

        return serviceClient;
    }

    private ServiceClient getRESTEnabledServiceClient(String trpUrl, String addUrl,
                                                      String operation)
            throws AxisFault {
        ServiceClient serviceClient = getServiceClient(trpUrl, addUrl, operation);
        serviceClient.getOptions().setProperty("enableREST", "true");

        return serviceClient;
    }

    public void destroy() {
        //to keep backward compatibility
    }

    private OMElement createStandardRequest(String symbol) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement method = fac.createOMElement("getQuote", omNs);
        OMElement value1 = fac.createOMElement("request", omNs);
        OMElement value2 = fac.createOMElement("symbol", omNs);

        value2.addChild(fac.createOMText(value1, symbol));
        value1.addChild(value2);
        method.addChild(value1);

        return method;
    }

    private OMElement createStandardSimpleRequest(String symbol) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement method = fac.createOMElement("getSimpleQuote", omNs);
        OMElement value1 = fac.createOMElement("symbol", omNs);

        value1.addChild(fac.createOMText(method, symbol));
        method.addChild(value1);

        return method;
    }

    private OMElement createMultipleQuoteRequest(String symbol, int iterations) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://services.samples", "ns");
        OMElement method = fac.createOMElement("getQuote", omNs);

        for (int i = 0; i < iterations; i++) {
            OMElement value1 = fac.createOMElement("request", omNs);
            OMElement value2 = fac.createOMElement("symbol", omNs);
            value2.addChild(fac.createOMText(value1, symbol));
            value1.addChild(value2);
            method.addChild(value1);
        }
        return method;
    }

    private OMElement createCustomQuoteRequest(String symbol) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("http://services.samples", "ns");
        OMElement chkPrice = factory.createOMElement("CheckPriceRequest", ns);
        OMElement code = factory.createOMElement("Code", ns);
        chkPrice.addChild(code);
        code.setText(symbol);
        return chkPrice;
    }

    private static OMElement buildResponse(OMElement omElement) {
        omElement.build();
        return omElement;
    }
}
