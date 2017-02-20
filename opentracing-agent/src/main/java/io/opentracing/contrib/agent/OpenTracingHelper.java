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
package io.opentracing.contrib.agent;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.global.GlobalTracer;
import io.opentracing.contrib.spanmanager.DefaultSpanManager;
import io.opentracing.contrib.spanmanager.SpanManager;
import io.opentracing.contrib.spanmanager.SpanManager.ManagedSpan;

/**
 * This class provides helper capabilities to the byteman rules.
 */
public class OpenTracingHelper extends Helper {

    private Tracer tracer = GlobalTracer.get();
    private SpanManager spanManager = DefaultSpanManager.getInstance();

    private static Map<Object,Span> spanCache = Collections.synchronizedMap(new WeakHashMap<Object,Span>());
    private static Map<Object,Span> finished = Collections.synchronizedMap(new WeakHashMap<Object,Span>());

    public OpenTracingHelper(Rule rule) {
        super(rule);
    }

    /**
     * This method returns the OpenTracing tracer.
     *
     * @return The tracer
     */
    public Tracer getTracer() {
        return tracer;
    }

    public void manageSpan(Span span) {
        spanManager.manage(span);
    }

    public Span currentSpan() {
        return spanManager.current().getSpan();
    }

    public Span releaseSpan() {
        ManagedSpan current = spanManager.current();
        current.release();
        return current.getSpan();
    }

    public void cacheSpan(Object key, Span span) {
        spanCache.put(key, span);
    }

    public Span retrieveSpan(Object key) {
        return spanCache.get(key);
    }

    /**********************************************/
    /** Needs to be replaced by span.isFinished() */
    public void finishedSpan(Object key, Span span) {
        finished.put(key, span);
    }

    public boolean isFinished(Object key) {
        return finished.containsKey(key);
    }
    /**********************************************/

}
