[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# Java Agent for OpenTracing

Java Agent for instrumenting Java applications using an OpenTracing compliant Tracer.

The instrumentation is performed in a non-intrusive manner leveraging the [ByteMan project](http://byteman.jboss.org/) to
define a set of rules. These rules can be used in three ways:

* Directly instrument a technology/framework (e.g. java.net.HttpURLConnection)

* Install a framework integration (e.g. OkHttp3)

* Define custom rules specific to an application (e.g. create spans to scope important internal units of work,
or add tags to an existing span to identify business relevant properties)

## Usage

The Java Agent can be used in two ways:

### Tracer and Framework Integrations on Class Path

This approach uses the plain `opentracing-agent.jar` provided by this project, and obtains the OpenTracing
Tracer and any required framework integrations from the classpath.

```java
java -javaagent path/to/opentracing-agent.jar ...
```

### Uber Jar

The other approach is to build an uber jar, using the maven assembly plugin, to package together
the `opentracing-agent.jar`, the OpenTracing compliant `Tracer`, any framework integrations, etc.

This approach is useful when wanting to instrument applications where modification of the classpath is not
possible (e.g. executable jars), or wanting to maintain separation between the application and the tracing
mechanism.

## Supported framework integrations

NOTE: Currently the ByteMan rules for installing tracing filters/interceptors into the following frameworks
is contained in this agent. However in the future the aim would be to move the rules to their associated framework
integration projects, meaning that the rules would be detected only when the integration artifact is added to
the classpath.

* [Servlet](https://github.com/opentracing-contrib/java-web-servlet-filter)
  * Jetty

* [OkHttp](https://github.com/opentracing-contrib/java-okhttp)

## Development
```shell
./mvnw clean install
```

## Release
Follow instructions in [RELEASE](RELEASE.md)

   [ci-img]: https://travis-ci.org/opentracing-contrib/java-agent.svg?branch=master
   [ci]: https://travis-ci.org/opentracing-contrib/java-agent
   [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-agent.svg?maxAge=2592000
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-agent
