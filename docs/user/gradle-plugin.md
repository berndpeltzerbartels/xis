# Gradle Plugin And Tools

[Documentation map](../README.md)

The XIS Gradle plugin keeps the normal project workflow small. It adds the annotation processor, copies HTML templates
that live next to Java controllers into the application resources, configures the XIS integration-test starter, and
provides comfortable tools for scaffolding, validation, local runs, and CI pipelines.

Use the plugin when you want the usual XIS layout:

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.11.2"
}
```

You still choose the runtime dependency yourself:

```groovy
dependencies {
    implementation "one.xis:xis-spring"
}
```

or:

```groovy
dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
}
```

## What The Plugin Configures

The plugin applies the Java plugin and configures the normal XIS build support:

| Area | What happens |
| --- | --- |
| HTML templates | `.html` files under `src/main/java` are copied into the runtime resources. |
| Annotation processing | `xis-apt` is added as annotation processor with the same version as the plugin. |
| Integration tests | `xis-boot-starter-test` is added for tests and JUnit Platform is enabled. The starter brings `xis-test`, `@XisBootTest`, and JUnit Jupiter. |
| Dependency versions | XIS dependencies used by the plugin are aligned to the plugin version. |
| XIS catalogs | The plugin generates component catalogs for reusable XIS libraries. Projects that also declare `xis-boot-native` generate native catalogs. |
| XIS validation | `xisValidate` runs XIS validation checks. The task is intended for local development and CI pipelines. |
| XIS Boot jars | Projects that use `xis-boot` get `xisJar` and `xisRun` tasks. |

This means a normal application does not need to wire the XIS annotation processor, `xis-test`, or
`xis-boot-starter-test` manually.
It also means XIS runtime dependencies can normally be declared without a version. The plugin version selects the
matching XIS artifact version.

## XIS Catalogs For Libraries

Applying `one.xis.plugin` marks the project as a XIS artifact. The plugin therefore generates catalog resources for
framework-managed classes such as components, pages, frontlets, modals, repositories, and proxy interfaces.

Those catalogs are especially important for XIS Boot Native. A native application can consume a reusable XIS library
without source access when the library jar contains the generated XIS catalog resources. If the library contributes XIS
components to a native executable, it must also declare `xis-boot-native`; that explicit dependency opts the library into
native catalog generation.

A plain Java library without the XIS plugin does not automatically contribute XIS components to a native application.
That is fine for ordinary helper classes. If the library contains classes that XIS should manage on the JVM, build it as
a XIS artifact by applying `one.xis.plugin`. If those classes should also be available in a native executable, add
`xis-boot-native` as well.

## `xisTemplates`

The `xisTemplates` task generates missing HTML templates for page and frontlet controllers:

```bash
./gradlew xisTemplates
```

The task is most useful when you write the controller, or at least its public shape, before writing the template. Add
the page or frontlet annotation, sketch the `@ModelData`, `@FormData`, and `@Action` methods, and then run
`xisTemplates`. The generated HTML tries to reflect that controller shape instead of only creating an empty file. This
keeps binding names, form names, and action names close to the Java code and removes a large part of the repetitive
template boilerplate.

If you create this class:

```java
package example.dashboard;

import one.xis.Page;

@Page("/dashboard.html")
class DashboardPage {
}
```

the task creates:

```text
src/main/java/example/dashboard/DashboardPage.html
```

The generated file is only a starting point. It is written next to the Java controller so the controller and template can
be edited together. Existing templates are not overwritten.

When the controller already exposes `@ModelData`, `@FormData`, or `@Action` methods, the generated template uses them as
a starter sketch:

| Controller member | Generated template sketch |
| --- | --- |
| scalar `@ModelData` | A simple expression such as `${title}`. |
| iterable `@ModelData` | A small `xis:foreach` block with one repeated item. |
| `@FormData` | A form with `xis:binding`, labels, inputs, `xis:message-for`, and error styling hooks. |
| `@Action` with matching form data | A form submit button using `xis:action`. |
| standalone `@Action` | A button with `type="button"` and `xis:action`. |

Form fields are derived from record components or non-static fields. The generator uses simple input type hints:
booleans become checkboxes, numeric values become number inputs, date-like values become date inputs, and other values
become text inputs. If a form action has a `@FormData("name")` parameter, the generated form uses that action. Otherwise
the form gets a neutral `save` action name that you can edit.

The generator intentionally produces plain HTML. It does not try to design the page, choose domain wording, or decide
the final layout for you. Treat the result as a first executable template that already knows the controller contract.

When the project also applies the Groovy or Kotlin plugin, `xisTemplates` scans those page and frontlet controllers too.
For Groovy and Kotlin controllers, generated templates are written under `src/main/resources` with the controller package
path.

## `xisTests`

The `xisTests` task generates missing starter integration tests for page controllers:

```bash
./gradlew xisTests
```

For the same page controller, the task creates:

```text
src/test/java/example/dashboard/DashboardPageTest.java
```

The generated test uses `@XisBootTest`, receives an `IntegrationTestContext` field, and opens the page through its
`@Page` URL. It is meant as a first executable sketch, not as a finished test suite:

```java
import one.xis.boot.test.XisBootTest;
import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@XisBootTest
class DashboardPageTest {

    private IntegrationTestContext context;

