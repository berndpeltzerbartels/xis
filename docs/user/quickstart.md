# Quickstart

[Documentation map](../README.md)

This quickstart creates a small XIS application with Spring Boot. The standalone XIS Boot runtime follows the same page,
template, and action model; only the application entry point and dependencies differ.

## Prerequisites

- Java 17 or newer
- Gradle 8 or newer

Java is the shortest path through this quickstart. XIS also supports Groovy 4+ controllers and form DTOs; see
[Groovy support](groovy.md) when you want to write the server-side code in Groovy.

## Gradle Setup

`settings.gradle`

```groovy
rootProject.name = "my-xis-app"
```

`build.gradle`

```groovy
plugins {
    id "java"
    id "org.springframework.boot" version "3.3.0"
    id "io.spring.dependency-management" version "1.1.5"
    id "one.xis.plugin" version "0.11.1"
}

group = "example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-spring"
}

tasks.withType(JavaCompile) {
    options.encoding = "UTF-8"
}

test {
    useJUnitPlatform()
}
```

The `one.xis.plugin` plugin adds the XIS annotation processor, copies HTML templates from `src/main/java` into the
runtime resources, configures the XIS test starter, adds the XIS scaffolding tasks, aligns XIS dependency versions to
the plugin version, and adds XIS Boot run/build tasks when you use `xis-boot`. You do not add `xis-test` or
`xis-boot-starter-test` yourself in this setup; the plugin adds the matching test dependency automatically. See
[Gradle plugin and tools](gradle-plugin.md) for the complete task overview.

## Application Class

`src/main/java/example/Application.java`

```java
package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## Where Templates Go

XIS templates normally live next to the Java controller in the same package. If the controller is:

```text
src/main/java/example/dashboard/DashboardPage.java
```

then the matching template is:

```text
src/main/java/example/dashboard/DashboardPage.html
```

This is intentional. You usually work on the controller and template together, and keeping them side by side makes that
relationship visible in the project tree. The XIS Gradle plugin copies these HTML files into the application resources
during the build.

The plugin can generate missing templates for page and frontlet controllers:

```bash
./gradlew xisTemplates
```

Run this after adding a controller when you want XIS to create the template file in the right package. It is often worth
writing the controller methods first, even if their bodies are still simple. When `xisTemplates` sees `@ModelData`,
`@FormData`, and `@Action` methods, it can generate expressions, repeated blocks, forms, validation message placeholders,
and action buttons that already match the controller. Generated templates are starting points; edit them like normal
HTML.

The plugin can also generate starter integration tests for page controllers:

```bash
./gradlew xisTests
```

Generated tests use `@XisBootTest`, get an `IntegrationTestContext` field, and open the page through its URL. The
required XIS test starter is added automatically by the plugin. Generated tests compile before the full page behavior is
implemented, so you can use them for a TDD-style workflow: sketch the page class and its model/form/action methods, run
`./gradlew xisTemplates xisTests`, edit the generated test until it describes the UI behavior you want, then implement
the services and refine the template until the test passes.

When the first version of a page is ready, run XIS validation checks manually:

```bash
./gradlew xisValidate
```

This is also the command you usually want in a CI pipeline. It stops at the first validation problem by default and
points to the affected template file and line.

## First Page

The first page may feel familiar if you remember simpler request-oriented web models: a controller exposes data and a
template renders it. XIS keeps that simple programming shape, but it is not classic server-side rendering. The browser
runs a SPA-style DOM application; XIS hides the client-server protocol, DOM replacement, navigation, and refresh logic
under the hood.

`src/main/java/example/dashboard/DashboardPage.java`

```java
package example.dashboard;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.WelcomePage;

@WelcomePage
@Page("/index.html")
public class DashboardPage {

    @ModelData
    public String title() {
        return "Dashboard";
    }
}
```

Create the template manually or run `./gradlew xisTemplates` and refine the generated content. Because the controller
already has a `title` model method, the generated template contains a `${title}` expression as a useful starting point:

`src/main/java/example/dashboard/DashboardPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
</head>
<body>
    <h1>${title}</h1>
    <p>Your first XIS page is running.</p>
