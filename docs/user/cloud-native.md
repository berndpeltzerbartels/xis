# Cloud Native And Native Images

[Documentation map](../../README.md)

XIS Boot Native is the cloud-native deployment path for standalone XIS applications. It is meant for applications that
should be compiled to a GraalVM native executable and deployed as a small container or directly as a native process.

The goal is not merely that XIS does not block native-image builds. XIS generates the metadata a native image needs:
component catalogs, resource catalogs, reflection configuration, proxy configuration, and a native runner. Application
code should therefore stay close to normal XIS Boot code.

## Basic Build

Use `xis-boot-native` instead of `xis-boot`:

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.19.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot-native"
}
```

The XIS Gradle plugin generates the native catalogs and runner when the application depends on `xis-boot-native`.
A native build needs:

- generated component catalogs for framework modules and application classes
- generated resource catalog for HTML, CSS, JavaScript, properties, and other bundled resources
- generated reflection configuration
- generated dynamic proxy configuration
- a generated runner that starts the XIS Boot Native context
- a local GraalVM installation with `native-image`

The plugin looks for `native-image` in this order: an explicitly configured GraalVM home (`-PgraalVmHome=...` or
`--graal-vm-home=...`), `GRAALVM_HOME`, `JAVA_HOME`, `PATH`, and then common local JDK installation directories. It does
not require a specific GraalVM distribution. If Oracle GraalVM is installed and selected through one of those mechanisms,
the plugin uses it.

Native Image performance depends noticeably on the GraalVM distribution, version, CPU, and build options. In XIS'
server-throughput benchmarks, native executables have not consistently beaten the JVM; the strongest reason to choose
native images has been lower memory use, fast startup, and compact deployment. For throughput-sensitive services, test
the exact target environment before assuming a native executable is faster. If Oracle GraalVM performs better for your
workload than another Native Image distribution, point the plugin at that installation explicitly.

The native plugin tasks are:

```bash
./gradlew xisNativeCompile
./gradlew xisNativeCompileForHost
./gradlew xisNativeRun
./gradlew xisNativeSmokeTest
```

`xisNativeCompile` builds the executable, `xisNativeRun` starts it locally, and `xisNativeSmokeTest` builds the executable,
starts it on a temporary port, verifies that the HTTP server answers, and stops it again.

`xisNativeCompileForHost` builds a second executable optimized for the current build host and passes `-march=native` to
`native-image`. Use it when the build runs on the same machine, VM, or container type that will later run the binary.
The executable is written next to the portable binary with a `-host` suffix.

XIS leaves the GraalVM optimization level at the documented native-image default. Additional GraalVM `native-image`
arguments, such as `-O3`, can be passed either as a task option or as a Gradle property:

```bash
./gradlew xisNativeCompile --native-image-args="-O3"
./gradlew xisNativeCompile -PxisNativeImageArgs="-O3"
./gradlew xisNativeCompileForHost -PxisNativeImageArgs="-O3"
```

These arguments affect the executable at build time. Runtime arguments for the application still belong to
`xisNativeRun --application-args`. CPU-specific options such as `-march=native` are intentionally isolated in
`xisNativeCompileForHost` because the resulting executable is tied more closely to the build machine.

## Application Entry Point

Application code still starts from a normal XIS Boot application class:

```java
package example;

import one.xis.boot.XISBootApplication;

