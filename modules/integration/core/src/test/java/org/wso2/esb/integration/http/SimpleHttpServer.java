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

package org.wso2.esb.integration.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.*;
import org.wso2.esb.integration.BackendServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimpleHttpServer implements BackendServer {

    private static final Log log = LogFactory.getLog(SimpleHttpServer.class);

    private int port;
    private Properties properties;

    private ServerSocket serverSocket;
    private ExecutorService listener;
    private ExecutorService workerPool;
    private HttpParams params;
    private HttpService httpService;
    private boolean shutdown = true;

    private TestRequestHandler requestHandler;

    public SimpleHttpServer() {
        this(8080, new Properties());
    }

    public SimpleHttpServer(int port, Properties properties) {
        this.port = port;
        this.properties = properties;
        this.requestHandler = new TestRequestHandler();
    }

    public TestRequestHandler getRequestHandler() {
        return requestHandler;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);

        params = new BasicHttpParams();
        params
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
                        getParameter(CoreConnectionPNames.SO_TIMEOUT, 60000))
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
                        getParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024))
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK,
                        getParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, 0) == 1)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY,
                        getParameter(CoreConnectionPNames.TCP_NODELAY, 1) == 1)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "WSO2ESB-Test-Server");

        // Configure HTTP protocol processor
        BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
        httpProcessor.addInterceptor(new ResponseDate());
        httpProcessor.addInterceptor(new ResponseServer());
        httpProcessor.addInterceptor(new ResponseContent());
        httpProcessor.addInterceptor(new ResponseConnControl());

        HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
        registry.register("*", requestHandler);

        // Set up the HTTP service
        httpService = new HttpService(
                httpProcessor,
                new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory(), registry, params);

        listener = Executors.newSingleThreadExecutor();
        workerPool = Executors.newFixedThreadPool(getParameter("ThreadCount", 2));

        shutdown = false;
        listener.submit(new HttpListener());
    }

    public void stop() throws IOException {
        log.info("Shutting down simple HTTP server");
        shutdown = true;
        listener.shutdownNow();
        workerPool.shutdownNow();
        serverSocket.close();

        try {
            listener.awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {

        }

        try {
            workerPool.awaitTermination(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {

        }
    }

    public boolean isStarted() {
        return !shutdown;
    }

    public void deployService(Object service) throws IOException {
        requestHandler.setInterceptor((RequestInterceptor) service);
    }

    private int getParameter(String name, int def) {
        String val = properties.getProperty(name);
        if (val != null && Integer.valueOf(val) > 0) {
            return Integer.valueOf(val);
        }
        return def;
    }

    private class HttpListener implements Runnable {

        public void run() {
            log.info("Starting HTTP server on port: " + port);
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = serverSocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    conn.bind(socket, params);

                    // Start worker thread
                    workerPool.submit(new ServerWorker(conn));
                } catch (IOException e) {
                    if (!shutdown) {
                        log.error("I/O error while accepting a connection", e);
                    }
                    break;
                }
            }
        }
    }

    private class ServerWorker implements Runnable {

        private final HttpServerConnection conn;

        public ServerWorker(HttpServerConnection conn) {
            this.conn = conn;
        }

        public void run() {
            HttpContext context = new BasicHttpContext(null);
            context.setAttribute("CURRENT_CONN", conn);

            try {
                while (!Thread.interrupted() && conn.isOpen()) {
                    httpService.handleRequest(conn, context);
                }
            } catch (ConnectionClosedException ex) {
                log.error("Client closed connection", ex);
            } catch (IOException ex) {
                log.error("I/O error", ex);
            } catch (HttpException ex) {
                log.error("Unrecoverable HTTP protocol violation", ex);
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
        }
    }
}
