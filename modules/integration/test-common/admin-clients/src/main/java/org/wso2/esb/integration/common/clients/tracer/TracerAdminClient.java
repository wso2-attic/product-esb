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
package org.wso2.esb.integration.common.clients.tracer;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.tracer.stub.client.MediationTracerServiceStub;
import org.wso2.esb.integration.common.clients.client.utils.AuthenticateStub;

public class TracerAdminClient {

	 private static final Log log = LogFactory.getLog(TracerAdminClient.class);
	 private final String serviceName = "MediationTracerService";
    private MediationTracerServiceStub tracerAdminStub;

    /**
     * @param backEndUrl    BackEnd URL
     * @param sessionCookie Session cookie
     * @throws org.apache.axis2.AxisFault
     */
    public TracerAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        tracerAdminStub = new MediationTracerServiceStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, tracerAdminStub);
    }

    /**
     * @param backEndUrl BackEnd URL
     * @param userName   Username
     * @param password   Password
     * @throws org.apache.axis2.AxisFault
     */
    public TracerAdminClient(String backEndUrl, String userName, String password) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        tracerAdminStub = new MediationTracerServiceStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, tracerAdminStub);
    }

    /**
     * Return TraceLogs
     * @return TraceLogs
     * @throws java.rmi.RemoteException
     */
    public String[] getTraceLogs() throws java.rmi.RemoteException{
        try {
            return tracerAdminStub.getTraceLogs();
        } catch (java.rmi.RemoteException e) {
            throw new java.rmi.RemoteException("Remote Exception occurred when getTraceLogs" + e);
        }
    }

    /**
     * Return true if TraceLogs cleared else false
     * @return boolean
     * @throws java.rmi.RemoteException
     */
    public boolean clearTraceLogs() throws java.rmi.RemoteException{
        try {
            return tracerAdminStub.clearTraceLogs();
        } catch (java.rmi.RemoteException e) {
            throw new java.rmi.RemoteException("Remote Exception occurred when clearTraceLogs" + e);
        }
    }

    public String[] searchTraceLog(String keyword,boolean ignoreCase) throws java.rmi.RemoteException,org.wso2.carbon.mediation.tracer.stub.client.MediationTracerExceptionException{
        try {
            return tracerAdminStub.searchTraceLog(keyword,ignoreCase);
        } catch (org.wso2.carbon.mediation.tracer.stub.client.MediationTracerExceptionException e) {
            throw new org.wso2.carbon.mediation.tracer.stub.client.MediationTracerExceptionException("MediationTracerExceptionException occurred when getTraceLogs" + e);
        } catch (java.rmi.RemoteException e) {
            throw new java.rmi.RemoteException("Remote Exception occurred when getTraceLogs" + e);
        }
    }

}
