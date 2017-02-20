/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentracing.contrib.agent.direct;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.opentracing.contrib.agent.common.OTAgentTestBase;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;

public class JavaHttpURLConnectionITest extends OTAgentTestBase {

    private static final String SAY_HELLO_URL = "http://localhost:8180/hello";

    private static Server server = null;

    public static final String TEST_FAULT_HEADER_FLAG = "test-fault";

    @BeforeClass
    public static void initClass() throws Exception {
        server = new Server(8180);
        server.setHandler(new HelloHandler());
        server.start();
    }

    @AfterClass
    public static void closeClass() throws Exception {
        server.stop();
    }

    @Test
    public void testHttpURLConnectionConnectGET() throws IOException {
        testHttpRequestConnect(new URL(SAY_HELLO_URL), "GET", false);
    }

    @Test
    public void testHttpURLConnectionGetStatusCodeGET() throws IOException {
        testHttpRequestGetStatusCode(new URL(SAY_HELLO_URL), "GET", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamGET() throws IOException {
        sendRequestUsingInputStream(new URL(SAY_HELLO_URL), "GET", false);
    }

    @Test
    public void testHttpURLConnectionConnectGETWithFault() throws IOException {
        testHttpRequestConnect(new URL(SAY_HELLO_URL), "GET", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamGETWithFault() throws IOException {
        sendRequestUsingInputStream(new URL(SAY_HELLO_URL), "GET", true);
    }

    @Test
    public void testHttpURLConnectionConnectPUT() throws IOException {
        testHttpRequestConnect(new URL(SAY_HELLO_URL), "PUT", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamPUT() throws IOException {
        sendRequestUsingInputStream(new URL(SAY_HELLO_URL), "PUT", false);
    }

    @Test
    public void testHttpURLConnectionConnectPOST() throws IOException {
        testHttpRequestConnect(new URL(SAY_HELLO_URL), "POST", false);
    }

    @Test
    public void testHttpURLConnectionInputStreamPOST() throws IOException {
        sendRequestUsingInputStream(new URL(SAY_HELLO_URL), "POST", false);
    }

    protected void testHttpRequestConnect(URL url, String method, boolean fault) throws IOException {
        sendRequest(url, method, fault, true);
    }

    protected void testHttpRequestGetStatusCode(URL url, String method, boolean fault) throws IOException {
        sendRequest(url, method, fault, false);
    }

    protected void sendRequest(URL url, String method, boolean fault, boolean connect) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);

        if (fault) {
            connection.addRequestProperty(TEST_FAULT_HEADER_FLAG, "true");
        }

        if (connect) {
            connection.connect();
        }

        int status = connection.getResponseCode();

        if (!fault) {
            assertEquals("Unexpected response code", 200, status);
        } else {
            assertEquals("Unexpected fault response code", 401, status);
        }

        // Call again to make sure does not attempt to finish the span again
        connection.getResponseCode();

        verifyTrace(url, method, fault);
    }

    protected void sendRequestUsingInputStream(URL url, String method, boolean fault) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);

        if (fault) {
            connection.addRequestProperty(TEST_FAULT_HEADER_FLAG, "true");
        }

        try (InputStream is = connection.getInputStream()) {
        } catch (IOException ioe) {
            if (!fault) {
                throw ioe;
            }
        }

        int status = connection.getResponseCode();

        if (!fault) {
            assertEquals("Unexpected response code", 200, status);
        } else {
            assertEquals("Unexpected fault response code", 401, status);
        }

        // Call again to make sure does not attempt to finish the span again
        connection.getResponseCode();

        verifyTrace(url, method, fault);
    }

    protected void verifyTrace(URL url, String method, boolean fault) {
        List<MockSpan> spans = getTracer().finishedSpans();
        assertEquals(1, spans.size());
        assertEquals(method, spans.get(0).operationName());
        assertEquals(url.toString(), spans.get(0).tags().get(Tags.HTTP_URL.getKey()));
        assertEquals(fault ? 401 : 200, spans.get(0).tags().get(Tags.HTTP_STATUS.getKey()));
    }

    public static class HelloHandler extends AbstractHandler {
        
        @Override
        public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("text/html;charset=utf-8");
            if (request.getHeader(TEST_FAULT_HEADER_FLAG) != null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("<h1>Hello World</h1>");                
            }
            baseRequest.setHandled(true);
        }
    }

}