    @Test
    void test() {
        var client = context.openPage("/dashboard.html");

        assertNotNull(client.getDocument());
    }
}
```

The test starter needed for this generated code is added automatically by the plugin. Do not add `xis-test` or
`xis-boot-starter-test` again in a normal plugin-based build.

When the project also applies the Groovy plugin, `xisTests` scans Groovy page controllers too and writes Groovy starter
tests under `src/test/groovy`. Kotlin controllers are scanned for template validation and runtime catalogs; write Kotlin
tests manually or use generated Java tests as a starting point.

This supports a TDD-style workflow:

```bash
./gradlew xisTemplates xisTests
```

Then edit the generated test until it describes the page behavior you want, implement the template and Java code, and
run the normal Gradle `test` task.

## `xisValidate`

The `xisValidate` task runs XIS validation checks without starting the application:

```bash
./gradlew xisValidate
```

It is meant for the normal development loop and for CI pipelines. By default, it stops at the first validation error so
CI logs stay short and the first problem is easy to see. To collect all validation errors in one run, use:

```bash
./gradlew xisValidate --all-errors
```

The task validates the HTML with the same XIS HTML parser that is used at runtime. A parser error here means the
application would fail when the page or frontlet is loaded.

The current checks cover the places where mistakes are easy to miss in a browser:

| Area | Examples |
| --- | --- |
| Attribute dependencies | `xis:format` requires `xis:binding`; `xis:error-class` and `xis:error-style` require `xis:binding` or `xis:error-binding`. |
| Mandatory attributes | `<xis:foreach>` requires `var` and `array`; `<xis:if>` requires `condition`; `<xis:include>` requires `name`; `<xis:frontlet-container>` requires `container-id`. |
| Attribute syntax | `xis:foreach`, `xis:repeat`, and `xis:drag` use `name:expression`; `xis:drop` uses `actionName(...)`. |
| Framework element syntax | `<xis:a>` and `<xis:button>` need a navigation or action target; `<xis:parameter>` needs `name`; storage bindings need a supported store. |
| Attribute placement | navigation attributes belong on links or buttons; form bindings belong on form controls or labels. |
| Selection styling | `xis:selection-class` needs a surrounding `xis:selection-group`. |
| Template data | expressions such as `${customer.firstName}` must have matching `@ModelData`; exposed model/form data must be used by the template. |
| Template properties | property paths such as `${customer.address.city}` are checked against fields, record components, getters, setters, and inherited members where the type is known. Repeat variables are checked against the element type of the repeated model data. |
| Form fields | bindings inside `<form xis:binding="...">` or `<xis:form binding="...">` are checked against the matching `@FormData` object. |

The validation is intentionally a preflight check, not a replacement for tests. It catches most common mistakes around
template variables, form bindings, missing attributes, and misspelled properties before the application starts. A
successful validation run does not prove that the page is behaviorally correct: dynamic expressions, custom JavaScript,
EL function semantics, database state, permissions, and browser behavior still belong in integration and e2e tests.

## `xisJar`

When the project declares `xis-boot`, the plugin adds:

```bash
./gradlew xisJar
```

The task creates an executable XIS Boot jar with the XIS Boot runner as main class. Spring Boot applications normally use
the Spring Boot plugin tasks instead.

The jar is written to `build/libs` and uses Gradle's normal archive name:

```text
build/libs/<project-name>-<version>.jar
```

For example:

```bash
java -jar build/libs/my-app-1.0-SNAPSHOT.jar
```

`xisJar` requires exactly one application class annotated with `@XISBootApplication`. The XIS annotation processor
uses that class to generate the executable entry point.

When the project also applies the Groovy or Kotlin plugin, the `@XISBootApplication` class may be written in that
language. The XIS plugin packages Java, Groovy, or Kotlin classes, templates, resources, and the generated runner into
the XIS jar.

## `xisRun`

When the project declares `xis-boot`, the plugin also adds:

```bash
./gradlew xisRun
```

`xisRun` always depends on `xisJar` and starts the jar created by that task. It runs `one.xis.boot.Runner` with
the XIS jar on the classpath, so Gradle and IDEs can treat it like a normal Java run task. You do not need to
configure the jar dependency yourself.

To run on a specific port, pass the port option:

```bash
./gradlew xisRun --port=9090
```

Additional application arguments can be passed as a single string:

```bash
./gradlew xisRun --args="9090"
```

To debug the application from an IDE, use Gradle's standard Java debug option and attach to port `5005`:

```bash
./gradlew xisRun --debug-jvm
```

## When To Add Test Dependencies Yourself

If you use the XIS Gradle plugin, the test starter is already added for tests. Generated tests should compile without
an explicit XIS test dependency in your `dependencies` block.

Without the plugin, add the dependency that matches the style of test you want:

```groovy
dependencies {
    testImplementation "one.xis:xis-boot-starter-test:0.11.2"
}
```

Use this when you want `@XisBootTest`, generated-test style, and JUnit Jupiter. If you only want the lower-level
`IntegrationTestContext` API and manage JUnit yourself, add `one.xis:xis-test` explicitly instead. In builds without the
plugin, use the same XIS version for all XIS modules.

See [Examples and tests](examples-and-tests.md) for the testing API and examples.

## Builds Without The Plugin

XIS can be used without the Gradle plugin, but then the build is explicit. You must add versions to XIS dependencies
yourself and wire the parts that the plugin normally configures:

- the XIS annotation processor
- copying HTML templates from `src/main/java` into runtime resources, if you use that layout
- XIS test dependencies and JUnit Platform configuration
- executable XIS Boot jar or run tasks, if you want plugin-like convenience

For normal JVM applications this is possible, just more verbose. It can be useful in builds that already have their own
strict build conventions.

For XIS Boot Native, the Gradle plugin is the supported path. Native images need generated component catalogs, native
class catalogs, resource catalogs, reflection configuration, proxy configuration, and a generated native runner. Without
the plugin, all of that metadata would have to be produced and wired into `native-image` manually. That is intentionally
not documented as a normal user workflow.