@XISBootApplication
class Application {
}
```

For native images, the generated runner uses generated catalogs instead of runtime package scanning.

## Java, Kotlin, And Groovy

XIS Boot Native supports Java and Kotlin applications. The Gradle plugin generates component catalogs, resource
metadata, reflection/proxy configuration, and the native runner for both source layouts.

Groovy controllers and forms are supported on the JVM path, but not as GraalVM native images. Even with static Groovy
compilation, the Groovy runtime can make dynamic call-site and meta-class infrastructure reachable during native-image
analysis.

If an application must be compiled with `xis-boot-native`, write the native application code in Java or Kotlin.

## Database Modules

Native database support is explicit. Add the normal persistence module plus the native driver module for the DBMS used by
the application.

This is different from a normal JVM build. On the JVM, a JDBC driver can often be added as `runtimeOnly` and discovered
later through `DriverManager` or the Java service loader. A GraalVM native image is closed at build time: the driver
classes, service registrations, resources, reflection needs, and initialization behavior must be known while the
executable is built. If a driver is only treated as an ordinary late runtime dependency, the application may compile and
even work on the JVM but fail during `native-image` or inside the native executable.

The `xis-boot-native-*` database modules make that dependency explicit. They provide tested default driver versions and
put the driver on the native build path in the way XIS expects. Applications may still override the driver version with
normal Gradle dependency management, but that replacement driver then belongs to the application's native test scope.

### H2

H2 is useful for local development, tests, small embedded applications, and smoke tests:

```groovy
dependencies {
    implementation "one.xis:xis-boot-native"
    implementation "one.xis:xis-sql"
    implementation "one.xis:xis-boot-native-h2"
}
```

### PostgreSQL

```groovy
dependencies {
    implementation "one.xis:xis-boot-native"
    implementation "one.xis:xis-sql"
    implementation "one.xis:xis-boot-native-postgresql"
}
```

### MariaDB

```groovy
dependencies {
    implementation "one.xis:xis-boot-native"
    implementation "one.xis:xis-sql"
    implementation "one.xis:xis-boot-native-mariadb"
}
```

### MongoDB

```groovy
dependencies {
    implementation "one.xis:xis-boot-native"
    implementation "one.xis:xis-boot-native-mongodb"
}
```

## Component Discovery

Normal XIS Boot can scan the classpath at startup. A native executable cannot rely on the same kind of runtime discovery.
For native builds, XIS generates catalogs during the build and starts the context from those catalogs.

This means:

- components, pages, frontlets, modals, includes, repositories, and proxy interfaces must be visible to the build
- artifacts built as XIS libraries can contribute generated catalogs, so reusable XIS modules can participate in native applications
- source-level Lombok is not a problem for this mechanism because XIS works with the compiled classes and generated
  catalogs, not with Lombok source transformations at runtime

If you are going to use Lombok as an annotation processor, use a Lombok version that supports the JDK used for the
build. For JDK 21 builds, use Lombok 1.18.34 or newer. Older Lombok versions can fail during compilation before XIS
native metadata generation starts.

## Reusable Libraries

Reusable XIS libraries can participate in native applications when they are built with the XIS Gradle plugin and declare
`xis-boot-native`. Applying `one.xis.plugin` marks the project as a XIS artifact; adding `xis-boot-native` opts the
artifact into native metadata generation. The library jar contributes its generated component and native class catalogs,
and the native application consumes those catalogs from its compile and runtime classpath.

A native application does not need source access to every library. It only needs the library artifact on the normal
Gradle classpath, and that artifact must contain the generated XIS catalog resources.

A library that was not built as a native XIS artifact does not automatically contribute XIS components to a native
application. If such a library only provides ordinary helper classes, that is fine. If it contains XIS components, pages,
repositories, or other framework-managed classes that should be available in a native executable, build it with the XIS
plugin and add `xis-boot-native` so the native metadata is part of the jar.

## Bean Methods

`@Bean` methods may return concrete application classes, application interfaces, or application base classes:

```java
@Component
class MailConfiguration {

    @Bean
    MailSender mailSender() {
        return new SmtpMailSender();
    }

    @Bean
    MessageFormatter messageFormatter() {
        return new HtmlMessageFormatter();
    }
}
```

This is important for readable application design. Consumers should depend on the meaningful application type, not on
the concrete implementation class:

```java
@Component
class CustomerNotifier {

    private final MailSender mailSender;

    CustomerNotifier(MailSender mailSender) {
        this.mailSender = mailSender;
    }
}
```

Avoid strongly generic return types in native applications:

```java
@Bean
Object mailSender() {
    return new SmtpMailSender();
}
```

The Java version can work at runtime, but it hides the meaningful type from the generated native metadata. In a native
build, XIS must know the types that can become beans, dependencies, proxies, and reflection targets. Use a concrete
class, an application interface, or an application base class instead.

## Proxies And AOP

XIS AOP and SQL repositories are interface-based. XIS Boot Native deliberately does not use Byte Buddy, CGLIB,
load-time weaving, or runtime bytecode enhancement. This makes native images smaller, easier to reason about, and more
predictable in cloud deployments.

If a component needs advice, use an interface:

```java
interface CustomerService {
    void createCustomer(Customer customer);
}
```

The implementation can carry the annotation, but the component must be injectable through the interface. This also
matches SQL repositories, which are proxy interfaces by design.

## Current Limitations

Native support is young and deliberately explicit. Expect these boundaries:

- the public Gradle plugin workflow for native builds may still change
- driver replacement is allowed, but only the default driver versions from the native DB modules are tested by XIS
- dynamic runtime classpath scanning is not part of the native startup model
- bytecode-enhanced AOP is intentionally unsupported
- generic `@Bean` return types such as `Object` or broad JDK marker interfaces are not suitable for native metadata

Those restrictions are part of the architecture, not temporary workarounds. They keep the cloud-native path predictable
and close to the normal XIS programming model.

## Logging

XIS uses SLF4J. XIS Boot ships a small default SLF4J provider, so standalone and native applications write framework
logs without requiring every application to add a logging backend first. The default provider is intentionally simple
and writes to the process output, which fits container platforms and local native executables.

Applications that need another logging backend can replace the default through normal Gradle dependency management.
