/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.systest.https.conduit;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import org.jboss.netty.handler.codec.http.HttpRequest;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.littleshoot.proxy.DefaultHttpProxyServer;
import org.littleshoot.proxy.HttpFilter;
import org.littleshoot.proxy.HttpRequestFilter;


/**
 * 
 */
public class HTTPSProxyConduitTest extends HTTPSConduitTest {
    static final int PROXY_PORT = Integer.parseInt(allocatePort(HTTPSProxyConduitTest.class));
    static DefaultHttpProxyServer proxy;
    static CountingFilter requestFilter = new CountingFilter();
    
    static class CountingFilter implements HttpRequestFilter {
        AtomicInteger count = new AtomicInteger();
        public void filter(HttpRequest httpRequest) {
            count.incrementAndGet();
        }
        
        public void reset() {
            count.set(0);
        }
        public int getCount() {
            return count.get();
        }
    }
    
    public HTTPSProxyConduitTest() {
    }

    
    @AfterClass
    public static void stopProxy() {
        proxy.stop();
        proxy = null;
    }
    
    @BeforeClass
    public static void startProxy() {
        proxy = new DefaultHttpProxyServer(PROXY_PORT, requestFilter, new HashMap<String, HttpFilter>());
        proxy.start();
    }
    @Before
    public void resetCount() {
        requestFilter.reset();
    }
    
    public void configureProxy(Client client) {
        HTTPConduit cond = (HTTPConduit)client.getConduit();
        HTTPClientPolicy pol = cond.getClient();
        if (pol == null) {
            pol = new HTTPClientPolicy();
            cond.setClient(pol);
        }
        pol.setProxyServer("localhost");
        pol.setProxyServerPort(PROXY_PORT);
    }
    
    public void resetProxyCount() {
        requestFilter.reset();
    }
    public void assertProxyRequestCount(int i) {
        assertEquals("Unexpected request count", i, requestFilter.getCount());
    }
    
}
