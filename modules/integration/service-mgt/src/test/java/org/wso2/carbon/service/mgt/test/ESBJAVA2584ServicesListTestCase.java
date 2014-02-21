package org.wso2.carbon.service.mgt.test;

import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.assertTrue;

public class ESBJAVA2584ServicesListTestCase extends ESBIntegrationTestCase {

    private static final Log log = LogFactory.getLog(ESBJAVA2584ServicesListTestCase.class);

    @BeforeClass(groups = {"wso2.esb"}, alwaysRun = true)
    public void initialize() throws java.lang.Exception {
        super.init();
    }

    @Test(groups = {"wso2.esb"})
    public void getServicesList() throws IOException {
        URL url = new URL( getMainSequenceURL() + "/services" );
        URLConnection conn = url.openConnection ();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        String response = sb.toString();
        log.info("Response received: " + response);
        log.error(response != null && response.length() > 0);
  
        assertTrue(response.contains("Deployed services"), "Services list @ http://localhost:8280/services does not list any service.");
        assertTrue(response.contains("services/echo?wsdl"), "Echo service is not listed under services list.");
        assertFalse(response.contains("MessageStoreAdminService"), "Services list should not display the admin services");
        assertFalse(response.contains("RegistryAdminService"), "Services list should not display the admin services");
    }

}

