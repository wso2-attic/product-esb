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

import org.apache.http.*;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class TestRequestHandler implements HttpRequestHandler {

    private int statusCode = HttpStatus.SC_OK;
    private String contentType = "application/xml";
    private String payload = "<test/>";
    private boolean emptyBody = false;

    private RequestInterceptor interceptor = null;

    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext context) throws HttpException, IOException {

        if (interceptor != null) {
            interceptor.requestReceived(request);
        }

        response.setStatusCode(statusCode);
        if (!emptyBody) {
            writeContent(request, response);
        }
    }

    private void writeContent(HttpRequest request, HttpResponse response) {
        // Check for edge cases as stated in the HTTP specs
        if ("HEAD".equals(request.getRequestLine().getMethod()) ||
                statusCode == HttpStatus.SC_NO_CONTENT ||
                statusCode == HttpStatus.SC_RESET_CONTENT ||
                statusCode == HttpStatus.SC_NOT_MODIFIED) {
            return;
        }

        EntityTemplate body = createEntity();
        body.setContentType(contentType);
        response.setEntity(body);
    }

    private EntityTemplate createEntity() {
        return new EntityTemplate(new ContentProducer() {
            public void writeTo(OutputStream outputStream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                writer.write(payload);
                writer.flush();
            }
        });
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setInterceptor(RequestInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void setEmptyBody(boolean emptyBody) {
        this.emptyBody = emptyBody;
    }
}
