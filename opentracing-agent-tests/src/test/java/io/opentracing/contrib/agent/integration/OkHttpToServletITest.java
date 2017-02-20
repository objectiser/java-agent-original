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
package io.opentracing.contrib.agent.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author gbrown
 */
public class OkHttpToServletITest extends io.opentracing.contrib.agent.common.HttpTestBase {

    private static final String HELLO_URL = "http://localhost:8180/hello";

    @Test
    public void testRequest() throws IOException {
        sayHello();
        assertEquals(2, getTracer().finishedSpans().size());
        assertEquals("GET", getTracer().finishedSpans().get(0).operationName());
        assertEquals("TestSpan", getTracer().finishedSpans().get(1).operationName());
        assertTrue(getTracer().finishedSpans().get(1).tags().containsKey("status.code"));
        
        // TODO: The spans don't have a parent/child relationship yet - need to use
        // span manager to avoid integration specific mechanisms
    }

    public void sayHello() throws IOException {
        // TODO: Rule does not currently work when just using the OkHttpClient default constructor
        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Request request = new Request.Builder()
              .url(HELLO_URL)
              .build();

        Response response = client.newCall(request).execute();
        
        assertEquals(200, response.code());
    }

}
