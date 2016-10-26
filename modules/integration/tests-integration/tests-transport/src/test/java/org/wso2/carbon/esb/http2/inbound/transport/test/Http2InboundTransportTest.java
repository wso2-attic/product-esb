package org.wso2.carbon.esb.http2.inbound.transport.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.clients.Http2Client;
import org.wso2.esb.integration.common.utils.clients.http2client.Http2Response;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;
import org.wso2.esb.integration.common.utils.servers.Http2Server;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.TreeMap;

/**
 * Created by chanakabalasooriya on 10/13/16.
 */
public class Http2InboundTransportTest extends ESBIntegrationTest {
    private Http2Server http2Server;
    private Http2Client http2Client;
    private ServerConfigurationManager serverConfigurationManager;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        serverConfigurationManager = new ServerConfigurationManager(context);
        serverConfigurationManager.applyConfiguration(
                new File(TestConfigurationProvider.getResourceLocation() + File.separator + "artifacts" +
                        File.separator + "ESB" + File.separator + "http2.inbound.transport"+File.separator+ "axis2.xml"));
        super.init();
        http2Server=new Http2Server(false,8083);
        http2Server.startServer();

        http2Client=new Http2Client("localhost",8082);

        addSequence(getArtifactConfig("TestSequence.xml"));
        addApi(getArtifactConfig("TestApi.xml"));
        addInboundEndpoint(getArtifactConfig("TestInbound.xml"));
    }

    @Test(groups = "wso2.esb", description = "Inbound Http2  test case" )
    public void inboundHttp2Test() throws AxisFault {
        Http2Response res= http2Client.doGet("stockquote/view/wso2",new TreeMap<String,String>());
        Assert.assertNotNull(res);

        InputStream in = new AutoCloseInputStream(new ByteArrayInputStream(res.getBytes()));
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        Document response=null;
        try {
            DocumentBuilder builder=factory.newDocumentBuilder();
            response=builder.parse(in);
        }catch (Exception e){}
        Assert.assertNotNull(response);
        Assert.assertEquals("m:CheckPriceResponse",response.getDocumentElement().getTagName());
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        http2Client.releaseConnection();
        http2Server.stopServer();
        super.cleanup();
        if(serverConfigurationManager!=null){
            serverConfigurationManager.restoreToLastConfiguration();
        }
    }

    private OMElement getArtifactConfig(String fileName) throws Exception {
        OMElement synapseConfig = null;
        String path ="artifacts" + File.separator + "ESB" + File.separator
                + "http2.inbound.transport" + File.separator + fileName;
        try {
            synapseConfig = esbUtils.loadResource(path);
        } catch (FileNotFoundException e) {
            throw new Exception("File Location " + path + " may be incorrect", e);
        } catch (XMLStreamException e) {
            throw new XMLStreamException("XML Stream Exception while reading file stream", e);
        }
        return synapseConfig;
    }

}
