/*
 *
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.esb.integration.common.utils.clients;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.esb.integration.common.utils.clients.http2client.Http2ClientInitializer;
import org.wso2.esb.integration.common.utils.clients.http2client.Http2Response;
import org.wso2.esb.integration.common.utils.clients.http2client.Http2SettingsHandler;
import org.wso2.esb.integration.common.utils.clients.http2client.HttpResponseHandler;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Http2Client {

    private final Log log = LogFactory.getLog(Http2Client.class);

    private boolean SSL = false;
    private int PORT;
    private String HOST;
    private SslContext sslContext = null;
    private int StreamId;
    private Channel channel;
    private HttpResponseHandler responseHandler;
    private Http2ClientInitializer initializer;
    private EventLoopGroup workerGroup;

    public Http2Client(String HOST, int PORT) {
        this.HOST = HOST;
        this.PORT = PORT;
        this.StreamId = 3;
    }

    public void createSSLContext(TrustManagerFactory trustManager) {
        try {
            sslContext = generateSSLContext(trustManager);
            SSL = true;
        } catch (SSLException e) {
            log.error(e.getStackTrace());
        }
    }

    public Http2Response doGet(String url, Map<String, String> headers) {
        initChannel();
        HttpScheme scheme = SSL ? HttpScheme.HTTPS : HttpScheme.HTTP;
        AsciiString hostName = new AsciiString(HOST + ':' + PORT);

        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, url);
        if (!headers.isEmpty()) {
            for (Map.Entry h : headers.entrySet()) {
                request.headers().add((CharSequence) h.getKey(), h.getValue());
            }
        }
        request.headers().add(HttpHeaderNames.HOST, hostName);
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
        io.netty.channel.ChannelPromise p;
        int s = StreamId;
        if (responseHandler == null) {
            log.error("Response handler is null");
            return null;
        } else if (channel == null) {
            log.error("Channel is null");
            return null;
        } else {
            responseHandler.put(StreamId, channel.writeAndFlush(request), p = channel.newPromise());
            StreamId += 2;
            Http2Response response;
            try {
                while (!p.isSuccess()) {
                    log.info("Waiting for response");
                    Thread.sleep(20);
                }
                response = responseHandler.getResponse(s);
            } catch (InterruptedException e) {
                response = null;
                log.error(e.getStackTrace());
            }
            return response;
        }

    }

    public Http2Response doPost(String url, String data, Map<String, String> headers) {

        initChannel();
        HttpScheme scheme = SSL ? HttpScheme.HTTPS : HttpScheme.HTTP;
        AsciiString hostName = new AsciiString(HOST + ':' + PORT);

        FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, url,
                Unpooled.copiedBuffer(data.getBytes()));

        if (!headers.isEmpty()) {
            for (Map.Entry h : headers.entrySet()) {
                request.headers().add((CharSequence) h.getKey(), h.getValue());
            }
        }
        request.headers().add(HttpHeaderNames.HOST, hostName);
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
        io.netty.channel.ChannelPromise p;
        int s = StreamId;
        responseHandler.put(StreamId, channel.writeAndFlush(request), p = channel.newPromise());
        StreamId += 2;
        Http2Response response;
        try {
            while (!p.isSuccess()) {
                log.info("Waiting for response");
                Thread.sleep(20);
            }
            response = responseHandler.getResponse(s);
        } catch (InterruptedException e) {
            response = null;
            log.error(e.getStackTrace());
        }
        return response;
    }

    public void releaseConnection() {
        if (channel != null) {
            channel.close().syncUninterruptibly();
        }
    }

    private SslContext generateSSLContext(TrustManagerFactory trustManager) throws SSLException {
        SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
        return SslContextBuilder.forClient()
                .sslProvider(provider)
                .trustManager(trustManager)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        Protocol.ALPN,
                        SelectorFailureBehavior.NO_ADVERTISE,
                        SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1))
                .build();

    }

    private void initChannel() {
        if (channel == null) {
            try {
                workerGroup = new NioEventLoopGroup();
                initializer = new Http2ClientInitializer(sslContext, Integer.MAX_VALUE);
                Bootstrap b = new Bootstrap();
                b.group(workerGroup);
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.remoteAddress(HOST, PORT);
                b.handler(initializer);

                // Start the client.
                channel = b.connect().syncUninterruptibly().channel();
                log.info("Connected to [" + HOST + ':' + PORT + ']');

                // Wait for the HTTP/2 upgrade to occur.
                Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
                http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);

                responseHandler = initializer.responseHandler();

            } catch (Exception e) {
                log.error(e.getStackTrace());
            }
        }
    }
}
