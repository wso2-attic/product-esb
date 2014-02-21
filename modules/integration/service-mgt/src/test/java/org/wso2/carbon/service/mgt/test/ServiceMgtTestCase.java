package org.wso2.carbon.service.mgt.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.ParameterUtil;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.carbon.proxyadmin.stub.types.carbon.Entry;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.carbon.service.mgt.stub.ServiceAdminServerException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ServiceMgtTestCase extends ESBIntegrationTestCase {
    private static final Log log = LogFactory.getLog(ServiceMgtTestCase.class);

    private LoginLogoutUtil util;
    private ServiceAdminStub serviceAdminStub;
    private ProxyServiceAdminStub proxyServiceAdminStub;

    private String proxyName = "MyProxy";
    private String parameterName = "testParam";
    private String changedParamValue = "tested";

    @BeforeClass(groups = {"wso2.esb"}, alwaysRun = true)
    public void login() throws java.lang.Exception {
        super.init();
        util = new LoginLogoutUtil();
        String loggedInSessionCookie = util.login();
        initialize(loggedInSessionCookie);
    }

    private void initialize(String sessionCookie) throws AxisFault {
        serviceAdminStub =
                new ServiceAdminStub(getAdminServiceURL("ServiceAdmin"));
        ServiceClient client = serviceAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                sessionCookie);

        proxyServiceAdminStub =
                new ProxyServiceAdminStub(getAdminServiceURL("ProxyServiceAdmin"));
        ServiceClient proxyClient = proxyServiceAdminStub._getServiceClient();
        Options proxyOptions = proxyClient.getOptions();
        proxyOptions.setManageSession(true);
        proxyOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                sessionCookie);

    }

    @AfterClass(groups = {"wso2.esb"})
    public void logout() throws java.lang.Exception {
        int port = 9443;
        ClientConnectionUtil.waitForPort(port);
        util.logout();
    }

    @Test(groups = {"wso2.esb"})
    public void changeParamForProxyService() throws XMLStreamException,
            FileNotFoundException, RemoteException,
            ServiceAdminServerException, ProxyServiceAdminProxyAdminException {
        loadESBConfigurationFromClasspath("/synapse-config.xml");

        String[] params = serviceAdminStub.getServiceParameters(proxyName);
        assertTrue(params != null, "No Parameters found  for the axis2 service of proxy -"+proxyName+", is not set correctly");

        assertTrue(changeParam(params), "Test Parameter in configuration is not available in " +
                "the axis2 service of proxy service");

        serviceAdminStub.setServiceParameters(proxyName, params);


        ProxyData proxyData = proxyServiceAdminStub.getProxy(proxyName);
        Entry[] entries = proxyData.getServiceParams();

        assertTrue(entries != null, "No Parameters found in the proxy service, after modifying axis2 parameter");
        boolean found = false;

        for (Entry entry: entries){
            if(entry.getKey().equalsIgnoreCase(parameterName)){
               found = true;
               assertTrue(entry.getValue().equalsIgnoreCase(changedParamValue), "Modification in the axis2 service " +
                       "of proxy - " +proxyName+
                       "is not propagated in the synapse proxy configuration");
               break;
            }
        }

        assertTrue(found, "Parameter - "+parameterName+" is not found in the synapse proxy configuration," +
                " after modifying the parameter in the axis2 service off proxy" );

    }

    private boolean changeParam(String[] params) throws XMLStreamException, AxisFault {
        if (null != params) {
            if (params.length > 0) {
                boolean changed = false;
                int iter = 0;
                for (String parameterStr : params) {
                    OMElement paramEle;
                    XMLStreamReader xmlSR = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(
                            parameterStr.getBytes()));
                    paramEle = new StAXOMBuilder(xmlSR).getDocumentElement();
                    Parameter parameter = ParameterUtil.createParameter(paramEle);
                    if ((parameter.getParameterType() == Parameter.TEXT_PARAMETER) &&
                            parameter.getName().equalsIgnoreCase(parameterName)) {
                        paramEle.setText(changedParamValue);
                        params[iter] = paramEle.toString();
                        changed = true;
                        break;
                    }
                    iter++;
                }
                return changed;
            } else return false;
        } else {
            return false;
        }
    }


}
