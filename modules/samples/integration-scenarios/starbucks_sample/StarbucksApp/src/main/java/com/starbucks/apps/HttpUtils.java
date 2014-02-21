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
import java.io.OutputStream;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class HttpUtils {
    
    public static HttpInvocationContext doPost(final String payload, String contentType, 
            String url) throws IOException {
        
        HttpUriRequest request = new HttpPost(url);        
        return invoke(request, payload, contentType);
    }
    
    public static HttpInvocationContext doPut(final String payload, String contentType, 
            String url) throws IOException {
        
        HttpUriRequest request = new HttpPut(url);                
        return invoke(request, payload, contentType);        
    }
    
    public static HttpInvocationContext doGet(String url) throws IOException {
        HttpUriRequest request = new HttpGet(url);
        return invoke(request, null, null);
    }
    
    public static HttpInvocationContext doDelete(String url) throws IOException {
        HttpUriRequest request = new HttpDelete(url);
        return invoke(request, null, null);
    }
    
    public static HttpInvocationContext doOptions(String url) throws IOException {
        HttpUriRequest request = new HttpOptions(url);
        return invoke(request, null, null);
    }
    
    private static HttpInvocationContext invoke(HttpUriRequest request, 
            final String payload, final String contentType) throws IOException {
        
        if (payload != null) {
           HttpEntityEnclosingRequest entityEncReq = (HttpEntityEnclosingRequest) request;
           EntityTemplate ent = new EntityTemplate(new ContentProducer() {
                public void writeTo(OutputStream outputStream) throws IOException {
                    outputStream.write(payload.getBytes());
                    outputStream.flush();
                }
            }); 
            ent.setContentType(contentType);
            entityEncReq.setEntity(ent);
        }
        
        HttpInvocationContext context = new HttpInvocationContext(payload);
        DefaultHttpClient client = getHttpClient(context);        
        HttpResponse response = client.execute(request);
        context.setHttpResponse(response);
        return context;
    }
    
    private static DefaultHttpClient getHttpClient(HttpInvocationContext context) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 30000);
        client.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {                     
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                return false;
            }
        });  
        client.addRequestInterceptor(context);
        client.addResponseInterceptor(context);
        return client;
    }
    
}
