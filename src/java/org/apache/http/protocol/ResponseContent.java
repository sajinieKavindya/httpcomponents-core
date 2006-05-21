/*
 * $HeadURL$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 *  Copyright 1999-2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.protocol;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;

/**
 * A response interceptor that sets up entity-related headers.
 * For use on the server side.
 *
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 *
 * @version $Revision$
 * 
 * @since 4.0
 */
public class ResponseContent implements HttpResponseInterceptor {

    public ResponseContent() {
        super();
    }
    
    public void process(final HttpResponse response, final HttpContext context) 
            throws HttpException, IOException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (response.containsHeader(HTTP.TRANSFER_ENCODING)) {
            throw new ProtocolException("Transfer-encoding header already present");
        }
        if (response.containsHeader(HTTP.CONTENT_LEN)) {
            throw new ProtocolException("Content-Length header already present");
        }
        HttpVersion ver = response.getStatusLine().getHttpVersion();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            long len = entity.getContentLength();
            if (entity.isChunked() && ver.greaterEquals(HttpVersion.HTTP_1_1)) {
                response.addHeader(new Header(HTTP.TRANSFER_ENCODING, 
                        HTTP.CHUNK_CODING));
            } else if (len >= 0) {
                response.addHeader(new Header(HTTP.CONTENT_LEN, 
                        Long.toString(entity.getContentLength())));
            }
            // Specify a content type if known
            if (entity.getContentType() != null) {
                response.setHeader(entity.getContentType()); 
            }
            // Specify a content encoding if known
            if (entity.getContentEncoding() != null) {
                response.setHeader(entity.getContentEncoding()); 
            }
        } else {
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_NO_CONTENT 
                    && status != HttpStatus.SC_NOT_MODIFIED
                    && status != HttpStatus.SC_RESET_CONTENT) {
                response.addHeader(new Header(HTTP.CONTENT_LEN, "0"));
            }
        }
    }
    
}