</body>
</html>
```

Run the app:

```bash
./gradlew bootRun
```

Open:

```text
http://localhost:8080/
```

`@WelcomePage` marks the default entry page. The page is also available directly at `/index.html`.

To generate a first test skeleton for the page, run:

```bash
./gradlew xisTests
```

This creates `src/test/java/example/dashboard/DashboardPageTest.java` if the file does not already exist. The generated
test is intentionally small: it uses `@XisBootTest`, receives an `IntegrationTestContext` field, opens `/index.html`,
and leaves space for assertions against the rendered document.

## Add an Action

`src/main/java/example/dashboard/CounterPage.java`

```java
package example.dashboard;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

@Page("/counter.html")
public class CounterPage {

    private int count;

    @ModelData
    public int count() {
        return count;
    }

    @Action
    public void increment() {
        count++;
    }
}
```

`src/main/java/example/dashboard/CounterPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Counter</title>
</head>
<body>
    <h1>Counter</h1>
    <p>Current count: ${count}</p>
    <button xis:action="increment">Increment</button>
</body>
</html>
```

The action is invoked through XIS. You do not create a REST endpoint for it.

## Add a Form With Validation

Forms bind HTML controls to a Java object. XIS deserializes the submitted values, validates annotations, and calls the
action only when validation succeeds.

`src/main/java/example/customer/CustomerPage.java`

```java
package example.customer;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.validation.EMail;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;

@Page("/customer.html")
public class CustomerPage {

    private CustomerForm saved;

    @FormData("customer")
    public CustomerForm customer() {
        return saved != null ? saved : new CustomerForm("", "");
    }

    @ModelData("savedMessage")
    public String savedMessage() {
        return saved == null ? "" : "Saved " + saved.name();
    }

    @Action
    public void save(@FormData("customer") CustomerForm customer) {
        this.saved = customer;
    }

    public record CustomerForm(
            @Mandatory @LabelKey("customer.name") String name,
            @Mandatory @EMail @LabelKey("customer.email") String email
    ) {
    }
}
```

`src/main/java/example/customer/CustomerPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Customer</title>
</head>
<body>
    <h1>Customer</h1>

    <form xis:binding="customer">
        <xis:global-messages/>

        <label for="name" xis:error-binding="name" xis:error-style="color: #b00020">Name</label>
        <input id="name" xis:binding="name" xis:error-class="error"/>
        <div xis:message-for="name"></div>

        <label for="email" xis:error-binding="email" xis:error-style="color: #b00020">Email</label>
        <input id="email" type="email" xis:binding="email" xis:error-class="error"/>
        <div xis:message-for="email"></div>

        <button type="submit" xis:action="save">Save</button>
    </form>

    <p>${savedMessage}</p>
</body>
</html>
```

`src/main/resources/messages.properties`

```properties
customer.name=Name
customer.email=Email
validation.mandatory=${label} is required
validation.email=Please enter a valid email address
```

`xis:message-for` prints the field message. `<xis:global-messages/>` prints form-level validation messages. `xis:error-class`
and `xis:error-style` let CSS or inline prototype styling highlight fields and labels while an error exists.

## Move The Form Into A Frontlet

Keeping the form directly on the page is fine for a small first page. When a part of the UI should be modular or
replaceable on its own, use a frontlet. A frontlet has its own controller and template fragment, and XIS can reload or
replace just that fragment instead of rebuilding the whole page.

`src/main/java/example/customer/CustomerFrontletPage.java`

```java
package example.customer;

import one.xis.Page;

@Page("/customer-frontlet.html")
public class CustomerFrontletPage {
}
```

`src/main/java/example/customer/CustomerFrontletPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Customer</title>
</head>
<body>
    <h1>Customer</h1>
    <main xis:frontlet-container="customer-main"
          xis:default-frontlet="CustomerFormFrontlet"></main>
</body>
</html>
```

`src/main/java/example/customer/CustomerFormFrontlet.java`

```java
package example.customer;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Frontlet;
import one.xis.FrontletResponse;
import one.xis.validation.EMail;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;

@Frontlet(containerId = "customer-main")
public class CustomerFormFrontlet {

