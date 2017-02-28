[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# Java Agent for OpenTracing

Java Agent for instrumenting Java applications using an OpenTracing compliant Tracer.

The instrumentation is performed in a non-intrusive manner leveraging the [ByteMan project](http://byteman.jboss.org/) to
define a set of rules. These rules can be used in three ways:

* Directly instrument a technology/framework (e.g. java.net.HttpURLConnection)

* Install a framework integration (e.g. OkHttp)

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

#### Spring Boot Example

If instrumenting a spring-boot application, add the following to the pom.xml:

```xml
    <!-- OpenTracing Tracer dependencies -->
    ....

    <!-- OpenTracing Contrib JavaAgent Rules -->
    <dependency>
      <groupId>io.opentracing.contrib</groupId>
      <artifactId>opentracing-agent-rules-java-net</artifactId>
      <version>...</version>
    </dependency>
    <dependency>
      <groupId>io.opentracing.contrib</groupId>
      <artifactId>opentracing-agent-rules-java-web-servlet-filter</artifactId>
      <version>...</version>
    </dependency>

    <!-- OpenTracing Contrib Framework Integrations -->
    <dependency>
      <groupId>io.opentracing.contrib</groupId>
      <artifactId>opentracing-web-servlet-filter</artifactId>
      <version>...</version>
    </dependency>
```

and then launch the application using

```
mvn spring-boot:run -Drun.jvmArguments=-javaagent:/path/to/opentracing-agent.jar
```

### Uber Jar

The other approach is to build an uber jar, using the maven assembly plugin, to package together
the `opentracing-agent.jar`, the OpenTracing compliant `Tracer`, any framework integrations, etc.

This approach is useful when wanting to instrument applications where modification of the classpath is not
possible (e.g. executable jars), or wanting to maintain separation between the application and the tracing
mechanism.


## Creating custom rules

Custom rules are defined using the [ByteMan](http://byteman.jboss.org/) rule format. These rules use
a helper class (_io.opentracing.contrib.agent.OpenTracingHelper_) that provides access to the OpenTracing Tracer,
as well as some additional support capabilities.

The example rules should be placed in a folder on the classpath called `otagent`, and have the file extension
`btm`.

Some example rules are:

```
RULE Custom instrumentation rule sayHello entry
CLASS example.MyClass
METHOD sayHello()
HELPER io.opentracing.contrib.agent.OpenTracingHelper
AT ENTRY
IF TRUE
DO
  manageSpan(getTracer().buildSpan("MySpan").start());
ENDRULE
```

The first line defines the name of the rule. The second identifies the target class, although it is also
possibly to specify an interface. The third line identifies the method name (optionally specifying the
parameter types).

The _AT_ clause identifies the point at which the identifed method will be instrumented. _ENTRY_ means that
the rule should be applied at the beginning of the method (see ByteMan documentation for other locations).

The _IF_ statement enables a predicate to be defined to guard whether the rule is performed.

The _DO_ clause identifies the actions to be performed when the rule is triggered.

The `getTracer()` method provides access to the OpenTracing compliant `Tracer`. The helper provides methods
for managing the current active span (i.e. `manageSpan`).

NOTE: Span management is being actively discussed in the OpenTracing standard so this area may change in the
near future.

```
RULE Custom instrumentation rule sayHello exit
CLASS example.MyClass
METHOD sayHello()
HELPER io.opentracing.contrib.agent.OpenTracingHelper
AT EXIT
IF currentSpan() != null
DO
  currentSpan().setTag("status.code","OK").finish();
  releaseCurrentSpan();
ENDRULE
```
This rule will trigger _AT EXIT_, so when the method is finished. The _IF_ statement checks whether there
is a current span, so will only trigger if an active span exists.

The actions performed in this case are to set a tag _status.code_ on the current span, and then finish it.
Finally the current span needs to be released so that it is no longer considered the active span.


## Supported framework integrations

NOTE: Currently the ByteMan rules for installing tracing filters/interceptors into the following frameworks
is contained in this agent. However in the future the aim would be to move the rules to their associated framework
integration projects, meaning that the rules would be detected only when the integration artifact is added to
the classpath.

* [Servlet](https://github.com/opentracing-contrib/java-web-servlet-filter) Currently supported containers:
  * Jetty
  * Tomcat

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
