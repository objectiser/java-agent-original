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
package io.opentracing.contrib.agent.common;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import io.opentracing.contrib.global.GlobalTracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;

/**
 * @author gbrown
 */
public class HttpTestBase {

    private static Server server = null;
    private static MockTracer tracer = new MockTracer(Propagator.TEXT_MAP);

    public static final String TEST_FAULT_HEADER_FLAG = "test-fault";

    @BeforeClass
    public static void initClass() throws Exception {
        GlobalTracer.register(tracer);

        server = new Server(8180);
        server.setHandler(new HelloHandler());
        server.start();
    }

    @AfterClass
    public static void closeClass() throws Exception {
        server.stop();
    }

    @Before
    public void init() {
        tracer.reset();
    }

    public MockTracer getTracer() {
        return tracer;
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
