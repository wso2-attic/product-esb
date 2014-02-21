package org.wso2.carbon.module.mgt.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.persistence.PersistenceDataNotFoundException;
import org.wso2.carbon.core.persistence.PersistenceUtils;
import org.wso2.carbon.core.persistence.file.ModuleFilePersistenceManager;
import org.wso2.carbon.core.util.ParameterUtil;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.module.mgt.stub.ModuleAdminServiceModuleMgtExceptionException;
import org.wso2.carbon.module.mgt.stub.ModuleAdminServiceStub;
import org.wso2.carbon.module.mgt.stub.types.ModuleMetaData;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

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
public class ModuleMgtTestCase extends ESBIntegrationTestCase {
    private static final Log log = LogFactory.getLog(ModuleMgtTestCase.class);

    private LoginLogoutUtil util;
    private ModuleAdminServiceStub moduleAdminServiceStub;


    private String testValue = "test123";

    private ModuleMetaData testModule;

    private String testParameter;



    public ModuleMgtTestCase() {
        super("ModuleAdminService");
    }

    @BeforeClass(groups = {"wso2.esb"}, alwaysRun = true)
    public void login() throws java.lang.Exception {
        super.init();
        util = new LoginLogoutUtil();
        String loggedInSessionCookie = util.login();

        moduleAdminServiceStub =
                new ModuleAdminServiceStub(getAdminServiceURL());
        ServiceClient client = moduleAdminServiceStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.esb"})
    public void logout() throws java.lang.Exception {
        int port = 9443;
        ClientConnectionUtil.waitForPort(port);
        util.logout();
    }


    @Test(groups = {"wso2.esb"})
    public void changeModuleParamsTest() throws IOException,
            ModuleAdminServiceModuleMgtExceptionException,
            XMLStreamException, PersistenceDataNotFoundException {
        ArrayList<ModuleMetaData> moduleMetaData = getAvailableModules();
        if (moduleMetaData.size() > 0) {
            testModule = getTestModule(moduleMetaData);
            log.info("Using test module :"+testModule.getModulename());
            String[] params = moduleAdminServiceStub
                    .getModuleParameters(testModule.getModulename(),
                            testModule.getModuleVersion());
            boolean modified = changeParam(params);
            if (modified) {
                moduleAdminServiceStub.setModuleParameters(testModule.getModulename(),
                        testModule.getModuleVersion(), params);
                assertTrue(isChangeReflectedInMetaFile(), "Change in the module parameter is not updated");
            }
        } else {
            log.info("No modules available");
        }
    }

    private ModuleMetaData getTestModule(ArrayList<ModuleMetaData> metaDatas) {
        for (ModuleMetaData metaData : metaDatas) {
            if (!metaData.getEngagedGlobalLevel() && !isUnEditableModule(metaData.getModulename())){
                return metaData;
            }
        }
        return null;
    }

    private boolean isUnEditableModule(String moduleName){
      String[] uneditableModules={"wso2throttle"};
        for (String aModule:uneditableModules){
            if(aModule.equalsIgnoreCase(moduleName)){
                return true;
            }
        }
        return false;
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
                    if (parameter.getParameterType() == Parameter.TEXT_PARAMETER) {
                        testParameter = parameter.getName();
                        paramEle.setText(testValue);
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

    private boolean isChangeReflectedInMetaFile() throws XMLStreamException,
            IOException, PersistenceDataNotFoundException {
        String xpathStrOfParent = Resources.ModuleProperties.VERSION_XPATH +
                PersistenceUtils.getXPathAttrPredicate(Resources.ModuleProperties.VERSION_ID,
                        testModule.getModuleVersion());
        String paramXPath;
        if (xpathStrOfParent.equals("/")) {
            paramXPath = xpathStrOfParent + Resources.ParameterProperties.PARAMETER +
                    PersistenceUtils.getXPathAttrPredicate(Resources.NAME, testParameter);
        } else {
            paramXPath = xpathStrOfParent + "/" + Resources.ParameterProperties.PARAMETER +
                    PersistenceUtils.getXPathAttrPredicate(Resources.NAME, testParameter);
        }
        AxisConfiguration axisConfiguration =
                ConfigurationContextFactory.
                        createConfigurationContextFromFileSystem(CarbonUtils.getCarbonRepository(),
                                null).getAxisConfiguration();
        ModuleFilePersistenceManager moduleFPM = new ModuleFilePersistenceManager(axisConfiguration);
        OMElement paramElement = (OMElement) moduleFPM.get(testModule.getModulename(), paramXPath);
        return paramElement.getText().equalsIgnoreCase(testValue);

    }

    private ArrayList<ModuleMetaData> getAvailableModules() throws RemoteException {
        ArrayList<ModuleMetaData> moduleMetaData
                = new ArrayList<ModuleMetaData>();
        if (moduleAdminServiceStub.listModules() != null) {
            for (ModuleMetaData mData : moduleAdminServiceStub.listModules()) {
                moduleMetaData.add(mData);
            }
        }
        return moduleMetaData;
    }
}
