/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.esb.integration.common.utils.clients.cxfclient;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class CXFClient {

	private String service;
	private String port;
	private String nameSpace;
	private Dispatch<SOAPMessage> dispatch;
	private Bus bus;

	public CXFClient(String wsdlURL, String nameSpace, String port, String service) throws MalformedURLException {

		SpringBusFactory bf = new SpringBusFactory();
		URL busFile = CXFClient.class.getResource("/resources/client.xml");
		bus = bf.createBus(busFile.toString());
		BusFactory.setDefaultBus(bus);
		URL wsdl = new URL(wsdlURL);

		this.setNameSpace(nameSpace);
		this.setPort(port);
		this.setService(service);

		QName serviceName = new QName(this.getNameSpace(), this.getService());
		QName portName = new QName(this.getNameSpace(), this.getPort());
		Service s = Service.create(wsdl, serviceName);
		dispatch = s.createDispatch(portName, SOAPMessage.class, Service.Mode.MESSAGE);
	}

	public SOAPMessage sendMessage(String message) throws IOException, SOAPException {
		if (message != null) {
			InputStream is = new ByteArrayInputStream(message.getBytes("UTF-8"));
			SOAPMessage soapReq1 = MessageFactory.newInstance().createMessage(null, is);
			return dispatch.invoke(soapReq1);
		}
		return null;
	}

	public void destroy() {
		if (bus != null) {
			bus.shutdown(true);
		}
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}
}