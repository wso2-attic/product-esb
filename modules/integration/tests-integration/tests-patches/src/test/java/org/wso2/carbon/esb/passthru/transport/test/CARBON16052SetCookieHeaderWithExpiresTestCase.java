package org.wso2.carbon.esb.passthru.transport.test;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;

/**
 * A response with 'Set-Cookie' header having 'Expires' attribute fails, if the value of Expires contains a comma.
 */
public class CARBON16052SetCookieHeaderWithExpiresTestCase extends ESBIntegrationTest {

    @BeforeClass
    public void init() throws Exception {
        super.init();
        loadESBConfigurationFromClasspath("artifacts" + File.separator + "ESB" + File.separator + "passthru"
                + File.separator + "transport" + File.separator + "CARBON16052" + File.separator
                + "APIExposedBy3rdParty.xml");
        loadESBConfigurationFromClasspath("artifacts" + File.separator + "ESB" + File.separator + "passthru"
                + File.separator + "transport" + File.separator + "CARBON16052" + File.separator
                + "APIExposedInESB.xml");
    }

    @Test(groups = "wso2.esb", description = "Test SetCookieHeader With Expires having a comma", enabled = true)
    public void testSetCookieHeaderWithExpires() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(getApiInvocationURL("apiExposedInESB"));
        HttpResponse httpResponse = httpclient.execute(httpGet);
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 200, "Request failed with status code : "
                + httpResponse.getStatusLine().getStatusCode());
    }

    @AfterClass
    public void cleanUp() throws Exception {
        super.cleanup();
    }

    protected void loadESBConfigurationFromClasspath(String relativeFilePath) throws Exception {
        relativeFilePath = relativeFilePath.replaceAll("[\\\\/]", Matcher.quoteReplacement(File.separator));

        OMElement synapseConfig = esbUtils.loadResource(relativeFilePath);

        if (relativeFilePath.contains("APIExposedInESB.xml")) {
            AXIOMXPath xpath = new AXIOMXPath("//xmlns:http");
            xpath.addNamespace("xmlns", "http://ws.apache.org/ns/synapse");
            OMElement elementHttp = (OMElement) xpath.selectSingleNode(synapseConfig);

            Iterator iterator = elementHttp.getAllAttributes();
            while (iterator.hasNext()) {
                OMAttribute omAttribute = (OMAttribute) iterator.next();
                if (omAttribute != null && omAttribute.getLocalName().equals("uri-template")) {
                    String port = getApiInvocationURL("apiExposedInESB").split(":")[2].split("/")[0];
                    omAttribute.setAttributeValue(omAttribute.getAttributeValue().replaceAll("8280", port));
                }
            }

            System.out.println(elementHttp);
        }

        updateESBConfiguration(synapseConfig);
    }
}
