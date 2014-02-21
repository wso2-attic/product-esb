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

package org.wso2.esb.integration;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.http.RequestInterceptor;
import org.wso2.esb.integration.http.SimpleHttpServer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Abstract class for all ESB integration tests. All WSO2 ESB related integration tests are
 * expected to be subclasses of this. This implementation provides a range of useful utilities
 * for updating ESB configuration, loading sample configurations, and starting up various backend
 * services. In addition to the utilities, this class also provides an init method and a cleanup
 * method which can be overriden without any TestNG annotations to obtain before-test-init and
 * after-test-cleanup semantics.
 */
public abstract class ESBIntegrationTestCase {
    
    private static final int ESB_HTTP_PORT = 8280;
    private static final int ESB_HTTPS_PORT = 8243;
    private static final int ESB_SERVLET_HTTP_PORT = 9763;
    private static final int ESB_SERVLET_HTTPS_PORT = 9443;
    
    protected Log log = LogFactory.getLog(getClass());

    private String adminService;
    protected BackendServer backendServer;
    
    public ESBIntegrationTestCase() {
        
    }
    
    public ESBIntegrationTestCase(String adminService) {
        this.adminService = adminService;
    }

    @BeforeMethod(groups = "wso2.esb")
    public final void doInit() throws Exception {
        init();
    }

    @AfterMethod(groups = "wso2.esb")
    public final void doCleanup() {
        cleanup();    
    }

    /**
     * This method gets called before each test method. Override this to do any sort of
     * test resource initialization so that everything is properly setup before the
     * test methods are invoked. Make sure the overriding method does not have any TestNG
     * annotations.
     *
     * @throws Exception on error
     */
    protected void init() throws Exception {
        
    }

    /**
     * This method gets called after each test method. By default this method will attempt
     * to shutdown any backend server instances spawned by the test. More logic can be included
     * in this method by overriding it. Overriding implementations should invoke super.cleanup().
     * Make sure the overriding method does not have any TestNG annotations.
     */
    protected void cleanup() {
        if (backendServer != null && backendServer.isStarted()) {
            try {
                backendServer.stop();
            } catch (IOException e) {
                log.warn("Error while shutting down the backend server", e);
            }
        }
    }

    /**
     * Authenticate the given web service stub against the ESB user manager. This
     * will make it possible to use the stub for invoking ESB admin services.
     *
     * @param stub Axis2 service stub which needs to be authenticated
     */
    protected void authenticate(Stub stub) {
        CarbonUtils.setBasicAccessSecurityHeaders(FrameworkSettings.USER_NAME,
                FrameworkSettings.PASSWORD, stub._getServiceClient());
    }

    /**
     * If this test instance was initialized with a name of an admin service, this
     * method returns the URL of the specified admin service. Otherwise it returns
     * null.
     *
     * @return An admin service URL or null
     */
    protected String getAdminServiceURL() {
        if (adminService != null) {
            return getAdminServiceURL(adminService);
        }
        return null;
    }

    /**
     * Returns the URL of the specified admin service. This method does not validate
     * whether the specified admin service name is real.
     *
     * @param service Name of an admin service
     * @return URL of the specified service
     */
    protected String getAdminServiceURL(String service) {
        return "https://" + FrameworkSettings.HOST_NAME + ":" + ESB_SERVLET_HTTPS_PORT +
                "/services/" + service;
    }

    protected String getProxyServiceURL(String service, boolean servletTransport) {
        int port = ESB_HTTP_PORT;
        if (servletTransport) {
            port = ESB_SERVLET_HTTP_PORT;
        }

        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            return "http://" + FrameworkSettings.HOST_NAME + ":" + port + "/services/" +
                    service;
        } else {
            //TODO: Fix this to return a proper URL
            return "http://" + FrameworkSettings.HOST_NAME + ":" + port +
                    "/services/" + FrameworkSettings.TENANT_NAME + "/";
        }
    }

    protected String getMainSequenceURL() {
        return "http://" + FrameworkSettings.HOST_NAME + ":" + ESB_HTTP_PORT;
    }

    /**
     * Loads the specified resource from the classpath and returns its content as an OMElement.
     *
     * @param path  A relative path to the resource file
     * @return An OMElement containing the resource content
     */
    protected OMElement loadClasspathResource(String path) {
        OMElement documentElement = null;
        FileInputStream inputStream;
        File file = new File((getClass().getResource(path).getPath()));
        if (file.exists()) {
            try {
                inputStream = new FileInputStream((getClass().getResource(path).getPath()));
                XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
                //create the builder
                StAXOMBuilder builder = new StAXOMBuilder(parser);
                //get the root element (in this case the envelope)
                documentElement = builder.getDocumentElement();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        return documentElement;
    }

    /**
     * Loads the specified ESB configuration file from the classpath and deploys it into the ESB.
     *
     * @param filePath A relative path to the configuration file
     * @throws RemoteException If an error occurs while loading the specified configuration
     */
    protected void loadESBConfigurationFromClasspath(String filePath) throws RemoteException {
        OMElement configElement = loadClasspathResource(filePath);
        updateESBConfiguration(configElement);
    }

    /**
     * Loads the configuration of the specified sample into the ESB.
     *
     * @param number Sample number
     * @throws Exception If an error occurs while loading the sample configuration
     */
    protected void loadSampleESBConfiguration(int number) throws Exception {
        String filePath = "repository" + File.separator + "samples" + File.separator +
                "synapse_sample_" + number + ".xml";
        File configFile = new File(filePath);
        FileInputStream inputStream = new FileInputStream(configFile.getAbsolutePath());
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement documentElement = builder.getDocumentElement();
        updateESBConfiguration(documentElement);
    }

    /**
     * Deploys the specified service in the sample Axis2 server and starts it up. See
     * SampleAxis2Server for a set of constants defining the available service names.
     *
     * @param service Name of the service to be started
     * @throws IOException If an error occurs while deploying the service or launching the server
     */
    protected void launchBackendAxis2Service(String service) throws IOException {
        backendServer = new SampleAxis2Server();
        backendServer.deployService(service);
        backendServer.start();
    }

    /**
     * Starts the simple HTTP server and registers the specified RequestInterceptor with it
     *
     * @param interceptor an interceptor to be registered with the service
     * @throws IOException If an error occurs while starting the sample server
     */
    protected void launchBackendHttpServer(RequestInterceptor interceptor) throws IOException {
        backendServer = new SimpleHttpServer();
        backendServer.start();
        if (interceptor != null) {
            backendServer.deployService(interceptor);
        }
    }

    private void updateESBConfiguration(OMElement config) throws RemoteException {
        ConfigServiceAdminStub configServiceAdminStub =
                new ConfigServiceAdminStub(getAdminServiceURL("ConfigServiceAdmin"));
        authenticate(configServiceAdminStub);
        configServiceAdminStub.updateConfiguration(config);
        configServiceAdminStub.cleanup();
    }
}
