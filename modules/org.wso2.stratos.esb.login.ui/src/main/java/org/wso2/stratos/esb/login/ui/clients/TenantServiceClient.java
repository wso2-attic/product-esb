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
package org.wso2.stratos.esb.login.ui.clients;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.AxisFault;
import org.wso2.stratos.esb.login.ui.TenantConfigServiceStub;
import org.wso2.stratos.esb.login.ui.beans.xsd.TenantInfoBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.CarbonConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;

public class TenantServiceClient {
     private static final Log log = LogFactory.getLog(TenantServiceClient.class);

    private TenantConfigServiceStub stub;
    private String epr;

    public TenantServiceClient(
            String cookie, String backendServerURL, ConfigurationContext configContext)
            throws RegistryException {

        epr = backendServerURL + "TenantConfigService";

        try {
            stub = new TenantConfigServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate AddServices service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public TenantServiceClient(ServletConfig config, HttpSession session)
            throws RegistryException {

        String cookie = (String)session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.
                getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        epr = backendServerURL + "TenantConfigService";

        try {
            stub = new TenantConfigServiceStub(configContext, epr);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        } catch (AxisFault axisFault) {
            String msg = "Failed to initiate Add Services service client. " + axisFault.getMessage();
            log.error(msg, axisFault);
            throw new RegistryException(msg, axisFault);
        }
    }

    public void addTenant(TenantInfoBean tenantInfoBean) throws Exception {
       stub.addTenant(tenantInfoBean);
    }
    public String proceedAddTenant(TenantInfoBean tenantInfoBean, String confirmationKey)throws Exception{
        return stub.proceedTenantAdding(tenantInfoBean, confirmationKey);
    }
    public TenantInfoBean[] getTenants() throws Exception {
        return stub.getTenants();
    }
    
    public TenantInfoBean getTenant(String domainName) throws Exception {
        return stub.getTenant(domainName);
    }

    public void updateTenant(TenantInfoBean tenantInfoBean, String oldPassword) throws Exception {
        stub.updateTenant(tenantInfoBean, oldPassword);
    }

    public void activateTenant(String domainName) throws Exception {
        stub.activateTenant(domainName);
    }

    public void deactivateTenant(String domainName) throws Exception {
        stub.deactivateTenant(domainName);
    }
    
    public boolean checkDomainAvailability(String domainName) throws Exception {
        return stub.checkDomainAvailability(domainName);
    }

    public String validateOrSuggestDomain(String domainName, String successKey) throws Exception {
        return stub.validateOrSuggestDomain(domainName, successKey);
    }
}
