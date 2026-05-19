# Groovy Support

[Documentation map](../README.md)

XIS supports Groovy 4+ for controller classes and form/data objects. The same server-driven programming model applies:
controllers declare pages, model data, form data, and actions; HTML templates declare the UI.

This can be useful when a team wants a lighter JVM syntax while keeping the XIS model: no separate client build, no
hand-written REST layer for normal interactions, and the same validation and form handling used by Java applications.

Java remains the primary and fastest path. Groovy support does not change the runtime path for Java applications.

## Requirements

Use Groovy 4 or newer:

`build.gradle`

```groovy
plugins {
    id "java"
    id "groovy"
    id "one.xis.plugin" version "0.11.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
    implementation "org.apache.groovy:groovy:4.0.24"
}
```

Groovy 3 is not supported for XIS annotations. Several XIS validation and formatting annotations also support Java
record components. Groovy 3 can fail during compilation when it sees annotations with `ElementType.RECORD_COMPONENT`;
Groovy 4 handles this correctly.

## Controller Example

`src/main/groovy/example/GroovyPage.groovy`

```groovy
package example

import one.xis.Action
import one.xis.FormData
import one.xis.HtmlFile
import one.xis.ModelData
import one.xis.Page
import one.xis.PathVariable
import one.xis.QueryParameter
import one.xis.validation.Mandatory

@Page('/groovy/{id}.html')
@HtmlFile('GroovyPage.html')
class GroovyPage {

    private GroovyForm saved = new GroovyForm(name: 'initial', amount: 3)

    @ModelData('headline')
    String headline(@PathVariable('id') Integer id, @QueryParameter('mode') String mode) {
        "Item ${id} ${mode}"
    }

    @FormData('form')
    GroovyForm form() {
        saved
    }

    @ModelData('saved')
    String saved() {
        "${saved.name}:${saved.amount}"
    }

    @Action('save')
    void save(@FormData('form') GroovyForm form) {
        saved = form
    }
}

class GroovyForm {
    @Mandatory
    String name

    int amount
}
```

`src/main/resources/example/GroovyPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="http://xis.one/html">
<head>
    <title>Groovy</title>
</head>
<body>
<h1>${headline}</h1>
<p>${saved}</p>

<form xis:binding="form">
    <input xis:binding="name" xis:error-class="error"/>
    <div xis:message-for="name"></div>

    <input xis:binding="amount" xis:error-class="error"/>
    <div xis:message-for="amount"></div>

    <button xis:action="save">Save</button>
</form>
</body>
</html>
```

The action receives a deserialized `GroovyForm`. XIS validates the form before the action runs. If validation fails,
the action is not called, the submitted values stay visible, and validation messages/classes are rendered like in Java
controllers.

## Template Location And Scaffolding

For Groovy controllers, you can place templates next to the controller under `src/main/groovy` or under
`src/main/resources` using the controller package path. XIS copies `*.html` files from `src/main/groovy` to the runtime
resources when the Gradle Groovy plugin is applied.

You normally do not need `@HtmlFile` when the template follows the default name and package location. Use
`@HtmlFile('GroovyPage.html')` only when you want to name the template explicitly; the file name is then resolved relative
to the controller package in both source layouts.

When the Gradle project applies both the Groovy plugin and the XIS plugin, the normal XIS scaffolding tasks also scan
Groovy controllers:

```bash
./gradlew xisTemplates xisTests
```

For Groovy controllers, generated templates are written below `src/main/resources` using the controller package path.
Generated starter integration tests are Groovy tests below `src/test/groovy`. They create an `IntegrationTestContext` in
`@BeforeEach`, register the Groovy page controller explicitly, and use the test starter dependency that the XIS Gradle
plugin adds automatically.

The same controller-first workflow applies to Groovy: sketch the page or frontlet class and its `@ModelData`,
`@FormData`, and `@Action` methods before running `xisTemplates`. The generated template then reflects that controller
shape with expressions, repeated blocks, bound forms, validation placeholders, and action buttons.

## XIS Boot

Standalone XIS Boot applications can also use `@XISBootApplication` on a Groovy class. The XIS Gradle plugin enables
Java annotation processing for Groovy compilation when the Groovy plugin is present, so `xisJar` can generate and package
the boot runner.

The resulting jar contains Java classes, Groovy classes, resources, and the generated `one.xis.boot.Runner`.

## Native Images

Groovy applications are currently supported on the JVM path, not on the XIS Boot Native path. Even statically compiled
Groovy code brings parts of the Groovy runtime into the GraalVM native-image analysis, including Groovy's meta-class and
dynamic call-site infrastructure. That currently makes native builds fail before the application can be treated like a
normal XIS native application.

Use Java for applications that should be compiled with `xis-boot-native`. Groovy support may become possible later, but
it needs dedicated Groovy/GraalVM work and is not part of the supported native workflow today.

## Tested Behavior

XIS has tests for these Groovy paths:

- component scanning and singleton creation
- constructor injection
- field injection
- `@Bean` method invocation
- `@Init` method invocation
- object deserialization from a controller method parameter
- page rendering with `@ModelData`
- form initialization with `@FormData`
- action invocation with deserialized `@FormData`
- validation failures on Groovy form objects

These tests run with Groovy 4.
