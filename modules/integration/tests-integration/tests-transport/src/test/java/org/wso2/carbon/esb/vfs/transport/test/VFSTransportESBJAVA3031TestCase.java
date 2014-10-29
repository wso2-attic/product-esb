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

package org.wso2.carbon.esb.vfs.transport.test;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.FileUtils;
import org.jruby.gen.org$jruby$RubySystemCallError$Populator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.core.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.core.annotations.SetEnvironment;
import org.wso2.carbon.automation.core.utils.frameworkutils.productsetters.AsSetter;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.esb.ESBIntegrationTest;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import static org.testng.Assert.assertTrue;

public class VFSTransportESBJAVA3031TestCase extends ESBIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;
    private String pathToVfsDir;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        pathToVfsDir = getClass().getResource(
                File.separator + "artifacts" + File.separator + "ESB" + File.separator
                        + "synapseconfig" + File.separator + "vfsTransport" + File.separator)
                .getPath();

        serverConfigurationManager = new ServerConfigurationManager(esbServer.getBackEndUrl());
        serverConfigurationManager.applyConfiguration(new File(pathToVfsDir + File.separator
                + "axis2.xml"));
        super.init();

        File outFolder = new File(pathToVfsDir + "test" + File.separator + "out" + File.separator);
        File inFolder = new File(pathToVfsDir + "test" + File.separator + "in" + File.separator);
        File originalFolder = new File(pathToVfsDir + "test" + File.separator + "original"
                + File.separator);
        File failureFolder = new File(pathToVfsDir + "test" + File.separator + "failure"
                + File.separator);

        assertTrue(outFolder.mkdirs(), "file folder not created");
        assertTrue(inFolder.mkdirs(), "file folder not created");
        assertTrue(originalFolder.mkdirs(), "file folder not created");
        assertTrue(failureFolder.mkdirs(), "file folder not created");
        //Checks the availability of the created folders
        assertTrue(outFolder.exists(), "File folder doesn't exists");
        assertTrue(inFolder.exists(), "File folder doesn't exists");
        assertTrue(originalFolder.exists(), "File folder doesn't exists");
        assertTrue(failureFolder.exists(), "File folder doesn't exists");
    }

    @AfterClass(alwaysRun = true)
    public void restoreServerConfiguration() throws Exception {
        try {
            super.cleanup();
        } finally {
            Thread.sleep(3000);
            serverConfigurationManager.restoreToLastConfiguration();
            serverConfigurationManager = null;
        }
    }

    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.integration_user })
    @Test(groups = { "wso2.esb" }, description = "Sending a file through VFS Transport : "
            + "transport.vfs.FileURI = /home/someuser/somedir"
            + " transport.vfs.ContentType = text/plain, "
            + "transport.vfs.FileNamePattern = - *\\.txt")
    public void testVFSProxy() throws Exception {

        // Adding the proxy
        addVFSProxy();
        File inputFile = getInputFile();
        // Creates target and out files
        File targetFile = new File(pathToVfsDir + "test" + File.separator + "in" + File.separator
                + "test.txt");

        File outfile = new File(pathToVfsDir + "test" + File.separator + "out" + File.separator
                + "test.txt");

        try {
            // Get a file channel for the file
            FileChannel channel = new RandomAccessFile(targetFile, "rw").getChannel();
            // Use the file channel to create a lock on the file.
            FileLock lock = channel.lock();
            try {
                FileUtils.copyFile(inputFile, targetFile);
            } catch (IOException e) {
                log.info("Stream is closed ");
            }
            // Try acquiring the lock without blocking.
            try {
                lock = channel.tryLock();
            } catch (OverlappingFileLockException e) {
                log.info("File is already lock in the thread " + e);
            }
            // Releases the lock
            lock.release();
            // Close the channel
            channel.close();

            // Checks the outfile's availability
            boolean value = checkFileWithTimer(outfile);
            Thread.sleep(60000);
            Assert.assertEquals(value, true);

        } catch (InterruptedIOException e) {
            log.info("Interruption happened  " + e);
            throw (e);
        } catch (Exception e) {
            log.error("Exception " + e);
            throw (e);
        } finally {
            deleteFile(targetFile);
            deleteFile(outfile);
            removeProxy("VFSProxy");
        }
    }

    /**
     * Check the output file with thee timer
     *
     * @param outFile
     * @return true
     * @throws InterruptedException
     */
    private boolean checkFileWithTimer(File outFile) throws InterruptedException {
        Long currentTime = System.currentTimeMillis();
        boolean check = false;
        try {
            while ((currentTime + 120000) >= System.currentTimeMillis()) {
                if (outFile.exists()) {
                    if (getFileSize(outFile) >= 0.912696136161685) {
                        check = true;
                        break;
                    }
                    Thread.sleep(2000);
                }
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            throw (e);
        }
        return check;
    }

    /**
     * Adds the VFS proxy
     *
     * @throws Exception
     */
    private void addVFSProxy() throws Exception {

        addProxyService(AXIOMUtil
                .stringToOM("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<proxy xmlns=\"http://ws.apache.org/ns/synapse\" name=\"VFSProxy\" transports=\"vfs\">\n"
                        + "                <parameter name=\"transport.vfs.FileURI\">"
                        + pathToVfsDir
                        + "test"
                        + File.separator
                        + "in"
                        + File.separator
                        + "</parameter> <!--CHANGE-->\n"
                        + "	           <parameter name=\"transport.vfs.ActionAfterProcess\">MOVE</parameter>\n"
                        + "		    <parameter name=\"transport.vfs.MoveAfterProcess\">"
                        + pathToVfsDir
                        + "test"
                        + File.separator
                        + "out"
                        + File.separator
                        + "</parameter> <!--CHANGE-->\n"
                        + "		    <parameter name=\"transport.vfs.MoveAfterFailure\">"
                        + pathToVfsDir
                        + "test"
                        + File.separator
                        + "failed"
                        + File.separator
                        + "</parameter> <!--CHANGE-->\n"
                        + "                <parameter name=\"transport.vfs.ContentType\">text/plain</parameter>\n"
                        + "                <parameter name=\"transport.vfs.FileNamePattern\">.*.txt</parameter>"
                        + "                <parameter name=\"transport.PollInterval\">10</parameter>\n"
                        + "                <target>\n"
                        + "                        <inSequence>\n"
                        + "                           <property name=\"OUT_ONLY\" value=\"true\"/>\n"
                        + "                           <send>\n"
                        + "                               <endpoint name=\"FileEpr\">\n"
                        + "                                   <address uri=\"vfs:file://"
                        + pathToVfsDir
                        + "test"
                        + File.separator
                        + "out\"/>\n"
                        + "                               </endpoint>\n"
                        + "                           </send>"
                        + "                        </inSequence>"
                        + "                </target>\n"
                        + "        </proxy>"));
    }

    /**
     * Generates the large file at run time
     *
     * @return the file
     */
    private File getInputFile() {

        Writer writer = null;
        int iterations = 70000000;
        String constRecord1 = "name";
        String constRecord2 = "id";
        String constRecord3 = "value";
        String record = null;
        File file = new File(pathToVfsDir + "test" + File.separator + "testInput.txt");

        try {
            record = constRecord1 + "," + constRecord2 + "," + constRecord3;
            writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i <= iterations; i++) {
                writer.write(record + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Gets the size of the file in bytes
     *
     * @param file
     * @return size of the file
     */
    private double getFileSize(File file) {
        double size = (((file.length() / 1024.0) / 1024.0) / 1024.0);
        return size;

    }

    /**
     * Deletes the proxy service
     *
     * @param proxyName
     * @throws Exception
     */
    private void removeProxy(String proxyName) throws Exception {
        deleteProxyService(proxyName);
    }

    /**
     * Deletes the file
     *
     * @param file
     * @return
     * @throws IOException
     */
    private boolean deleteFile(File file) throws IOException {
        return file.exists() && file.delete();
    }
}