    @FormData("customer")
    public CustomerForm customer() {
        return new CustomerForm("", "");
    }

    @Action
    public FrontletResponse save(@FormData("customer") CustomerForm customer) {
        return new FrontletResponse(CustomerDetailsFrontlet.class)
                .frontletParameter("name", customer.name())
                .frontletParameter("email", customer.email());
    }

    public record CustomerForm(
            @Mandatory @LabelKey("customer.name") String name,
            @Mandatory @EMail @LabelKey("customer.email") String email
    ) {
    }
}
```

`src/main/java/example/customer/CustomerFormFrontlet.html`

```html
<xis:template xmlns:xis="https://xis.one/xsd">
    <form xis:binding="customer">
        <xis:global-messages/>

        <label for="name" xis:error-binding="name" xis:error-style="color: #b00020">Name</label>
        <input id="name" xis:binding="name" xis:error-class="error"/>
        <div xis:message-for="name"></div>

        <label for="email" xis:error-binding="email" xis:error-style="color: #b00020">Email</label>
        <input id="email" type="email" xis:binding="email" xis:error-class="error"/>
        <div xis:message-for="email"></div>

        <button type="submit" xis:action="save">Save</button>
    </form>
</xis:template>
```

`src/main/java/example/customer/CustomerDetailsFrontlet.java`

```java
package example.customer;

import one.xis.Frontlet;
import one.xis.Parameter;
import one.xis.ModelData;

@Frontlet(containerId = "customer-main")
public class CustomerDetailsFrontlet {

    @ModelData("summary")
    public String summary(@Parameter("name") String name,
                          @Parameter("email") String email) {
        return name + " <" + email + ">";
    }
}
```

`src/main/java/example/customer/CustomerDetailsFrontlet.html`

```html
<xis:template xmlns:xis="https://xis.one/xsd">
    <h2>Saved customer</h2>
    <p>${summary}</p>
</xis:template>
```

The `save` action runs in the form frontlet. Returning `FrontletResponse` replaces the frontlet in the current
container with `CustomerDetailsFrontlet` and passes the submitted values as frontlet parameters.

Frontlets also become useful when an application grows beyond one deployment unit. Pages and frontlets can be served by
different XIS applications, so teams can split a shell page and selected UI fragments across runtimes. That is an
advanced setup; continue with [Microfrontend Architecture](advanced/microfrontend-architecture.md) when you need it.

## Add Authentication

Add authentication when pages or actions should require a login:

```groovy
dependencies {
    implementation "one.xis:xis-spring"
    implementation "one.xis:xis-authentication"
}
```

Provide a `UserInfoService`. The smallest local setup can keep users in memory:

```java
package example.security;

import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;
import one.xis.context.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class DemoUserInfoService implements UserInfoService {

    @Override
    public Optional<UserInfo> getUserInfo(String userId) {
        if (!"admin".equals(userId)) {
            return Optional.empty();
        }
        var user = new UserInfoImpl();
        user.setUserId("admin");
        user.setPassword("admin");
        user.setRoles(Set.of("ADMIN"));
        return Optional.of(user);
    }

    @Override
    public boolean supportsLocalLogin() {
        return true;
    }
}
```

Protect pages with `@Authenticated` when a login is enough and with `@Roles` when named roles are required:

```java
package example.admin;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;

@Page("/admin.html")
@Roles({"ADMIN", "SUPPORT"})
public class AdminPage {

    @ModelData("status")
    public String status() {
        return "Admin/support area";
    }

    @Action
    @Roles("ADMIN")
    public void rebuildIndex() {
        // call application service here
    }
}
```

The template can also hide controls that do not match the current user's roles:

```html
<h1>${status}</h1>
<p xis:if="isUserInRoles('ADMIN', 'SUPPORT')">Restricted tools are available.</p>
<button xis:if="isUserInRole('ADMIN')" xis:action="rebuildIndex">Rebuild index</button>
```

`isUserInRole(...)` and `isUserInRoles(...)` run in the browser. They are useful for a cleaner UI, but they are not
security. Keep `@Roles` or `@Authenticated` on the Java controller or action that must be protected.
