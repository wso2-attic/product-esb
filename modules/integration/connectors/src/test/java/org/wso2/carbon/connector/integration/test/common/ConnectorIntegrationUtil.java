package org.wso2.carbon.connector.integration.test.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.mediation.library.stub.upload.MediationLibraryUploaderStub;
import org.wso2.carbon.mediation.library.stub.upload.types.carbon.LibraryFileItem;

public class ConnectorIntegrationUtil {

	private static final Log log = LogFactory.getLog(ConnectorIntegrationUtil.class);

	public static void uploadConnector(String repoLocation,
	                                   MediationLibraryUploaderStub mediationLibUploadStub,
	                                   String strFileName) throws MalformedURLException,
	                                                      RemoteException {
		List<LibraryFileItem> uploadLibraryInfoList = new ArrayList<LibraryFileItem>();
		LibraryFileItem uploadedFileItem = new LibraryFileItem();
		uploadedFileItem.setDataHandler(new DataHandler(new URL("file:" + File.separator +
		                                                        File.separator + repoLocation +
		                                                        File.separator + strFileName)));
		uploadedFileItem.setFileName(strFileName);
		uploadedFileItem.setFileType("zip");
		uploadLibraryInfoList.add(uploadedFileItem);
		LibraryFileItem[] uploadServiceTypes = new LibraryFileItem[uploadLibraryInfoList.size()];
		uploadServiceTypes = uploadLibraryInfoList.toArray(uploadServiceTypes);
		mediationLibUploadStub.uploadLibrary(uploadServiceTypes);
	}

	public static Properties getConnectorConfigProperties(String connectorName) {
		String connectorConfigFile = null;
		ProductConstant.init();
		try {
			connectorConfigFile =
			                      ProductConstant.SYSTEM_TEST_SETTINGS_LOCATION + File.separator +
			                              "artifacts" + File.separator + "ESB" + File.separator +
			                              "connector" + File.separator + "config" + File.separator +
			                              connectorName + ".properties";
			File connectorPropertyFile = new File(connectorConfigFile);
			InputStream inputStream = null;
			if (connectorPropertyFile.exists()) {
				inputStream = new FileInputStream(connectorPropertyFile);
			}

			if (inputStream != null) {
				Properties prop = new Properties();
				prop.load(inputStream);
				inputStream.close();
				return prop;
			}

		} catch (IOException ignored) {
			log.error("automation.properties file not found, please check your configuration");
		}

		return null;
	}

	public static OMElement sendReceive(OMElement payload, String endPointReference,
	                                    String operation, String contentType) throws AxisFault {
		ServiceClient sender;
		Options options;
		OMElement response = null;
		if (log.isDebugEnabled()) {
			log.debug("Service Endpoint : " + endPointReference);
			log.debug("Service Operation : " + operation);
			log.debug("Payload : " + payload);
		}
		try {
			sender = new ServiceClient();
			options = new Options();
			options.setTo(new EndpointReference(endPointReference));
			options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED,
			                    Boolean.FALSE);
			options.setTimeOutInMilliSeconds(45000);
			options.setAction("urn:" + operation);
			options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
			options.setProperty(Constants.Configuration.MESSAGE_TYPE, contentType);
			sender.setOptions(options);

			response = sender.sendReceive(payload);
			if (log.isDebugEnabled()) {
				log.debug("Response Message : " + response);
			}
		} catch (AxisFault axisFault) {
			log.error(axisFault.getMessage());
			throw new AxisFault("AxisFault while getting response :" + axisFault.getMessage(),
			                    axisFault);
		}
		return response;
	}

}
