# Kotlin Support

[Documentation map](../../README.md)

XIS supports Kotlin for controller classes, components, form objects, and standalone XIS Boot applications. The same
server-driven programming model applies: controllers declare pages, model data, form data, and actions; HTML templates
declare the UI.

Kotlin works on the normal JVM path and with XIS Boot Native. That means a Kotlin XIS Boot application can be packaged as
a runnable JVM jar or compiled to a GraalVM native executable with the XIS Gradle plugin.

## Requirements

Use the Kotlin JVM plugin together with the XIS Gradle plugin:

`build.gradle`

```groovy
plugins {
    id "java"
    id "org.jetbrains.kotlin.jvm" version "2.0.21"
    id "one.xis.plugin" version "0.16.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
    implementation "org.jetbrains.kotlin:kotlin-stdlib"
}
```

For native images, use `xis-boot-native`:

```groovy
dependencies {
    implementation "one.xis:xis-boot-native"
    implementation "org.jetbrains.kotlin:kotlin-stdlib"
}
```

## Controller Example

`src/main/kotlin/example/KotlinPage.kt`

```kotlin
package example

import one.xis.Action
import one.xis.FormData
import one.xis.HtmlFile
import one.xis.ModelData
import one.xis.Page
import one.xis.PathVariable
import one.xis.QueryParameter
import one.xis.validation.Mandatory

@Page("/kotlin/{id}.html")
@HtmlFile("KotlinPage.html")
class KotlinPage {

    private var saved = KotlinForm(name = "initial", amount = 3)

    @ModelData("headline")
    fun headline(@PathVariable("id") id: Int, @QueryParameter("mode") mode: String?): String {
        return "Item $id ${mode ?: ""}".trim()
    }

    @FormData("form")
    fun form(): KotlinForm = saved

    @ModelData("saved")
    fun saved(): String = "${saved.name}:${saved.amount}"

    @Action("save")
    fun save(@FormData("form") form: KotlinForm) {
        saved = form
    }
}

class KotlinForm(
    @field:Mandatory
    var name: String = "",
    var amount: Int = 0
)
```

`src/main/kotlin/example/KotlinPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <title>Kotlin</title>
</head>
<body>
<h1>${headline}</h1>
<p>${saved}</p>

<form xis:binding="form">
    <input xis:binding="name" xis:error-class="error"/>
    <div xis:message-for="name"></div>

    <input xis:binding="amount" type="number" xis:error-class="error"/>
    <div xis:message-for="amount"></div>

    <button xis:action="save">Save</button>
</form>
</body>
</html>
```

The action receives a deserialized `KotlinForm`. XIS validates the form before the action runs. If validation fails, the
action is not called, the submitted values stay visible, and validation messages/classes are rendered like in Java and
Groovy controllers.

For form and model DTOs, prefer mutable properties with defaults or nullable properties. XIS creates and fills these
objects from Java-side framework code. Kotlin non-null types are compile-time contracts for Kotlin callers, but Java
reflection can still leave a property unset or pass `null` into generated accessors. Defaults like `var name: String = ""`
are therefore friendlier than `lateinit var name: String` or constructor-only DTOs without defaults.

## Template Location

For Kotlin controllers, you can place templates next to the controller under `src/main/kotlin` or under
`src/main/resources` using the controller package path. XIS copies `*.html` files from `src/main/kotlin` to the runtime
resources when the Kotlin JVM plugin is applied.

You normally do not need `@HtmlFile` when the template follows the default name and package location. Use
`@HtmlFile("KotlinPage.html")` only when you want to name the template explicitly; the file name is then resolved
relative to the controller package in both source layouts.

## XIS Boot

Standalone XIS Boot applications can use `@XISBootApplication` on a Kotlin class:

```kotlin
package example

import one.xis.boot.XISBootApplication
import one.xis.boot.XISBootRunner

@XISBootApplication
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            XISBootRunner.run(Application::class.java, args)
        }
    }
}
```

The XIS Gradle plugin can then package the application:

```bash
./gradlew xisJar
```

## Native Images

Kotlin is supported by XIS Boot Native. Use `xis-boot-native` and the normal native tasks:

```bash
./gradlew xisNativeCompile
./gradlew xisNativeRun
./gradlew xisNativeSmokeTest
```

The plugin scans Kotlin source files for XIS components, copies Kotlin-side templates into runtime resources, generates
native resource metadata, and creates the native runner. Native startup uses generated catalogs instead of runtime
classpath scanning, just like Java native applications.

## Tested Behavior

XIS has end-to-end coverage for these Kotlin paths:

- component scanning and singleton creation
- constructor injection
- `@Bean` method invocation
- default template resolution next to a Kotlin controller
- explicit `@HtmlFile` template resolution next to a Kotlin controller
- page rendering with `@ModelData`
- getter-like `@ModelData`
- form initialization with `@FormData`
- action invocation with deserialized `@FormData`
- XIS Boot Native compilation and native smoke startup
