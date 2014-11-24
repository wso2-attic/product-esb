/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.esb.integration.common.clients.inbound.endpoint;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.stub.InboundAdminInboundManagementException;
import org.wso2.carbon.inbound.stub.InboundAdminStub;
import org.wso2.carbon.inbound.stub.types.carbon.InboundEndpointDTO;
import org.wso2.esb.integration.common.clients.client.utils.AuthenticateStub;

import java.rmi.RemoteException;

public class InboundAdminClient {

    private static final Log log = LogFactory.getLog(InboundAdminClient.class);

    private final String serviceName = "InboundAdmin";
    private InboundAdminStub endpointAdminStub;

    /**
     *
     * @param backEndUrl BackEnd URL
     * @param sessionCookie Session cookie
     * @throws org.apache.axis2.AxisFault
     */
    public InboundAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        endpointAdminStub = new InboundAdminStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, endpointAdminStub);
    }

    /**
     *
     * @param backEndUrl  BackEnd URL
     * @param userName Username
     * @param password Password
     * @throws org.apache.axis2.AxisFault
     */
    public InboundAdminClient(String backEndUrl, String userName, String password)throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        endpointAdminStub = new InboundAdminStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, endpointAdminStub);
    }

     public String getAllInboundEndpointNames() throws RemoteException, InboundAdminInboundManagementException {
         try {
             return endpointAdminStub.getAllInboundEndpointNames();
         } catch (RemoteException e) {
            log.error("Remote Exception occurred when getting Inbound Endpoints",e);
             throw new RemoteException("Error when get endpoint names in InboundAdmin Client",e);
         } catch (InboundAdminInboundManagementException e) {
             log.error("Inbound Admin Management Exception occurred when getting Inbound Endpoints" ,e);
             throw new InboundAdminInboundManagementException("Error when get endpoint names in InboundAdmin Client",e);
         }
     }

    public InboundEndpointDTO getInboundEndpointbyName(String endointName) throws RemoteException,
                                                                                InboundAdminInboundManagementException {
        try {
            return endpointAdminStub.getInboundEndpointbyName(endointName);
        }  catch (RemoteException e) {
            log.error("Remote Exception occurred when getting Inbound Endpoint  by name",e);
            throw new RemoteException("Error when get endpoint names in InboundAdmin Client",e);
        } catch (InboundAdminInboundManagementException e) {
            log.error("Inbound Admin Management Exception occurred when getting Inbound Endpoint by name" ,e);
            throw new InboundAdminInboundManagementException("Inbound Admin Management Exception occurred when getting " +
                                                                                           "Inbound Endpoint by name",e);
        }
    }

    public void addInboundEndpoint(String name, String sequence,
                                   String onError,  String protocol, String classImpl,
                                   String[] sParams) throws RemoteException, InboundAdminInboundManagementException {
        try {
            endpointAdminStub.addInboundEndpoint(name,sequence,onError,protocol,classImpl,sParams);
        } catch (RemoteException e) {
            log.error("Remote Exception occurred when addInboundEndpoint",e);
            throw new RemoteException("Remote Exception occurred when addInboundEndpoint",e);
        } catch (InboundAdminInboundManagementException e) {
            log.error("Error when add inbound endpoint InboundAdmin Client" ,e);
            throw new InboundAdminInboundManagementException("Error when add inbound endpoint InboundAdmin Client",e);
        }
    }

    public void addInboundEndpoint(String element) throws RemoteException, InboundAdminInboundManagementException {
        try {
            endpointAdminStub.addInboundEndpointFromXMLString(element);
        } catch (RemoteException e) {
            log.error("RemoteException when add inbound endpoint InboundAdmin Client",e);
            throw new RemoteException("Error when add inbound endpoint InboundAdmin Client",e);
        }
    }

    public void updateInboundEndpoint(String name, String sequence,
                                      String onError, String protocol, String classImpl,
                                      String[] sParams) throws RemoteException, InboundAdminInboundManagementException {
        try {
            endpointAdminStub.updateInboundEndpoint(name,sequence,onError,protocol,classImpl,sParams);
        } catch (RemoteException e) {
            log.error("Error when update inbound endpoint InboundAdmin Client",e);
            throw new RemoteException("Error when update inbound endpoint InboundAdmin Client",e);
        } catch (InboundAdminInboundManagementException e) {
            log.error("InboundAdminInboundManagementException when update inbound endpoint InboundAdmin Client",e);
            throw new InboundAdminInboundManagementException("Error when update inbound endpoint InboundAdmin Client",e);
        }
    }

    public void removeInboundEndpoint(String name) throws RemoteException, InboundAdminInboundManagementException {
        try {
            endpointAdminStub.removeInboundEndpoint(name);
        } catch (RemoteException e) {
            log.error("RemoteException when removing inbound endpoint InboundAdmin Client",e);
            throw new RemoteException("RemoteException when removing inbound endpoint InboundAdmin Client",e);
        } catch (InboundAdminInboundManagementException e) {
            log.error("InboundAdminInboundManagementException when removing inbound endpoint InboundAdmin Client",e);
            throw new InboundAdminInboundManagementException("InboundAdminInboundManagementException when removing " +
                                                                            "inbound endpoint InboundAdmin Client",e);
        }
    }
}
