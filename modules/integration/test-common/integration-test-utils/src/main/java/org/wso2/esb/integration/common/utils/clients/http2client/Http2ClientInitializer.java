package org.wso2.esb.integration.common.utils.clients.http2client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.*;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;

import static io.netty.handler.logging.LogLevel.INFO;

public class Http2ClientInitializer extends ChannelInitializer<SocketChannel> {
    private static final Http2FrameLogger logger = new Http2FrameLogger(INFO, Http2ClientInitializer.class);

    private final SslContext sslCtx;
    private final int maxContentLength;
    private HttpToHttp2ConnectionHandler connectionHandler;
    private HttpResponseHandler responseHandler;
    private Http2SettingsHandler settingsHandler;

    public Http2ClientInitializer(SslContext sslCtx, int maxContentLength) {
        this.sslCtx = sslCtx;
        this.maxContentLength = maxContentLength;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        final Http2Connection connection = new DefaultHttp2Connection(false);
        connectionHandler = new HttpToHttp2ConnectionHandlerBuilder()
                .frameListener(new DelegatingDecompressorFrameListener(
                        connection,
                        new InboundHttp2ToHttpAdapterBuilder(connection)
                                .maxContentLength(maxContentLength)
                                .propagateSettings(true)
                                .build()))
                .frameLogger(logger)
                .connection(connection)
                .build();
        responseHandler = new HttpResponseHandler();
        settingsHandler = new Http2SettingsHandler(ch.newPromise());
        if (sslCtx != null) {
            configureSsl(ch);
        } else {
            configureClearText(ch);
        }
    }

    public HttpResponseHandler responseHandler() {
        return responseHandler;
    }

    public Http2SettingsHandler settingsHandler() {
        return settingsHandler;
    }

    protected void configureEndOfPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(settingsHandler, responseHandler);
    }

    private void configureSsl(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
            @Override
            protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                    ChannelPipeline p = ctx.pipeline();
                    p.addLast(connectionHandler);
                    configureEndOfPipeline(p);
                    return;
                }
                ctx.close();
                throw new IllegalStateException("unknown protocol: " + protocol);
            }
        });
    }

    private void configureClearText(SocketChannel ch) {
        HttpClientCodec sourceCodec = new HttpClientCodec();
        Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(connectionHandler);
        HttpClientUpgradeHandler upgradeHandler = new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, 65536);

        ch.pipeline().addLast(sourceCodec,
                upgradeHandler,
                new UpgradeRequestHandler(),
                new UserEventLogger());
    }

    private final class UpgradeRequestHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            DefaultFullHttpRequest upgradeRequest =
                    new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            ctx.writeAndFlush(upgradeRequest);
            ctx.fireChannelActive();
            ctx.pipeline().remove(this);
            configureEndOfPipeline(ctx.pipeline());
        }
    }

    private static class UserEventLogger extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            ctx.fireUserEventTriggered(evt);
        }
    }
}
