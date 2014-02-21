package org.wso2.carbon.esb.scenarios.test.healthcare;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.aarservices.stub.ExceptionException;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.axis2serverutils.SampleAxis2Server;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.carbon.esb.util.ESBTestConstant;
import org.wso2.carbon.esb.util.ServiceDeploymentUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

public class HealthCareScenarioServerStartupTestCase {
    private SampleAxis2Server axis2Server1 = null;
    EnvironmentBuilder builder = null;
    private String[] serviceNames = {"geows", "hcfacilitylocator", "hcinformationservice"};

    @BeforeTest(alwaysRun = true)
    public void deployServices()
            throws IOException, LoginAuthenticationExceptionException, ExceptionException {

        if (FrameworkFactory.getFrameworkProperties(ProductConstant.ESB_SERVER_NAME).getEnvironmentSettings().is_builderEnabled()) {
            axis2Server1 = new SampleAxis2Server("test_axis2_server_9009.xml");
            axis2Server1.start();
            axis2Server1.deployService("geows");
            axis2Server1.deployService("hcfacilitylocator");
            axis2Server1.deployService("hcinformationservice");

        } else {

            builder = new EnvironmentBuilder().as(ProductConstant.ADMIN_USER_ID);

            EnvironmentVariables appServer = builder.build().getAs();

            int deploymentDelay = builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
            ServiceDeploymentUtil deployer = new ServiceDeploymentUtil();
            String serviceFilePath;

            for(String serviceName : this.serviceNames) {
                serviceFilePath = ProductConstant.getResourceLocations(ProductConstant.AXIS2_SERVER_NAME)
                        + File.separator + "aar" + File.separator + serviceName + ".aar";

                deployer.deployArrService(appServer.getBackEndUrl(), appServer.getSessionCookie()
                        , serviceName, serviceFilePath, deploymentDelay);
            }
        }
    }

    @AfterTest(alwaysRun = true)
    public void unDeployServices()
            throws MalformedURLException, LoginAuthenticationExceptionException, ExceptionException,
            RemoteException {
        if (axis2Server1 != null && axis2Server1.isStarted()) {
            axis2Server1.stop();
        } else {
            if (builder != null) {
                EnvironmentVariables appServer = builder.build().getAs();
                int deploymentDelay = builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();

                for (String service: this.serviceNames) {
                    ServiceDeploymentUtil deployer = new ServiceDeploymentUtil();
                    deployer.unDeployArrService(appServer.getBackEndUrl(), appServer.getSessionCookie()
                            , service, deploymentDelay);
                }
            }
        }
    }
}
