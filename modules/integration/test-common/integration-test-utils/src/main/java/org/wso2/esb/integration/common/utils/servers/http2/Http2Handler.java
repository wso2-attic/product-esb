package org.wso2.esb.integration.common.utils.servers.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * A simple handler that responds with the message "Hello World!".
 *
 * <p>This example is making use of the "multiplexing" http2 API, where streams are mapped to child
 * Channels. This API is very experimental and incomplete.
 */
@Sharable
public class Http2Handler extends ChannelDuplexHandler {

    static final ByteBuf RESPONSE_BYTES = unreleasableBuffer(copiedBuffer("Hello World", CharsetUtil.UTF_8));
    static final ByteBuf DATA_RESPONSE=unreleasableBuffer(copiedBuffer("<?xml version='1.0' encoding='UTF-8'?>" +
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<soapenv:Body>" +
                    "<ns:getQuoteResponse xmlns:ns=\"http://services.samples\">" +
                    "<ns:return xmlns:ax21=\"http://services.samples/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ax21:GetQuoteResponse\">" +
                    "<ax21:change>4.355291912708564</ax21:change>" +
                    "<ax21:earnings>12.138178985496905</ax21:earnings>" +
                    "<ax21:high>170.70158142117154</ax21:high>" +
                    "<ax21:last>163.97644300968693</ax21:last>" +
                    "<ax21:lastTradeTimestamp>Tue Sep 20 11:49:14 IST 2016 </ax21:lastTradeTimestamp>" +
                    "<ax21:low>-160.7773112104583</ax21:low>" +
                    "<ax21:marketCap>6355651.160707118</ax21:marketCap>" +
                    "<ax21:name>WSO2 Company</ax21:name>" +
                    "<ax21:open>-162.55332747332653</ax21:open>" +
                    "<ax21:peRatio>-19.76675449564413</ax21:peRatio>" +
                    "<ax21:percentageChange>2.338399285949457</ax21:percentageChange>" +
                    "<ax21:prevClose>186.25099395461845</ax21:prevClose>" +
                    "<ax21:symbol>WSO2</ax21:symbol><ax21:volume>9821" +
                    "</ax21:volume></ns:return></ns:getQuoteResponse>" +
                    "</soapenv:Body>" +
                    "</soapenv:Envelope>\n"
            ,CharsetUtil.UTF_8));

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Http2Handler exception caught");
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead(ctx, (Http2DataFrame) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    /**
     * If receive a frame with end-of-stream set, send a pre-canned response.
     */
    public void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) throws Exception {
        System.out.println("On Data triggered");
        if (data.isEndStream()) {
            ByteBuf content = ctx.alloc().buffer();
            ByteBuf con = data.content();
            if (con.isReadable()) {
                int contentLength = con.readableBytes();
                byte[] arr = new byte[contentLength];
                con.readBytes(arr);
                System.out.println("Received data: "+new String(arr, 0, contentLength, CharsetUtil.UTF_8));
            }
            content.writeBytes(DATA_RESPONSE.duplicate());
            Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
            headers.add(HttpHeaderNames.CONTENT_TYPE,"text/xml");
            ctx.write(new DefaultHttp2HeadersFrame(headers));
            ctx.writeAndFlush(new DefaultHttp2DataFrame(content, true));
        }
    }

    /**
     * If receive a frame with end-of-stream set, send a pre-canned response.
     */
    public void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers)
            throws Exception {
        //System.out.println();
        if (headers.isEndStream() && (headers.headers().method().toString()).equalsIgnoreCase("GET")) {
            if (headers.headers().contains("http2-settings")) {
                //log.info("Settings Frame Headers " + headers.headers().toString());
                return;
            }
            //ByteBuf content = ctx.alloc().buffer();
            //content.writeBytes(RESPONSE_BYTES.duplicate());
           // ByteBufUtil.writeAscii(content, " - via HTTP/2");
            ByteBuf content = ctx.alloc().buffer();
         //   ByteBuf con = data.content();
           /* if (con.isReadable()) {
                int contentLength = con.readableBytes();
                byte[] arr = new byte[contentLength];
                con.readBytes(arr);
                System.out.println("Received data: "+new String(arr, 0, contentLength, CharsetUtil.UTF_8));
            }*/
            content.writeBytes(DATA_RESPONSE.duplicate());
            Http2Headers head = new DefaultHttp2Headers().status(OK.codeAsText());
            head.add(HttpHeaderNames.CONTENT_TYPE,"text/xml");
            ctx.write(new DefaultHttp2HeadersFrame(head));
            ctx.writeAndFlush(new DefaultHttp2DataFrame(content, true));
        }
    }

    /**
     * Sends a "Hello World" DATA frame to the client.
     */
    private void sendResponse(ChannelHandlerContext ctx, ByteBuf payload) {
        System.out.println("sendRespond triggered");
        // Send a frame for the response status
        Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
        ctx.write(new DefaultHttp2HeadersFrame(headers));
        ctx.writeAndFlush(new DefaultHttp2DataFrame(payload, true));
    }
}
