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

package org.wso2.esb.integration.common.utils.servers.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;


public class Http2Handler extends ChannelDuplexHandler {

    private static final Log log = LogFactory.getLog(Http2Handler.class);
    public static final ByteBuf DATA_RESPONSE = unreleasableBuffer(copiedBuffer("<?xml version='1.0' encoding='UTF-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><ns:getQuoteResponse xmlns:ns=\"http://services.samples\"><ns:return xmlns:ax21=\"http://services.samples/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ax21:GetQuoteResponse\"><ax21:change>4.355291912708564</ax21:change><ax21:earnings>12.138178985496905</ax21:earnings><ax21:high>170.70158142117154</ax21:high><ax21:last>163.97644300968693</ax21:last><ax21:lastTradeTimestamp>Tue Sep 20 11:49:14 IST 2016" +
                    "</ax21:lastTradeTimestamp><ax21:low>-160.7773112104583" +
                    "</ax21:low><ax21:marketCap>6355651.160707118</ax21:marketCap>" +
                    "<ax21:name>WSO2 Company</ax21:name><ax21:open>-162.55332747332653" +
                    "</ax21:open><ax21:peRatio>-19.76675449564413</ax21:peRatio>" +
                    "<ax21:percentageChange>2.338399285949457</ax21:percentageChange>" +
                    "<ax21:prevClose>186.25099395461845</ax21:prevClose>" +
                    "<ax21:symbol>WSO2</ax21:symbol><ax21:volume>9821" +
                    "</ax21:volume></ns:return></ns:getQuoteResponse>" +
                    "</soapenv:Body></soapenv:Envelope>[\r][\n]"
            , CharsetUtil.UTF_8));

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Http2Handler exception caught");
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


    public void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) throws Exception {
        if (data.isEndStream()) {
            ByteBuf content = ctx.alloc().buffer();
            content.writeBytes(DATA_RESPONSE.duplicate());
            Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
            headers.add(HttpHeaderNames.CONTENT_TYPE, "text/xml");
            ctx.write(new DefaultHttp2HeadersFrame(headers));
            ctx.writeAndFlush(new DefaultHttp2DataFrame(content, true));
        }
    }


    public void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers)
            throws Exception {
        if (headers.isEndStream() && (headers.headers().method().toString()).equalsIgnoreCase("GET")) {
            if (headers.headers().contains("http2-settings")) {
                return;
            }
            Http2Headers headers1 = new DefaultHttp2Headers().status(OK.codeAsText());
            ctx.writeAndFlush(new DefaultHttp2HeadersFrame(headers1));
        }
    }
}
