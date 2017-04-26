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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;

import javax.enterprise.event.Observes;

import javax.enterprise.inject.Any;

import javax.enterprise.inject.spi.EventMetadata;

import org.junit.Test;

import org.microbean.kubernetes.client.cdi.annotation.Added;
import org.microbean.kubernetes.client.cdi.annotation.Modified;

import org.microbean.main.Main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.fabric8.kubernetes.api.model.Event;

@ApplicationScoped
public class TestKubernetesClientExtension {

  
  /*
   * Static fields.
   */

  
  /**
   * The number of instances of this class that have been created (in
   * the context of JUnit execution; any other usage is undefined).
   */
  private static int instanceCount;


  /*
   * Constructors
   */
  
  
  public TestKubernetesClientExtension() {
    super();
    instanceCount++;
  }


  /*
   * Instance methods.
   */

  private final void onStartup(@Observes @Initialized(ApplicationScoped.class) final Object event) {

  }

  // See
  // http://www.next-presso.com/2014/06/you-think-you-know-everything-about-cdi-events-think-again/,
  // particularly the bit about how @Any is useless on event
  // observers.  Because here it is critical.
  private final void onEvent(@Observes @Any final Event event, final EventMetadata eventMetadata) {
    System.out.println("*** event received: " + event);
    System.out.println("*** metadata: " + eventMetadata);
  }
  
  @Test
  public void testContainerStartup() {
    final int oldInstanceCount = instanceCount;
    Main.main(null);
    assertEquals(oldInstanceCount + 1, instanceCount);
  }
  
}
