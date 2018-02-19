/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017-2018 microBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.kubernetes.client.cdi;

import java.io.Closeable;
import java.io.IOException;

import java.util.concurrent.ExecutorService;

import javax.enterprise.context.ApplicationScoped;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import io.fabric8.kubernetes.client.utils.HttpClientUtils;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * A class housing various <a
 * href="http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#producer_method">producer
 * method</a>s.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@ApplicationScoped
class Producers {

  private Producers() {
    super();
  }

  @Produces
  @ApplicationScoped
  private static final OkHttpClient produceOkHttpClient(final Config config) {
    return HttpClientUtils.createHttpClient(config);
  }

  private static final void destroyOkHttpClient(@Disposes final OkHttpClient client) throws IOException {
    if (client != null) {
      // See https://square.github.io/okhttp/3.x/okhttp/okhttp3/OkHttpClient.html
      final Dispatcher dispatcher = client.dispatcher();
      if (dispatcher != null) {
        final ExecutorService executorService = dispatcher.executorService();
        if (executorService != null) {
          // Stop accepting new connection requests.
          executorService.shutdown();
        }
        // Cancel any connections in progress.
        dispatcher.cancelAll();
      }
      final ConnectionPool connectionPool = client.connectionPool();
      if (connectionPool != null) {
        // Boot all connections out of the pool.
        connectionPool.evictAll();
      }
      final Closeable cache = client.cache();
      if (cache != null) {
        cache.close();
      }
    }
  }

  @Produces
  private static final Config produceConfig() {
    return new ConfigBuilder().build();
  }
  
  @Produces
  @ApplicationScoped
  private static final DefaultKubernetesClient produceKubernetesClient(final OkHttpClient httpClient, final Config config) {
    return new DefaultKubernetesClient(httpClient, config);
  }

  private static final void disposeKubernetesClient(@Disposes final DefaultKubernetesClient client) {
    // We deliberately do NOT call close() on the supplied client,
    // because it is possible to construct a DefaultKubernetesClient
    // with an OkHttpClient passed in from the outside (as is done in
    // this class).  Consequently it is bad form for a
    // DefaultKubernetesClient to close() it!
  }
  
}
