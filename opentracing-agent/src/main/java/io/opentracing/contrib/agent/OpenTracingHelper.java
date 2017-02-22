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

    private static Map<Object,Span> spanAssociations = Collections.synchronizedMap(new WeakHashMap<Object,Span>());
    private static Map<Object,Span> finished = Collections.synchronizedMap(new WeakHashMap<Object,Span>());

    private static Map<Object,Integer> state = Collections.synchronizedMap(new WeakHashMap<Object,Integer>());

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

    /**
     * This method requests that the supplied span becomes the
     * current 'active' span.
     *
     * @param span The span
     */
    public void manageSpan(Span span) {
        spanManager.manage(span);
    }

    /**
     * This method returns the currently active span.
     *
     * @return The current span, or null if no active span exists
     */
    public Span currentSpan() {
        return spanManager.current().getSpan();
    }

    /**
     * This method requests that the current active span
     * should be released (unmanaged). If the underlying
     * span management mechanism maintains a stack, then
     * this will result in the previous (unfinished) parent
     * span being reinstated as the current active span.
     *
     * @return The span being released, or null if no active span found
     */
    public Span releaseCurrentSpan() {
        ManagedSpan current = spanManager.current();
        current.release();
        return current.getSpan();
    }

    /**
     * This method establishes an association between an application object
     * (i.e. the subject of the instrumentation) and a span. Once the
     * application object is no longer being used, the association with the
     * span will automatically be discarded.
     *
     * @param obj The application object to be associated with the span
     * @param span The span
     */
    public void associateSpan(Object obj, Span span) {
        spanAssociations.put(obj, span);
    }

    /**
     * This method retrieves the span associated with the supplied application
     * object.
     *
     * @param obj The application object
     * @return The span, or null if no associated span exists
     */
    public Span retrieveSpan(Object obj) {
        return spanAssociations.get(obj);
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

    /**
     * This method enables an instrumentation rule to record a 'state'
     * number against an application object. This can be used in
     * subsequent rules to determine what the valid actions that
     * can be performed.
     *
     * @param obj The application object
     * @param value The state value
     */
    public void setState(Object obj, int value) {
        state.put(obj, new Integer(value));
    }

    /**
     * This method retrieves the current 'state' number associated with
     * the supplied application object.
     *
     * @param obj The application object
     * @return The state, or 0 if no state currently exists
     */
    public int getState(Object obj) {
        Integer value = state.get(obj);
        return value == null ? 0 : value.intValue();
    }
}
