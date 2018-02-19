# microbean-kubernetes-client-cdi

[![Build Status](https://travis-ci.org/microbean/microbean-helm.svg?branch=master)](https://travis-ci.org/microbean/microbean-kubernetes-client-cdi)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.microbean/microbean-kubernetes-client-cdi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.microbean/microbean-kubernetes-client-cdi)

# Overview

The [microbean-kubernetes-client-cdi][github-location] project makes
an [`ApplicationScoped`][application-scoped], [`Default`][default]-qualified
[`DefaultKubernetesClient`][default-kubernetes-client] available in
your CDI 2.0 application.

# Usage

Place the jar file this project produces on your CDI 2.0 application's
classpath.  Then [`@Inject`][inject] a
[`DefaultKubernetesClient`][default-kubernetes-client] (or any of its
supertypes) where you need to use it.

[github-location]: https://github.com/microbean/microbean-kubernetes-client-cdi
[application-scoped]: http://docs.jboss.org/cdi/api/2.0/javax/enterprise/context/ApplicationScoped.html
[default-kubernetes-client]: https://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/DefaultKubernetesClient.html
[default]: http://docs.jboss.org/cdi/api/2.0/javax/enterprise/inject/Default.html
[inject]: https://static.javadoc.io/javax.inject/javax.inject/1/javax/inject/Inject.html
