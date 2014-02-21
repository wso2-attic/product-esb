package org.wso2.carbon.mediator.property.test.addToCollection;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;
/*
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */

public class AddToCollectionTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(AddToCollectionTest.class);

    @Override
    public void init() {
        log.info("Initializing Add to Collection Test class ");
        log.debug("Add to Collection Test Initialised");

    }

    @Override
    public void runSuccessCase() {
        log.debug("Running SuccessCase");

        try {

            AuthenticateStub authenticateStub = new AuthenticateStub();
            ResourceAdminServiceStub resourceAdminServiceStub = new ResourceAdminServiceStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ResourceAdminService");
            authenticateStub.authenticateAdminStub(resourceAdminServiceStub, sessionCookie);
            //add a collection to the registry
            String collectionPath = resourceAdminServiceStub.addCollection("/_system/config/", "ResFiles", "", "contains ResFiles");
            log.info("collection added to " + collectionPath);
            // Changing media type
            collectionPath = resourceAdminServiceStub.addCollection("/_system/config/", "ResFiles", "application/vnd.wso2.esb", "application/vnd.wso2.esb media type collection");
            String resource = frameworkPath + File.separator + "components" + File.separator + "mediators-property" + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "addToCollection" + File.separator + "synapse.xml";

            resourceAdminServiceStub.addResource("/ResFiles/synapse.xml", "application/xml", "resDesc", new DataHandler(new URL("file:///" + resource)), null);

            String textContent = resourceAdminServiceStub.getTextContent("/ResFiles/synapse.xml");

            if (textContent.equals(null)) {
                log.error("Unable to get text content");
                Assert.fail("Unable to get text content");
            } else {
                System.out.println("Resource successfully added to the registry and retrieved contents successfully");
            }
            resourceAdminServiceStub.delete("/ResFiles/synapse.xml");

            if (!textContent.equals(null)) {
                System.out.println("Resource successfully deleted from the registry");

            } else {
                log.error("Unable to delete the resource from the registry");
                Assert.fail("Unable to delete the resource from the registry");
            }

        }
        catch (Exception e) {
            Assert.fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());

        }


    }

    @Override
    public void runFailureCase() {

    }

    @Override
    public void cleanup() {

    }
}
