# XIS

**XIS is a lightweight SPA framework for Java, Groovy, and Kotlin: annotation-driven, fast, and built around backend controllers
plus plain HTML templates. XIS provides ready-to-use client-server transport, state synchronization, navigation, form
handling, validation feedback, and partial UI refreshes without extra work and without any boilerplate code. It runs
standalone on the JVM with XIS Boot, as a GraalVM native executable for cloud-native deployment with XIS Boot Native,
or inside Spring. It supports distributed Microfrontend Architecture, and in most applications you write just annotated
backend controllers and templates. Coding is as simple as in the old days of request-response, but the result is a modern and
fast SPA application. XIS Boot Native supports Java and Kotlin native executables; Groovy is supported on the JVM path.**

The framework keeps application code close to the feature it implements. Instead of maintaining REST endpoints, DTOs,
frontend API clients, client-side state management, and duplicated validation rules, most features are built from a
server-side controller and the HTML template that presents it.

The programming model is server-driven: Java, Groovy, or Kotlin controllers describe pages, frontlets, actions, form data, and model data.
HTML templates describe the UI. XIS handles browser-server communication, navigation, form submission, validation
feedback, frontlet updates, and SPA-style page transitions.

## Why XIS Exists

Many web applications end up with the same split:

- Java, Groovy, or Kotlin backend controllers
- REST endpoints and DTOs
- JavaScript or TypeScript API clients
- frontend state management
- duplicated validation and navigation rules

XIS removes most of that coordination layer. You build a feature as a vertical slice: one Java controller and one HTML
template can define a working interactive page.

That vertical-slice model also fits Microfrontend Architecture. A service can own not only its business logic, but also
the page or frontlet that presents that logic, while XIS composes those pieces into one browser application.

The Gradle plugin adds comfortable tools for that workflow: it can generate starter templates and starter integration
tests, configure the matching XIS test starter automatically, run XIS-specific validation checks for local development
or Jenkins pipelines, and build or run standalone XIS Boot applications.

Optional persistence modules add the same lightweight style to data access: `xis-sql` provides JDBC repository support
with transactions, and `xis-mongodb` provides MongoDB document repositories plus change-stream callbacks for
event-driven UI refreshes.

## Minimal Page

`src/main/java/example/HelloPage.java`

```java
package example;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.WelcomePage;

@WelcomePage
@Page("/hello.html")
class HelloPage {

    private int counter;

    @ModelData
    String greeting() {
        return "Hello XIS";
    }

    @ModelData
    int count() {
        return counter;
    }

    @Action
    void increment() {
        counter++;
    }
}
```

`src/main/java/example/HelloPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hello XIS</title>
</head>
<body>
<h1>${greeting}</h1>
<p>Count: ${count}</p>
<button xis:action="increment">Increment</button>
</body>
</html>
```

The button calls the Java `increment` action. XIS transports the interaction and refreshes the rendered state without
requiring a REST endpoint or a handwritten frontend client.

## Runtime Choices

Most applications should choose one runtime:

- `xis-spring` for Spring Boot applications
- `xis-boot` for standalone XIS applications without Spring Boot
- `xis-boot-native` for XIS Boot applications that should be built and deployed as GraalVM native executables

The selected runtime brings the controller annotations such as `@Page`, `@Frontlet`, `@ModelData`, `@FormData`,
`@Action`, and `@PathVariable`.

`xis-boot-native` is the cloud-native path for small containers, fast startup, and deployments where you want a native
binary without building your own discovery, reflection, resource, and proxy infrastructure.

XIS also supports Groovy 4+ and Kotlin controllers and form DTOs. This is useful when a team wants a lighter JVM syntax
while still using the same XIS annotations, templates, forms, validation, and runtime behavior. Java and Kotlin are
supported by XIS Boot Native; Groovy is supported on the JVM path.

## Feature Index

