/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.tests.utils;

import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.app.RemoteRegistry;

import java.io.File;
import java.net.URL;

public class InitilizeApi {
    static RemoteRegistry registry = null;
    static String REMOTE_REGISTRY_URL;
    static int iterationsNumber;
    static int concurrentUsers;
    static int workerClass;

    public RemoteRegistry getRegistry(String carbonHome, String httpsPort, String httpPort) {
        try {
            // REMOTE_REGISTRY_URL = System.getProperty("url.property");
            REMOTE_REGISTRY_URL = "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/registry/";
            System.setProperty("javax.net.ssl.trustStore", FrameworkSettings.CARBON_HOME + File.separator + "repository" + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
            registry = new RemoteRegistry(new URL(REMOTE_REGISTRY_URL), "admin", "admin");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return registry;
    }
}