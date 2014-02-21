/*
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

package org.wso2.esb.integration.axis2;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StockQuoteClient {

    private static final Log log = LogFactory.getLog(StockQuoteClient.class);

    private ConfigurationContext cfgCtx;
    private ServiceClient serviceClient;

    private List<Header> httpHeaders = new ArrayList<Header>();

    public StockQuoteClient() {
        String repositoryPath = "samples" + File.separator + "axis2Client" +
                File.separator + "client_repo";

        File repository = new File(repositoryPath);
        log.info("Using the Axis2 repository path: " + repository.getAbsolutePath());

        try {
            cfgCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    repository.getCanonicalPath(), null);
            serviceClient = new ServiceClient(cfgCtx, null);
            log.info("Sample client initialized successfully...");
        } catch (Exception e) {
            log.error("Error while initializing the StockQuoteClient", e);
        }
    }

    public void setHeader(String localName, String ns, String value) throws AxisFault {
        serviceClient.addStringHeader(new QName(ns, localName), value);
    }

    public OMElement sendSimpleStockQuoteRequest(String trpUrl, String addUrl,
                                                 String symbol) throws AxisFault {

        Options options = getOptions(trpUrl, addUrl);
        serviceClient.setOptions(options);
        return serviceClient.sendReceive(createStandardRequest(symbol));
    }

    public OMElement sendSecuredSimpleStockQuoteRequest(String trpUrl, String addUrl,
                                                 String symbol, String policyPath) throws Exception {

        Options options = getOptions(trpUrl, addUrl);

        if(policyPath != null && !policyPath.equals("")){
            serviceClient.engageModule("addressing");
            serviceClient.engageModule("rampart");
            options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, loadPolicy(policyPath));
        }

        serviceClient.setOptions(options);
        return serviceClient.sendReceive(createStandardRequest(symbol));
    }

    public OMElement sendCustomQuoteRequest(String trpUrl, String addUrl,
                                            String symbol) throws AxisFault {

        Options options = getOptions(trpUrl, addUrl);
        serviceClient.setOptions(options);
        return serviceClient.sendReceive(createCustomQuoteRequest(symbol));
    }

    public OMElement sendMultipleQuoteRequest(String trpUrl, String addUrl,
                                            String symbol, int n) throws AxisFault {

        Options options = getOptions(trpUrl, addUrl);
        serviceClient.setOptions(options);
        return serviceClient.sendReceive(createMultipleQuoteRequest(symbol, n));
    }

    private Options getOptions(String trpUrl, String addUrl) throws AxisFault {
        Options options = new Options();

        if (trpUrl != null && !"null".equals(trpUrl)) {
            options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        if (addUrl != null && !"null".equals(addUrl)) {
            serviceClient.engageModule("addressing");
            options.setTo(new EndpointReference(addUrl));
        }

        options.setAction("urn:getQuote");
        if (httpHeaders.size() > 0) {
            options.setProperty(HTTPConstants.HTTP_HEADERS, httpHeaders);
        }
        return options;
    }

    public void destroy() {
        try {
            serviceClient.cleanup();
        } catch (AxisFault axisFault) {
            log.error("Error while cleaning up the service client", axisFault);
        }
        cfgCtx.cleanupContexts();
        serviceClient = null;
        cfgCtx = null;
    }

    public void addHttpHeader(String name, String value) {
        httpHeaders.add(new Header(name, value));
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
        OMNamespace ns = factory.createOMNamespace(
                "http://services.samples", "ns");
        OMElement chkPrice = factory.createOMElement("CheckPriceRequest", ns);
        OMElement code = factory.createOMElement("Code", ns);
        chkPrice.addChild(code);
        code.setText(symbol);
        return chkPrice;
    }

    private static Policy loadPolicy(String xmlPath) throws Exception {
        
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }

}
