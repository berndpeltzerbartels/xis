# Quickstart

[Documentation map](../../README.md)

This quickstart creates a small XIS application with Spring Boot. The standalone XIS Boot runtime follows the same page,
template, and action model; only the application entry point and dependencies differ.

## Prerequisites

- Java 17 or newer
- Gradle 8 or newer

Java is the shortest path through this quickstart. XIS also supports Groovy 4+ and Kotlin controllers and form DTOs; see
[Groovy support](groovy.md) and [Kotlin support](kotlin.md) when you want to write the server-side code in another JVM
language.

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
    id "one.xis.plugin" version "0.19.0"
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
class Application {

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

Generated tests use `@XisBootTest`, register the page controller in the XIS test context, and open the page through its
URL. The required XIS test starter is added automatically by the plugin. Generated tests compile before the full page behavior is
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
class DashboardPage {

    @ModelData
    String title() {
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
test is intentionally small: it uses `@XisBootTest`, registers the page controller in the test context, opens
`/index.html`, and leaves space for assertions against the rendered document.

## Add an Action

`src/main/java/example/dashboard/CounterPage.java`

```java
package example.dashboard;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

@Page("/counter.html")
class CounterPage {

    private int count;

    @ModelData
    int count() {
        return count;
    }

    @Action
    void increment() {
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
    <p>Current count: <span id="count">${count}</span></p>
    <button id="increment" xis:action="increment">Increment</button>
</body>
</html>
```

The action is invoked through XIS. You do not create a REST endpoint for it.

You can generate the test file with `./gradlew xisTests`; you do not have to create the boilerplate yourself. The
generated file is intentionally small, so make the behavior explicit by adding the click and assertions:

`src/test/java/example/dashboard/CounterPageTest.java`

```java
package example.dashboard;

import one.xis.boot.test.XisBootTest;
import one.xis.context.IntegrationTestContext;
import one.xis.test.InTestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@XisBootTest
class CounterPageTest {

    private IntegrationTestContext context;

    @InTestContext
    private CounterPage counterPage;

    @Test
    void incrementUpdatesCounter() {
        var client = context.openPage("/counter.html");
        var document = client.getDocument();

        assertEquals("0", document.getElementById("count").getInnerText());

        document.getElementById("increment").click();

        assertEquals("1", document.getElementById("count").getInnerText());
    }
}
```

## Add a Form With Validation

Forms bind HTML controls to a Java object. XIS deserializes the submitted values, validates annotations, and calls the
action only when validation succeeds.

`src/main/java/example/customer/CustomerForm.java`

```java
package example.customer;

import one.xis.validation.EMail;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;

record CustomerForm(@Mandatory @LabelKey("customer.name") String name,
        @Mandatory @EMail @LabelKey("customer.email") String email) {
}
```

`src/main/java/example/customer/CustomerNewPage.java`

```java
package example.customer;

import one.xis.Action;
import one.xis.FormData;
import one.xis.HtmlFile;
import one.xis.Page;

@Page("/customers/new.html")
@HtmlFile("CustomerFormPage.html")
class CustomerNewPage {

    @FormData("customer")
    CustomerForm customer() {
        return new CustomerForm("", "");
    }

    @Action
    void save(@FormData("customer") CustomerForm customer) {
        // Store the customer in your service or repository.
    }
}
```

`src/main/java/example/customer/CustomerFormPage.html`

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

## Add SQL

Update the dependencies:

`build.gradle`

```groovy
dependencies {
    implementation "one.xis:xis-spring"
    implementation "one.xis:xis-sql"
    runtimeOnly "org.postgresql:postgresql:42.7.11"
}
```

Create a local PostgreSQL database:

```bash
createdb xis_quickstart
```

Point the application at that database:

`src/main/resources/application.properties`

```properties
xis.sql.url=jdbc:postgresql://localhost:5432/xis_quickstart
xis.sql.driver-class-name=org.postgresql.Driver
```

If your local PostgreSQL needs explicit credentials, also add `xis.sql.user` and `xis.sql.password`.

Add a DDL change set for the customer table. XIS discovers change-set classes at startup, runs missing changes against
the configured `DataSource`, and records them in `__xis_schema_change`, so the table is created without a separate SQL
script:

`src/main/java/example/customer/CustomerSchema.java`

```java
package example.customer;

import one.xis.ddl.Change;
import one.xis.ddl.ChangeSet;
import one.xis.ddl.DDL;

@ChangeSet("customer-schema")
class CustomerSchema {

    @Change("001-create-customers")
    void createCustomers(DDL ddl) {
        var customers = ddl.createTableIfNotExists("customers");
        customers.addColumn("id").bigint().generatedIdentity().primaryKey();
        customers.addColumn("name").varchar(200).notNull();
        customers.addColumn("email").varchar(200).notNull();
    }
}
```

Add the SQL entity, repository, and service:

`src/main/java/example/customer/Customer.java`

```java
package example.customer;

import one.xis.sql.Entity;

@Entity("customers")
class Customer {
    Long id;
    String name;
    String email;
}
```

`src/main/java/example/customer/CustomerRepository.java`

```java
package example.customer;

import one.xis.sql.CrudRepository;
import one.xis.sql.Repository;

@Repository
interface CustomerRepository extends CrudRepository<Customer, Long> {
}
```

`src/main/java/example/customer/CustomerService.java`

```java
package example.customer;

import java.util.List;

interface CustomerService {

    List<Customer> findAll();

    CustomerForm newForm();

    CustomerForm editForm(long id);

    void save(Long id, CustomerForm form);
}
```

`src/main/java/example/customer/CustomerServiceImpl.java`

```java
package example.customer;

import one.xis.context.Component;

import java.util.List;

@Component
class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customers;

    CustomerServiceImpl(CustomerRepository customers) {
        this.customers = customers;
    }

    @Override
    public List<Customer> findAll() {
        return customers.findAll();
    }

    @Override
    public CustomerForm newForm() {
        return new CustomerForm("", "");
    }

    @Override
    public CustomerForm editForm(long id) {
        var customer = customers.findById(id).orElseThrow();
        return new CustomerForm(customer.name, customer.email);
    }

    @Override
    public void save(Long id, CustomerForm form) {
        var customer = new Customer();
        customer.id = id;
        customer.name = form.name();
        customer.email = form.email();
        customers.save(customer);
    }
}
```

Now let the form page save through the service and navigate back to the customer list:

`src/main/java/example/customer/CustomerNewPage.java`

```java
package example.customer;

import one.xis.Action;
import one.xis.FormData;
import one.xis.HtmlFile;
import one.xis.Page;

@Page("/customers/new.html")
@HtmlFile("CustomerFormPage.html")
class CustomerNewPage {

    private final CustomerService customerService;

    CustomerNewPage(CustomerService customerService) {
        this.customerService = customerService;
    }

    @FormData("customer")
    CustomerForm customer() {
        return customerService.newForm();
    }

    @Action
    Class<?> save(@FormData("customer") CustomerForm customer) {
        customerService.save(null, customer);
        return CustomerListPage.class;
    }
}
```

## Add A Customer List

Add a list page and link it to the form page.

`src/main/java/example/customer/CustomerListPage.java`

```java
package example.customer;

import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/customers.html")
class CustomerListPage {

    private final CustomerService customerService;

    CustomerListPage(CustomerService customerService) {
        this.customerService = customerService;
    }

    @ModelData
    List<Customer> customers() {
        return customerService.findAll();
    }
}
```

`src/main/java/example/customer/CustomerListPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Customers</title>
</head>
<body>
    <h1>Customers</h1>

    <p>
        <a xis:page="/customers/new.html">Add customer</a>
    </p>

    <table>
        <thead>
        <tr>
            <th>Name</th>
            <th>Email</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <tr xis:repeat="customer:${customers}">
            <td>${customer.name}</td>
            <td>${customer.email}</td>
            <td>
                <a xis:page="/customers/${customer.id}/edit.html">Edit</a>
            </td>
        </tr>
        </tbody>
    </table>

    <p xis:if="${empty(customers)}">No customers yet.</p>
</body>
</html>
```

## Add Edit

Use a second page controller for editing, but keep the same `CustomerFormPage.html` template. The page URL carries the
customer id as a path variable.

`src/main/java/example/customer/CustomerEditPage.java`

```java
package example.customer;

import one.xis.Action;
import one.xis.FormData;
import one.xis.HtmlFile;
import one.xis.Page;
import one.xis.PathVariable;

@Page("/customers/{id}/edit.html")
@HtmlFile("CustomerFormPage.html")
class CustomerEditPage {

    private final CustomerService customerService;

    CustomerEditPage(CustomerService customerService) {
        this.customerService = customerService;
    }

    @FormData("customer")
    CustomerForm customer(@PathVariable("id") long id) {
        return customerService.editForm(id);
    }

    @Action
    Class<?> save(@PathVariable("id") long id, @FormData("customer") CustomerForm customer) {
        customerService.save(id, customer);
        return CustomerListPage.class;
    }
}
```

The HTML template does not change. The existing customer id comes from the page URL, not from a hidden form field.

## Move The Customer UI Into Frontlets

The customer pages now work end to end: list, new, edit, validation, and SQL persistence. When a part of the UI should
be modular or replaceable on its own, move it into frontlets. A frontlet has its own controller and template fragment,
and XIS can reload or replace just that fragment instead of rebuilding the whole page.

`src/main/java/example/customer/CustomerFrontletPage.java`

```java
package example.customer;

import one.xis.Page;

@Page("/customer-frontlet.html")
class CustomerFrontletPage {
}
```

`src/main/java/example/customer/CustomerFrontletPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Customers</title>
</head>
<body>
    <h1>Customers</h1>
    <main xis:frontlet-container="customer-main"
          xis:default-frontlet="CustomerListFrontlet"></main>
</body>
</html>
```

`src/main/java/example/customer/CustomerListFrontlet.java`

```java
package example.customer;

import one.xis.Frontlet;
import one.xis.ModelData;

import java.util.List;

@Frontlet(containerId = "customer-main")
class CustomerListFrontlet {

    private final CustomerService customerService;

    CustomerListFrontlet(CustomerService customerService) {
        this.customerService = customerService;
    }

    @ModelData
    List<Customer> customers() {
        return customerService.findAll();
    }
}
```

`src/main/java/example/customer/CustomerListFrontlet.html`

```html
<xis:template xmlns:xis="https://xis.one/xsd">
    <p>
        <button xis:frontlet="CustomerFormFrontlet"
                xis:target-container="customer-main">Add customer</button>
    </p>

    <table>
        <thead>
        <tr>
            <th>Name</th>
            <th>Email</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <tr xis:repeat="customer:${customers}">
            <td>${customer.name}</td>
            <td>${customer.email}</td>
            <td>
                <a xis:frontlet="CustomerFormFrontlet"
                   xis:target-container="customer-main">
                    <xis:parameter name="customerId" value="${customer.id}"/>
                    Edit
                </a>
            </td>
        </tr>
        </tbody>
    </table>

    <p xis:if="${empty(customers)}">No customers yet.</p>
</xis:template>
```

`src/main/java/example/customer/CustomerFormFrontlet.java`

```java
package example.customer;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.NullAllowed;

@Frontlet(containerId = "customer-main")
class CustomerFormFrontlet {

    private final CustomerService customerService;

    CustomerFormFrontlet(CustomerService customerService) {
        this.customerService = customerService;
    }

    @FormData("customer")
    CustomerForm customer(@FrontletParameter("customerId") @NullAllowed Long customerId) {
        return customerId == null ? customerService.newForm() : customerService.editForm(customerId);
    }

    @Action
    Class<?> save(@FrontletParameter("customerId") @NullAllowed Long customerId,
            @FormData("customer") CustomerForm customer) {
        customerService.save(customerId, customer);
        return CustomerListFrontlet.class;
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
        <button type="button"
                xis:frontlet="CustomerListFrontlet"
                xis:target-container="customer-main">Cancel</button>
    </form>
</xis:template>
```

After a successful save, the action returns `CustomerListFrontlet.class`, so the list replaces the form in the same
container. If validation fails, the action is not called and the form stays visible with validation messages.

Frontlets can read `@PathVariable` values from the current page URL, even when the frontlet itself has no URL. Use
`@FrontletParameter` for values that belong to a specific frontlet instance, such as the `customerId` passed from the
list to the form above. The frontlet action can read the same parameter, so the edit id does not need a hidden form
field.

Frontlets also become useful when an application grows beyond one deployment unit. Pages and frontlets can be served by
different XIS applications, so teams can split a shell page and selected UI fragments across runtimes. That is an
advanced setup; continue with [Microfrontend Architecture](advanced/microfrontend-architecture.md) when you need it.

## Add Authentication

Add authentication when the customer screen should require a login:

```groovy
dependencies {
    implementation "one.xis:xis-spring"
    implementation "one.xis:xis-authentication"
    implementation "one.xis:xis-local-credentials-sql"
    runtimeOnly "org.postgresql:postgresql:42.7.11"
}
```

Provide a `UserAccountService` for the account and roles. `xis-local-credentials-sql` brings the local credential
service and `xis-sql` transitively, validates the password, and stores the password hash in SQL:

```java
package example.security;

import one.xis.auth.UserAccount;
import one.xis.auth.UserAccountImpl;
import one.xis.auth.UserAccountService;
import one.xis.context.Component;

import java.util.Optional;
import java.util.Set;

@Component
class DemoUserAccountService implements UserAccountService<UserAccount> {

    @Override
    public Optional<UserAccount> getUserAccount(String userId) {
        if (!"admin".equals(userId)) {
            return Optional.empty();
        }
        var user = new UserAccountImpl();
        user.setUserId("admin");
        user.setRoles(Set.of("ADMIN"));
        return Optional.of(user);
    }

    @Override
    public void saveUserAccount(UserAccount userAccount) {
        // Store account profile data here if the application needs it.
    }
}
```

Protect the customer page with `@Authenticated` when a login is enough:

```java
package example.customer;

import one.xis.Page;
import one.xis.Authenticated;

@Authenticated
@Page("/customer-frontlet.html")
class CustomerFrontletPage {
}
```

Use `@Roles` when named roles are required. For example, keep the list visible to every logged-in user but restrict
saving to admins:

```java
package example.customer;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.NullAllowed;
import one.xis.Roles;

@Frontlet(containerId = "customer-main")
class CustomerFormFrontlet {

    private final CustomerService customerService;

    CustomerFormFrontlet(CustomerService customerService) {
        this.customerService = customerService;
    }

    @FormData("customer")
    CustomerForm customer(@FrontletParameter("customerId") @NullAllowed Long customerId) {
        return customerId == null ? customerService.newForm() : customerService.editForm(customerId);
    }

    @Action
    @Roles("ADMIN")
    Class<?> save(@FrontletParameter("customerId") @NullAllowed Long customerId,
            @FormData("customer") CustomerForm customer) {
        customerService.save(customerId, customer);
        return CustomerListFrontlet.class;
    }
}
```

The list template can also hide controls that do not match the current user's roles:

```html
<button xis:if="isUserInRole('ADMIN')"
        xis:frontlet="CustomerFormFrontlet"
        xis:target-container="customer-main">Add customer</button>
```

`isUserInRole(...)` and `isUserInRoles(...)` run in the browser. They are useful for a cleaner UI, but they are not
security. Keep `@Roles` or `@Authenticated` on the Java controller or action that must be protected.
