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

package org.wso2.carbon.mediator.log.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

public class ESBTestServerManager extends TestServerManager{

    private static final Log log = LogFactory.getLog(ESBTestServerManager.class);

    @Override
    @BeforeSuite(timeOut = 120000)
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);

        log.debug("Test Start Server Success");
        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 120000)
    public void stopServer() throws Exception {
        log.debug("Test Stop Server Success");
        super.stopServer();
    }

    @Override
    protected void copyArtifacts(String s) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