- [Pages, model data, actions, shared values, frontlets, frontlet containers, and includes](docs/user/core-model.md)
- [Template expressions, property/index access, operators, conditions, iteration, built-in EL functions, and formatting helpers](docs/user/templates.md)
- [XIS tags and attributes for navigation, actions, drag and drop, modals, frontlets, forms, validation messages, dynamic classes, client state, and raw content](docs/user/tags-and-attributes.md)
- [Navigation responses for pages, frontlets, modals, deep links, action buttons, action links, and form actions](docs/user/navigation.md)
- [Routers for server-side navigation decisions before a page, frontlet, or modal is selected](docs/user/routers.md)
- [Forms, validation, nested objects, lists, type conversion, custom formatters, validation highlighting, and validation messages](docs/user/forms-and-validation.md)
- [Modal dialogs with their own controller, template, model data, form data, actions, validation, and parent reload behavior](docs/user/modals.md)
- [Drag and drop actions with integration-test support](docs/user/drag-and-drop.md)
- [Refresh and push events for already open pages and frontlets, including targeted client updates](docs/user/events.md)
- [Client state with localStorage, sessionStorage, clientState, and storage bindings](docs/user/annotations.md#browser-storage-parameters)
- [SQL repositories, entity mapping, relations, CRUD, explicit SQL, transactions, save/delete cascades, functions, and stored procedures](docs/user/sql.md)
- [MongoDB document repositories, JSON queries, document mapping, and change streams](docs/user/mongodb.md)
- [Security with local authentication, roles, template visibility, external OpenID Connect providers, XIS as IDP, and distributed SSO](docs/user/security.md)
- [Microfrontend Architecture for distributed XIS applications](docs/user/advanced/microfrontend-architecture.md)
- [Groovy 4+ support for controllers and forms](docs/user/groovy.md)
- [Kotlin support for controllers, forms, and native executables](docs/user/kotlin.md)
- [Gradle tools for template generation, test generation, validation, runnable jars, and local runs](docs/user/gradle-plugin.md)
- [Cloud Native deployment with XIS Boot Native, GraalVM native-image, and native database modules](docs/user/cloud-native.md)
- [Integration tests, generated tests, E2E tests, and the integration-test browser model](docs/user/examples-and-tests.md)
- [Custom JavaScript extensions, custom EL functions, global browser behavior, and form submission from JavaScript](docs/user/advanced/custom-javascript.md)
- [Custom proxies for generated clients, repositories, and infrastructure extensions](docs/user/advanced/custom-proxies.md)
- [XIS Theme for generated standard pages, forms, navigation, dashboards, panels, messages, and basic layout](docs/user/advanced/theme.md)

## Documentation

This README is the canonical documentation start page for users, framework developers, and coding agents. It includes the
working map directly so repository text search finds the public feature surface, annotations, template tags, persistence
annotations, security hooks, native-image topics, and Gradle tasks from one entry point.

## User Documentation

Start here if you want quick results:

- [Quickstart](docs/user/quickstart.md)

Read these to build normal XIS applications:

- [Runtime and dependency model](docs/user/runtime-and-dependencies.md)
- [XIS Boot Native and cloud-native native-image builds](docs/user/runtime-and-dependencies.md#xis-boot-native)
- [Cloud Native and native images](docs/user/cloud-native.md)
- [Gradle plugin and tools](docs/user/gradle-plugin.md)
- [Groovy support](docs/user/groovy.md)
- [Kotlin support](docs/user/kotlin.md)
- [Template location and mapping](docs/user/template-location-and-mapping.md)
- [Core model](docs/user/core-model.md)
- [Annotation reference](docs/user/annotations.md)
- [Template syntax](docs/user/templates.md)
- [Tags and attributes](docs/user/tags-and-attributes.md)
- [Request lifecycle](docs/user/request-lifecycle.md)
- [Navigation and responses](docs/user/navigation.md)
- [Forms and validation](docs/user/forms-and-validation.md)
- [Modals](docs/user/modals.md)
- [Drag and drop](docs/user/drag-and-drop.md)
- [Events](docs/user/events.md)
- [Scheduled jobs](docs/user/scheduled-jobs.md)
- [Security](docs/user/security.md)
- [Examples and tests](docs/user/examples-and-tests.md)

## Advanced Topics

Optional or specialized capabilities are still important enough to be visible:

- [Microfrontend Architecture and distributed mode](docs/user/advanced/microfrontend-architecture.md)
- [Reusable web artifacts with replaceable templates](docs/user/advanced/reusable-web-artifacts.md)
- [Aspects and interface advice](docs/user/advanced/aspects.md), including the native-image-friendly AOP model
- [Custom proxies](docs/user/advanced/custom-proxies.md)
- [Explicit SQL transactions](docs/user/advanced/sql-transactions.md)
- [Custom JavaScript and custom EL functions](docs/user/advanced/custom-javascript.md)
- [Integration-test browser model](docs/user/advanced/integration-test-browser.md)
- [XIS theme](docs/user/advanced/theme.md)

User documentation should be organized around public API behavior and copyable examples, not around repository modules.

## Working Map

Use this map while building or reviewing an application. The root README shows the feature surface; this map is more
complete and intentionally detailed.

### Application Structure

- [Runtime choices and dependencies](docs/user/runtime-and-dependencies.md)
- [Cloud Native and native images](docs/user/cloud-native.md)
- [XIS Boot applications with `@XISBootApplication`](docs/user/annotations.md#class-annotations)
- [Spring Boot integration](docs/user/runtime-and-dependencies.md#runtime-choice)
- [Groovy 4+ controllers and forms](docs/user/groovy.md)
- [Kotlin controllers, forms, and native executables](docs/user/kotlin.md)
- [Template location, `@HtmlFile`, `@DefaultHtmlFile`, generated templates, generated tests](docs/user/template-location-and-mapping.md)
- [Static resources](docs/user/runtime-and-dependencies.md#static-resources)
- [Reusable web artifacts with replaceable templates](docs/user/advanced/reusable-web-artifacts.md)
- [Microfrontend Architecture and distributed mode](docs/user/advanced/microfrontend-architecture.md)

### Pages, Components, And UI Composition

- [Pages with `@Page` and `@WelcomePage`](docs/user/core-model.md#pages)
- [Frontlets with `@Frontlet`](docs/user/core-model.md#frontlets)
- [Frontlet containers](docs/user/core-model.md#frontlet-containers)
- [Includes with `@Include`](docs/user/core-model.md#includes)
- [Modals with `@Modal`](docs/user/modals.md)
- [Routers with `@Router` and `@Route`](docs/user/routers.md)
- [Page and frontlet titles with `@Title`](docs/user/annotations.md#method-annotations)

### Controller Data And Actions

- [Model data with `@ModelData`](docs/user/core-model.md#model-data)
- [Form data with `@FormData`](docs/user/forms-and-validation.md#basic-form-binding)
- [Actions with `@Action`](docs/user/core-model.md#actions)
- [Action results as model data with `@Action` plus `@ModelData`](docs/user/annotations.md#action-results-as-model-data)
- [Shared values with `@SharedValue`](docs/user/annotations.md#shared-values)
- [Path variables with `@PathVariable`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [Query parameters with `@QueryParameter`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [Action parameters with `@ActionParameter`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [Frontlet parameters with `@FrontletParameter`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [Modal parameters with `@ModalParameter`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [Client id with `@ClientId`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [User id with `@UserId`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [Current user context with `UserContext`](docs/user/annotations.md#parameter-field-and-record-component-annotations)

### Navigation And Responses

- [Page links and `xis:page`](docs/user/navigation.md#page-links)
- [Action links, buttons, and `xis:action`](docs/user/navigation.md#action-links-and-buttons)
- [Form actions and validation-aware submit behavior](docs/user/navigation.md#form-actions)
- [Returning page classes](docs/user/navigation.md#return-a-page-class)
- [Returning `PageResponse`](docs/user/navigation.md#return-pageresponse)
- [Returning `PageUrlResponse`](docs/user/navigation.md#return-pageurlresponse)
- [Frontlet navigation and `FrontletResponse`](docs/user/navigation.md#frontlet-navigation)
- [Returning `ModalResponse`](docs/user/navigation.md#return-modalresponse)
- [Deep linking](docs/user/navigation.md#deep-linking)
- [`void` actions and automatic refresh](docs/user/navigation.md#void-actions)

### Templates, Tags, And Attributes

- [Expression language](docs/user/templates.md#expression-language)
- [Property and index access](docs/user/templates.md#property-and-index-access)
- [Operators and conditions](docs/user/templates.md#operators-and-conditions)
- [Built-in EL functions](docs/user/templates.md#built-in-el-functions)
- [Formatting helpers such as `formatDate`, `formatDateTime`, and `formatTime`](docs/user/templates.md#built-in-el-functions)
- [Iteration with `xis:foreach`, `xis:repeat`, and `<xis:foreach>`](docs/user/templates.md#iteration)
- [Conditions with `xis:if` and `<xis:if>`](docs/user/templates.md#conditions)
- [XIS navigation tags and attributes](docs/user/tags-and-attributes.md#page-navigation)
- [XIS action tags and attributes](docs/user/tags-and-attributes.md#actions)
- [Drag and drop attributes `xis:drag` and `xis:drop`](docs/user/tags-and-attributes.md#drag-and-drop-actions)
- [Modal tag and attribute syntax](docs/user/tags-and-attributes.md#modals)
- [Frontlet tag and attribute syntax](docs/user/tags-and-attributes.md#frontlets)
- [Includes](docs/user/tags-and-attributes.md#includes)
- [Forms](docs/user/tags-and-attributes.md#forms)
- [Validation message tags and attributes](docs/user/tags-and-attributes.md#validation-messages)
- [Error highlighting with `xis:error-class` and `xis:error-style`](docs/user/tags-and-attributes.md#validation-messages)
- [Dynamic classes with `xis:class`](docs/user/tags-and-attributes.md#dynamic-classes)
- [Client state and storage bindings](docs/user/tags-and-attributes.md#client-state)
- [Raw trusted content with `xis:raw`](docs/user/tags-and-attributes.md#raw-content)

### Template Attribute Index

- [`xis:src`](docs/user/tags-and-attributes.md#tags-and-attributes)
- [`xis:if`](docs/user/tags-and-attributes.md#conditions)
- [`xis:foreach`](docs/user/tags-and-attributes.md#iteration)
- [`xis:repeat`](docs/user/tags-and-attributes.md#iteration)
- [`xis:page`](docs/user/tags-and-attributes.md#page-navigation)
- [`xis:action`](docs/user/tags-and-attributes.md#actions)
- [`xis:drag`](docs/user/tags-and-attributes.md#drag-and-drop-actions)
- [`xis:drop`](docs/user/tags-and-attributes.md#drag-and-drop-actions)
- [`xis:modal`](docs/user/tags-and-attributes.md#modals)
- [`xis:frontlet-container`](docs/user/tags-and-attributes.md#frontlets)
- [`xis:default-frontlet`](docs/user/tags-and-attributes.md#frontlets)
- [`xis:scroll-to-top`](docs/user/tags-and-attributes.md#frontlets)
- [`xis:frontlet`](docs/user/tags-and-attributes.md#frontlets)
- [`xis:target-container`](docs/user/tags-and-attributes.md#frontlets)
- [`xis:include`](docs/user/tags-and-attributes.md#includes)
- [`xis:binding`](docs/user/tags-and-attributes.md#forms)
- [`xis:message-for`](docs/user/tags-and-attributes.md#validation-messages)
- [`xis:error-class`](docs/user/tags-and-attributes.md#validation-messages)
- [`xis:error-binding`](docs/user/tags-and-attributes.md#validation-messages)
- [`xis:error-style`](docs/user/tags-and-attributes.md#validation-messages)
- [`xis:selection-group`](docs/user/tags-and-attributes.md#dynamic-classes)
- [`selection-group`](docs/user/tags-and-attributes.md#dynamic-classes)
- [`xis:selection-class`](docs/user/tags-and-attributes.md#dynamic-classes)
- [`selection-class`](docs/user/tags-and-attributes.md#dynamic-classes)
- [`xis:storage-binding`](docs/user/tags-and-attributes.md#client-state)

### Template Tag Index

- [`<xis:if>`](docs/user/tags-and-attributes.md#conditions)
- [`<xis:foreach>`](docs/user/tags-and-attributes.md#iteration)
- [`<xis:a>`](docs/user/tags-and-attributes.md#page-navigation)
- [`<xis:button>`](docs/user/tags-and-attributes.md#page-navigation)
- [`<xis:action>`](docs/user/tags-and-attributes.md#actions)
- [`<xis:parameter>`](docs/user/tags-and-attributes.md#actions)
- [`<xis:frontlet-container>`](docs/user/tags-and-attributes.md#frontlets)
- [`<xis:frontlet>`](docs/user/tags-and-attributes.md#frontlets)
- [`<xis:include>`](docs/user/tags-and-attributes.md#includes)
- [`<xis:form>`](docs/user/tags-and-attributes.md#forms)
- [`<xis:input>`](docs/user/tags-and-attributes.md#forms)
- [`<xis:textarea>`](docs/user/tags-and-attributes.md#forms)
- [`<xis:select>`](docs/user/tags-and-attributes.md#forms)
- [`<xis:checkbox>`](docs/user/tags-and-attributes.md#forms)
- [`<xis:radio>`](docs/user/tags-and-attributes.md#forms)
- [`<xis:submit>`](docs/user/tags-and-attributes.md#forms)
- [`<xis:message>`](docs/user/tags-and-attributes.md#validation-messages)
- [`<xis:global-messages>`](docs/user/tags-and-attributes.md#validation-messages)
- [`<xis:storage-binding>`](docs/user/tags-and-attributes.md#client-state)
- [`<xis:raw>`](docs/user/tags-and-attributes.md#raw-content)

### Forms, Validation, And Formatting

- [Basic form binding](docs/user/forms-and-validation.md#basic-form-binding)
- [Multiple submit actions](docs/user/forms-and-validation.md#multiple-submit-actions)
- [Nested objects and lists](docs/user/forms-and-validation.md#nested-objects-and-lists)
- [File uploads with `@Upload`](docs/user/forms-and-validation.md#file-uploads)
- [Type conversion](docs/user/forms-and-validation.md#type-conversion)
- [Custom formatters with `@UseFormatter`](docs/user/forms-and-validation.md#custom-formatters)
- [Validation annotations `@Mandatory`, `@AllElementsMandatory`, `@EMail`, `@MinLength`, `@RegExpr`, `@Validate`, `@LabelKey`, `@NullAllowed`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [Validation messages, global messages, and field messages](docs/user/forms-and-validation.md)
- [Validation highlighting](docs/user/tags-and-attributes.md#validation-messages)

### Events, Push, And Client Refresh

- [Refresh events](docs/user/events.md)
- [Server-triggered refresh events](docs/user/request-lifecycle.md#server-triggered-refresh-events)
- [`@RefreshOnUpdateEvents`](docs/user/annotations.md#class-annotations)
- [`RefreshEventPublisher`](docs/user/events.md)
- [Publishing to all clients](docs/user/events.md)
- [Publishing to one client](docs/user/events.md)
- [Visible page and frontlet refresh behavior](docs/user/events.md)
- [Scheduled jobs with `@Scheduled`](docs/user/scheduled-jobs.md)

### Client State

- [`@LocalStorage`](docs/user/annotations.md#browser-storage-parameters)
- [`@SessionStorage`](docs/user/annotations.md#browser-storage-parameters)
- [`@ClientState`](docs/user/annotations.md#browser-storage-parameters)
- [Storage bindings in templates](docs/user/tags-and-attributes.md#client-state)
- [Integration-test browser storage](docs/user/advanced/integration-test-browser.md#storage)

### Persistence

- [SQL overview](docs/user/sql.md)
- [SQL `@Entity`](docs/user/sql.md#entity-mapping)
- [SQL `@Column`](docs/user/sql.md#entity-mapping)
- [SQL `@Ignore`](docs/user/sql.md#entity-mapping)
- [SQL `@JsonColumn`](docs/user/sql.md#entity-mapping)
- [SQL `@NoColumn`](docs/user/sql.md#entity-mapping)
- [SQL `@OptionalColumn`](docs/user/sql.md#entity-mapping)
- [SQL `@Repository`](docs/user/sql.md#repositories)
- [SQL `@Param`](docs/user/sql.md#select)
- [SQL `@Select`](docs/user/sql.md#select)
- [SQL `@Insert`](docs/user/sql.md#insert-and-update)
- [SQL `@Update`](docs/user/sql.md#insert-and-update)
- [SQL `@Save`](docs/user/sql.md#save)
- [SQL `@Delete`](docs/user/sql.md#delete)
- [SQL `@Transactional`](docs/user/sql.md#transactions)
- [SQL `@Function`](docs/user/sql.md#functions-and-stored-procedures)
- [SQL `@StoredProcedure`](docs/user/sql.md#functions-and-stored-procedures)
- [MongoDB overview](docs/user/mongodb.md)
- [MongoDB `@MongoDocument`](docs/user/mongodb.md#documents)
- [MongoDB `@MongoId`](docs/user/mongodb.md#documents)
- [MongoDB `@MongoField`](docs/user/mongodb.md#documents)
- [MongoDB `@MongoIgnore`](docs/user/mongodb.md#documents)
- [MongoDB `@MongoRepository`](docs/user/mongodb.md#repositories)
- [MongoDB `@MongoQuery`](docs/user/mongodb.md#repositories)
- [MongoDB `@MongoWatch`](docs/user/mongodb.md#change-streams)

### Security

- [Local authentication](docs/user/security.md#local-authentication)
- [`@Authenticated`](docs/user/security.md#page-and-action-roles)
- [`@Roles`](docs/user/security.md#page-and-action-roles)
- [`@OwnedBy`](docs/user/security.md#ownership-checks)
- [Role-based template visibility](docs/user/security.md#role-based-visibility-in-templates)
- [DTO roles](docs/user/security.md#dto-roles)
- [Custom login template](docs/user/security.md#custom-login-template)
- [External OpenID Connect providers](docs/user/security.md#external-idp)
- [Keycloak](docs/user/security.md#keycloak)
- [Google](docs/user/security.md#google)
- [XIS as an OpenID Connect provider](docs/user/security.md#xis-as-an-openid-connect-provider)
- [SSO in distributed XIS applications](docs/user/security.md#sso-in-distributed-xis-applications)

### Plain HTTP Endpoints For External Clients

- [Plain endpoint annotations](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Controller`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Get`, `@Post`, `@Put`, `@Delete`, `@Head`, `@Options`, `@Trace`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@RequestBody`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@RequestHeader`, `@ResponseHeader`, `@CookieValue`, `@BearerToken`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@UrlParameter` and `one.xis.http.PathVariable`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Produces`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@PublicResources`](docs/user/annotations.md#plain-http-endpoint-annotations)

### Context, Configuration, And Extension Points

- [`@Component`, `@Service`, `@DefaultComponent`](docs/user/annotations.md#class-annotations)
- [`@Bean`](docs/user/annotations.md#method-annotations)
- [`@Inject`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Value`](docs/user/annotations.md#configuration-values)
- [`@Init`](docs/user/annotations.md#method-annotations)
- [`@EventListener`](docs/user/annotations.md#method-annotations)
- [`@Scheduled`](docs/user/scheduled-jobs.md)
- [`@Proxy`](docs/user/advanced/custom-proxies.md)
- [`@UseAdvice`](docs/user/advanced/aspects.md)
- [`@ImportInstances`](docs/user/annotations.md#advanced-and-rarely-needed-annotations)
- [Custom proxies](docs/user/advanced/custom-proxies.md)
- [Aspects and interface advice](docs/user/advanced/aspects.md)
- [Custom JavaScript extension files](docs/user/advanced/custom-javascript.md#register-extension-files)
- [Custom EL functions](docs/user/advanced/custom-javascript.md#add-custom-el-functions)
- [Submitting XIS forms from JavaScript](docs/user/advanced/custom-javascript.md#submit-a-xis-form-from-javascript)

### Testing And Tools

- [Examples and tests](docs/user/examples-and-tests.md)
- [Integration-test browser model](docs/user/advanced/integration-test-browser.md)
- [Drag and drop integration tests](docs/user/drag-and-drop.md#testing)
- [`@XisBootTest`](docs/user/examples-and-tests.md)
- [`one.xis.test.Mock`, `one.xis.test.Spy`, and `one.xis.test.Captor`](docs/user/examples-and-tests.md)
- [Gradle plugin and tools](docs/user/gradle-plugin.md)
- [`xisTemplates`](docs/user/gradle-plugin.md#xistemplates)
- [`xisTests`](docs/user/gradle-plugin.md#xistests)
- [`xisValidate`](docs/user/gradle-plugin.md#xisvalidate)
- [`xisJar`](docs/user/gradle-plugin.md#xisjar)
- [`xisRun`](docs/user/gradle-plugin.md#xisrun)

## Annotation Index

This index lists the public annotations a user can encounter. When two modules use the same simple name, the package is
shown in the link text.

### XIS Controller API

- [`@Action`](docs/user/annotations.md#method-annotations)
- [`@ActionParameter`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Authenticated`](docs/user/annotations.md#class-annotations)
- [`@ClientId`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@ClientState`](docs/user/annotations.md#browser-storage-parameters)
- [`@DefaultHtmlFile`](docs/user/template-location-and-mapping.md#default-templates)
- [`@FormData`](docs/user/forms-and-validation.md#basic-form-binding)
- [`@Frontlet`](docs/user/core-model.md#frontlets)
- [`@FrontletParameter`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@HtmlFile`](docs/user/template-location-and-mapping.md#explicit-template-with-htmlfile)
- [`@ImportInstances`](docs/user/annotations.md#advanced-and-rarely-needed-annotations)
- [`@Include`](docs/user/core-model.md#includes)
- [`@LocalStorage`](docs/user/annotations.md#browser-storage-parameters)
- [`@Modal`](docs/user/modals.md)
- [`@ModelData`](docs/user/core-model.md#model-data)
- [`@NullAllowed`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@OwnedBy`](docs/user/security.md#ownership-checks)
- [`@Page`](docs/user/core-model.md#pages)
- [`@PathVariable`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@QueryParameter`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@RefreshOnUpdateEvents`](docs/user/events.md)
- [`@Roles`](docs/user/security.md#page-and-action-roles)
- [`@Route`](docs/user/routers.md#how-routing-works)
- [`@Router`](docs/user/routers.md)
- [`@SessionStorage`](docs/user/annotations.md#browser-storage-parameters)
- [`@SharedValue`](docs/user/annotations.md#shared-values)
- [`@Title`](docs/user/annotations.md#method-annotations)
- [`@Upload`](docs/user/forms-and-validation.md#file-uploads)
- [`@UseFormatter`](docs/user/forms-and-validation.md#custom-formatters)
- [`@UserId`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@WelcomePage`](docs/user/core-model.md#pages)

### Validation

- [`@AllElementsMandatory`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@EMail`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@LabelKey`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Mandatory`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@MinLength`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@RegExpr`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Validate`](docs/user/annotations.md#parameter-field-and-record-component-annotations)

### Context And Boot

- [`@Bean`](docs/user/annotations.md#method-annotations)
- [`@Component`](docs/user/annotations.md#class-annotations)
- [`@DefaultComponent`](docs/user/annotations.md#class-annotations)
- [`@EventListener`](docs/user/annotations.md#method-annotations)
- [`@Inject`](docs/user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Init`](docs/user/annotations.md#method-annotations)
- [`@Proxy`](docs/user/advanced/custom-proxies.md)
- [`@UseAdvice`](docs/user/advanced/aspects.md)
- [`@Scheduled`](docs/user/scheduled-jobs.md)
- [`@Service`](docs/user/annotations.md#class-annotations)
- [`@Value`](docs/user/annotations.md#configuration-values)
- [`@XISBootApplication`](docs/user/annotations.md#class-annotations)

### Plain HTTP Controller API For External Clients

- [`@Controller`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Get`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Post`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Put`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`one.xis.http.@Delete`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Head`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Options`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Trace`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@RequestBody`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@RequestHeader`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@ResponseHeader`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@CookieValue`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@BearerToken`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@UrlParameter`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`one.xis.http.@PathVariable`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@Produces`](docs/user/annotations.md#plain-http-endpoint-annotations)
- [`@PublicResources`](docs/user/annotations.md#plain-http-endpoint-annotations)

### SQL

- [`@Column`](docs/user/sql.md#entity-mapping)
- [`one.xis.sql.@Delete`](docs/user/sql.md#delete)
- [`@Entity`](docs/user/sql.md#entity-mapping)
- [`@Function`](docs/user/sql.md#functions-and-stored-procedures)
- [`@Ignore`](docs/user/sql.md#entity-mapping)
- [`@Insert`](docs/user/sql.md#insert-and-update)
- [`@JsonColumn`](docs/user/sql.md#entity-mapping)
- [`@NoColumn`](docs/user/sql.md#entity-mapping)
- [`@OptionalColumn`](docs/user/sql.md#entity-mapping)
- [`@Param`](docs/user/sql.md#select)
- [`@Repository`](docs/user/sql.md#repositories)
- [`@Save`](docs/user/sql.md#save)
- [`@Select`](docs/user/sql.md#select)
- [`@StoredProcedure`](docs/user/sql.md#functions-and-stored-procedures)
- [`@Transactional`](docs/user/sql.md#transactions)
- [`@Update`](docs/user/sql.md#insert-and-update)

### MongoDB

- [`@MongoDocument`](docs/user/mongodb.md#documents)
- [`@MongoField`](docs/user/mongodb.md#documents)
- [`@MongoId`](docs/user/mongodb.md#documents)
- [`@MongoIgnore`](docs/user/mongodb.md#documents)
- [`@MongoQuery`](docs/user/mongodb.md#repositories)
- [`@MongoRepository`](docs/user/mongodb.md#repositories)
- [`@MongoWatch`](docs/user/mongodb.md#change-streams)

### Test Support

- [`@XisBootTest`](docs/user/examples-and-tests.md)
- [`one.xis.test.@InTestContext`](docs/user/examples-and-tests.md)
- [`one.xis.test.@Mock`](docs/user/examples-and-tests.md)
- [`one.xis.test.@Spy`](docs/user/examples-and-tests.md)
- [`one.xis.test.@Captor`](docs/user/examples-and-tests.md)

## Framework Developer Documentation

Use this layer when working on XIS itself:

- [Documentation strategy](docs/framework/documentation-strategy.md)
- [Migration map](docs/framework/migration-map.md)
- [Architecture](docs/architecture.md)

Module README files belong to this layer. They may explain internals, design trade-offs, and implementation details.

## Agent Documentation

Use this layer when an automated coding agent works in the repository:

- [Agent documentation rules](docs/agent/documentation-rules.md)

The root [AGENTS.md](AGENTS.md) file summarizes repository-wide working rules.

## Documentation Principle

This project treats documentation examples as part of the public API. A documented example should be copyable into a
project and should become test-covered when the related API stabilizes.

When changing public XIS behavior, update the user documentation and add or adapt an executable example.

## License

Apache License 2.0
