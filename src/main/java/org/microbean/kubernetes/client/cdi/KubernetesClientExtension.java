/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 MicroBean.
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

import java.lang.annotation.Annotation;

import java.util.Set;

import java.util.concurrent.CountDownLatch;

import javax.annotation.Priority;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Initialized;

import javax.enterprise.event.Observes;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;

import org.microbean.cdi.AbstractBlockingExtension;

import org.microbean.kubernetes.client.cdi.annotation.Added;
import org.microbean.kubernetes.client.cdi.annotation.Modified;
import org.microbean.kubernetes.client.cdi.annotation.Deleted;
import org.microbean.kubernetes.client.cdi.annotation.Error;

import io.fabric8.kubernetes.api.model.Event;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.Watcher.Action;

import static javax.interceptor.Interceptor.Priority.LIBRARY_AFTER;
import static javax.interceptor.Interceptor.Priority.LIBRARY_BEFORE;

public class KubernetesClientExtension extends AbstractBlockingExtension {

  private boolean startWatcher;
  
  private Closeable watch;

  private volatile KubernetesClientException closeException;
  
  public KubernetesClientExtension() {
    super();
  }

  public KubernetesClientExtension(final CountDownLatch latch) {
    super(latch);
  }

  private final <X> void processObserverMethod(@Observes final ProcessObserverMethod<Event, X> event, final BeanManager beanManager) {
    if (event != null && beanManager != null && !this.startWatcher) {
      final ObserverMethod<Event> observerMethod = event.getObserverMethod();
      if (observerMethod != null) {
        this.startWatcher = true;
      }
    }
  }

  private final void watch(@Observes @Initialized(ApplicationScoped.class) @Priority(LIBRARY_AFTER) final Object event, final KubernetesClient client, final BeanManager beanManager) throws InterruptedException {
    if (client != null && beanManager != null && this.startWatcher) {
      final javax.enterprise.event.Event<Event> eventBroadcaster = beanManager.getEvent().select(Event.class);
      assert eventBroadcaster != null;
      final javax.enterprise.event.Event<Event> addedBroadcaster = eventBroadcaster.select(Added.Literal.INSTANCE);
      assert addedBroadcaster != null;
      final javax.enterprise.event.Event<Event> modifiedBroadcaster = eventBroadcaster.select(Modified.Literal.INSTANCE);
      assert modifiedBroadcaster != null;
      final javax.enterprise.event.Event<Event> deletedBroadcaster = eventBroadcaster.select(Deleted.Literal.INSTANCE);
      assert deletedBroadcaster != null;
      final javax.enterprise.event.Event<Event> errorBroadcaster = eventBroadcaster.select(Error.Literal.INSTANCE);
      assert errorBroadcaster != null;
      
      // TODO: restrict namespace, filter events using strategy found
      // here:
      // http://stackoverflow.com/questions/32894599/how-do-i-get-events-associated-with-a-pod-via-the-api#comment53651235_32898853
      this.watch = client.events().inAnyNamespace().watch(new Watcher<Event>() {
          @Override
          public final void eventReceived(final Action action, final Event resource) {
            javax.enterprise.event.Event<Event> specificBroadcaster = null;
            if (action != null) {
              switch (action) {
              case ADDED:
                specificBroadcaster = addedBroadcaster;
                break;
              case MODIFIED:
                specificBroadcaster = modifiedBroadcaster;
                break;
              case DELETED:
                specificBroadcaster = deletedBroadcaster;
                break;
              case ERROR:
                specificBroadcaster = errorBroadcaster;
                break;
              default:
                throw new IllegalStateException();
              }
            }
            assert specificBroadcaster != null;
            specificBroadcaster.fire(resource);
          }
          
          @Override
          public final void onClose(final KubernetesClientException kubernetesClientException) {
            if (kubernetesClientException != null) {
              closeException = kubernetesClientException;
            }
          }
        });
      this.fireBlockingEvent(beanManager);
    }
  }

  private final void stopWatching(@Observes @BeforeDestroyed(ApplicationScoped.class) @Priority(LIBRARY_BEFORE) final Object event) throws Exception {
    final Closeable watch = this.watch;
    if (watch != null) {
      KubernetesClientException closeException = this.closeException;
      try {
        watch.close();
      } catch (final Exception everything) {
        if (closeException != null) {
          closeException.addSuppressed(everything);
          throw closeException;
        } else {
          throw everything;
        }
      }
      if (closeException != null) {
        throw closeException;
      }
    }
  }

}
