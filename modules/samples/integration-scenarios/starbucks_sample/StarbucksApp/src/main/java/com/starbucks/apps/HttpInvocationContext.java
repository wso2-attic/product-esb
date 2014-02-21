/*
 *  Copyright 2012 WSO2
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.starbucks.apps;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * Encapsulates all the metadata generated during a HTTP request-response
 * invocation. This includes message payloads, headers and all other
 * information required to validate an invocation at a later time.
 */
public class HttpInvocationContext implements HttpRequestInterceptor, HttpResponseInterceptor {
    
    private StringBuilder requestData = new StringBuilder();
    private StringBuilder responseData = new StringBuilder();
    
    private HttpResponse response;
    
    private String requestPayload;
    private String responsePayload;
    
    public HttpInvocationContext(String requestPayload) {
        this.requestPayload = requestPayload;
    }
    
    public void process(HttpRequest request, 
            HttpContext httpContext) throws HttpException,IOException {
        
        requestData.append(request.getRequestLine()).append("\n");
        HeaderIterator iter = request.headerIterator();
        while (iter.hasNext()) {
            Header h = iter.nextHeader();
            requestData.append(h.getName()).append(": ").append(h.getValue()).append("\n");
        }

        requestData.append("\n");
        if (requestPayload != null) {
            requestData.append(requestPayload);
        }                
    }
    
    public void process(HttpResponse response, 
            HttpContext httpContext) throws HttpException,IOException {
                
        responseData.append(response.getStatusLine()).append("\n");
        for (Header h : response.getAllHeaders()) {
            responseData.append(h.getName()).append(": ").append(h.getValue()).append("\n");
        }
        responseData.append("\n");
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream in = null;
            try {
                in = entity.getContent();
                int length;
                StringBuilder payload = new StringBuilder("");
                byte[] tmp = new byte[2048];
                while ((length = in.read(tmp)) != -1) {
                    payload.append(new String(tmp, 0, length));
                }
                if (payload.length() > 0) {
                    this.responsePayload = payload.toString();
                    responseData.append(XmlUtils.prettyPrint(this.responsePayload));                
                }
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }
    
    public void setHttpResponse(HttpResponse response) {
        this.response = response;
    }
    
    public String getRequestData() {
        return requestData.toString();
    }
    
    public String getResponseData() {
        return responseData.toString();
    }
    
    public HttpResponse getHttpResponse() {
        return response;
    }
    
    public String getResponsePayload() {
        return responsePayload;
    }
    
}
