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

package org.wso2.carbon.esb.mediator.test.datamapper;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.clients.registry.ResourceAdminServiceClient;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DataMapperOneToOneTestCase extends DataMapperIntegrationTest {

    private final String ARTIFACT_ROOT_PATH = "/artifacts/ESB/mediatorconfig/datamapper/one_to_one/";
    private final String REGISTRY_ROOT_PATH = "datamapper/one_to_one/";

    @Test(groups = {"wso2.esb"}, description = "Datamapper simple one to one xml to xml conversion")
    public void testOneToOneXmlToXml() throws Exception {
        loadESBConfigurationFromClasspath(ARTIFACT_ROOT_PATH + "xml_to_xml/" + File.separator + "synapse.xml");
        uploadResourcesToGovernanceRegistry(REGISTRY_ROOT_PATH + "xml_to_xml/", ARTIFACT_ROOT_PATH + "xml_to_xml" + File.separator);

        String request =
                "   <company>\n" +
                        "      <name>WSO2</name>\n" +
                        "      <usoffice>\n" +
                        "         <address>\n" +
                        "            <no>787</no>\n" +
                        "            <street>Castro Street,Mountain View</street>\n" +
                        "            <city>CA</city>\n" +
                        "            <code>94041</code>\n" +
                        "            <country>US</country>\n" +
                        "         </address>\n" +
                        "         <phone> +1 650 745 4499</phone>\n" +
                        "         <fax> +1 408 689 4328</fax>\n" +
                        "      </usoffice>\n" +
                        "      <europeoffice>\n" +
                        "         <address>\n" +
                        "            <no>2-6 </no>\n" +
                        "            <street>Boundary Row</street>\n" +
                        "            <city>London</city>\n" +
                        "            <code>SE1 8HP</code>\n" +
                        "            <country>UK</country>\n" +
                        "         </address>\n" +
                        "         <phone>+44 203 318 6025</phone>\n" +
                        "      </europeoffice>\n" +
                        "      <asiaoffice>\n" +
                        "         <address>\n" +
                        "            <no>20</no>\n" +
                        "            <street>Palm Grove</street>\n" +
                        "            <city>Colombo 03</city>\n" +
                        "            <code>10003</code>\n" +
                        "            <country>LKA</country>\n" +
                        "         </address>\n" +
                        "         <phone>+94 11 214 5345</phone>\n" +
                        "         <fax>+94 11 2145300</fax>\n" +
                        "      </asiaoffice>\n" +
                        "   </company>\n";
        String response = sendRequest(getProxyServiceURLHttp("OneToOneXmlToXml"), request, "text/xml");
        Assert.assertEquals(response, "<company><offices><usoffice><address>WSO2787CA</address><phone> +1 650 745 4499</phone><fax> +1 408 689 4328</fax></usoffice><europeoffice><address>WSO22-6 London</address><phone>+44 203 318 6025</phone></europeoffice><asiaoffice><address>WSO220Colombo 03</address><phone>+94 11 214 5345</phone><fax>+94 11 2145300</fax></asiaoffice></offices></company>");
    }

    @Test(groups = {"wso2.esb"}, description = "Datamapper simple one to one json to json conversion")
    public void testOneToOneJsonToJson() throws Exception {
        loadESBConfigurationFromClasspath(ARTIFACT_ROOT_PATH + "json_to_json/" + File.separator + "synapse.xml");
        uploadResourcesToGovernanceRegistry(REGISTRY_ROOT_PATH + "json_to_json/", ARTIFACT_ROOT_PATH + "json_to_json" + File.separator);


        String request = "{\n" +
                "    \"name\": \"WSO2\",\n" +
                "    \"usoffice\": {\n" +
                "      \"address\": {\n" +
                "        \"no\": \"787\",\n" +
                "        \"street\": \"Castro Street,Mountain View\",\n" +
                "        \"city\": \"CA\",\n" +
                "        \"code\": \"94041\",\n" +
                "        \"country\": \"US\"\n" +
                "      },\n" +
                "      \"phone\": \" +1 650 745 4499\",\n" +
                "      \"fax\": \" +1 408 689 4328\"\n" +
                "    },\n" +
                "    \"europeoffice\": {\n" +
                "      \"address\": {\n" +
                "        \"no\": \"2-6 \",\n" +
                "        \"street\": \"Boundary Row\",\n" +
                "        \"city\": \"London\",\n" +
                "        \"code\": \"SE1 8HP\",\n" +
                "        \"country\": \"UK\"\n" +
                "      },\n" +
                "      \"phone\": \"+44 203 318 6025\",\n" +
                "      \"fax\": \"+44 11 2145300\"\n" +
                "    },\n" +
                "    \"asiaoffice\": {\n" +
                "      \"address\": {\n" +
                "        \"no\": \"20\",\n" +
                "        \"street\": \"Palm Grove\",\n" +
                "        \"city\": \"Colombo 03\",\n" +
                "        \"code\": \"10003\",\n" +
                "        \"country\": \"LKA\"\n" +
                "      },\n" +
                "      \"phone\": \"+94 11 214 5345\",\n" +
                "      \"fax\": \"+94 11 2145300\"\n" +
                "    }\n" +
                "}\n";

        String response = sendRequest(getProxyServiceURLHttp("OneToOneJsonToJson"), request, "application/json");
        Assert.assertEquals(response, "{\"offices\":{\"usoffice\":{\"address\":\"WSO2787CA\",\"phone\":\" +1 650 745 4499\",\"fax\":\" +1 408 689 4328\"},\"europeoffice\":{\"address\":\"WSO22-6 London\",\"phone\":\"+44 203 318 6025\",\"fax\":\"+44 11 2145300\"},\"asiaoffice\":{\"address\":\"WSO220Colombo 03\",\"phone\":\"+94 11 214 5345\",\"fax\":\"+94 11 2145300\"}}}");
    }

}

