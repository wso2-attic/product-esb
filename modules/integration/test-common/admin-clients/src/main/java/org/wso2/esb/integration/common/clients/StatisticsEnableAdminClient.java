/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.esb.integration.common.clients;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.das.messageflow.data.publisher.stub.DASMessageFlowPublisherAdminStub;
import org.wso2.carbon.das.messageflow.data.publisher.stub.conf.PublisherConfig;
import org.wso2.esb.integration.common.clients.client.utils.AuthenticateStub;

import java.rmi.RemoteException;

public class StatisticsEnableAdminClient {

	DASMessageFlowPublisherAdminStub dasMessageFlowPublisherAdminStub;
	private final String serviceName = "DASMessageFlowPublisherAdmin";

	/**
	 * creation of  StatisticsEnableAdminClient using sessionCookie
	 *
	 * @param backendUrl    backendUrl
	 * @param sessionCookie session Cookie
	 * @throws AxisFault if error while creating the connection
	 */
	public StatisticsEnableAdminClient(String backendUrl, String sessionCookie) throws AxisFault {
		String endPoint = backendUrl + serviceName;
		dasMessageFlowPublisherAdminStub = new DASMessageFlowPublisherAdminStub(endPoint);
		AuthenticateStub.authenticateStub(sessionCookie, dasMessageFlowPublisherAdminStub);
	}

	/**
	 * Creation of StatisticsEnableAdminClient using userName and password
	 *
	 * @param backendUrl backendUrl
	 * @param userName   user name
	 * @param password   password
	 * @throws AxisFault if error while creating the connection
	 */

	public StatisticsEnableAdminClient(String backendUrl, String userName, String password) throws AxisFault {
		String endPoint = backendUrl + serviceName;
		dasMessageFlowPublisherAdminStub = new DASMessageFlowPublisherAdminStub(endPoint);
		AuthenticateStub.authenticateStub(userName, password, dasMessageFlowPublisherAdminStub);
	}

	/**
	 * Add Statistic configuration to the esb
	 */
	public void addStatisticsConfiguration() throws RemoteException {
		PublisherConfig publisherConfig = new PublisherConfig();
		publisherConfig.setPassword("admin");
		publisherConfig.setUserName("admin");
		publisherConfig.setUrl("tcp://127.0.0.1:8462");
		publisherConfig.setMessageFlowPublishingEnabled(true);
		dasMessageFlowPublisherAdminStub.configureEventing(publisherConfig);
	}

	/**
	 * Remove Statistic configuration to the esb
	 */
	public void removeAllStatisticsConfiguration() throws RemoteException {
		PublisherConfig[] publisherConfigs = dasMessageFlowPublisherAdminStub.getAllPublisherNames();
		for (PublisherConfig config : publisherConfigs) {
			dasMessageFlowPublisherAdminStub.removeServer(config.getServerId());
		}
	}
}
