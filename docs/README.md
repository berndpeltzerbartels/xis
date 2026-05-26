# XIS Documentation

**XIS is a lightweight SPA framework for Java, Groovy, and Kotlin: annotation-driven, fast, and built around backend controllers
plus plain HTML templates. XIS provides ready-to-use client-server transport, state synchronization, navigation, form
handling, validation feedback, and partial UI refreshes without extra work and without any boilerplate code. It runs
standalone on the JVM with XIS Boot, as a GraalVM native executable for cloud-native deployment with XIS Boot Native,
or inside Spring. It supports distributed Microfrontend Architecture, and in most applications you write just annotated
backend controllers and templates. Coding is as simple as in the old days of request-response, but the result is a modern and
fast SPA application. XIS Boot Native supports Java and Kotlin native executables; Groovy is supported on the JVM path.**

This directory is the canonical documentation source for users, framework developers, and coding agents.

The old documentation application in `docs-app` / `/Users/bernd/projects/xis-docs` is source material, not the target
structure. The Markdown documentation in this repository should become the stable knowledge base.

## User Documentation

Start here if you want quick results:

- [Quickstart](user/quickstart.md)

Read these to build normal XIS applications:

- [Runtime and dependency model](user/runtime-and-dependencies.md)
- [XIS Boot Native and cloud-native native-image builds](user/runtime-and-dependencies.md#xis-boot-native)
- [Cloud Native and native images](user/cloud-native.md)
- [Gradle plugin and tools](user/gradle-plugin.md)
- [Groovy support](user/groovy.md)
- [Kotlin support](user/kotlin.md)
- [Template location and mapping](user/template-location-and-mapping.md)
- [Core model](user/core-model.md)
- [Annotation reference](user/annotations.md)
- [Template syntax](user/templates.md)
- [Tags and attributes](user/tags-and-attributes.md)
- [Request lifecycle](user/request-lifecycle.md)
- [Navigation and responses](user/navigation.md)
- [Forms and validation](user/forms-and-validation.md)
- [Modals](user/modals.md)
- [Drag and drop](user/drag-and-drop.md)
- [Events](user/events.md)
- [Scheduled jobs](user/scheduled-jobs.md)
- [Security](user/security.md)
- [Examples and tests](user/examples-and-tests.md)

## Advanced Topics

Optional or specialized capabilities are still important enough to be visible:

- [Microfrontend Architecture and distributed mode](user/advanced/microfrontend-architecture.md)
- [Reusable web artifacts with replaceable templates](user/advanced/reusable-web-artifacts.md)
- [Aspects and interface advice](user/advanced/aspects.md), including the native-image-friendly AOP model
- [Custom proxies](user/advanced/custom-proxies.md)
- [Explicit SQL transactions](user/advanced/sql-transactions.md)
- [Custom JavaScript and custom EL functions](user/advanced/custom-javascript.md)
- [Integration-test browser model](user/advanced/integration-test-browser.md)
- [XIS theme](user/advanced/theme.md)

User documentation should be organized around public API behavior and copyable examples, not around repository modules.

## Working Map

Use this map while building or reviewing an application. The root README shows the feature surface; this map is more
complete and intentionally detailed.

### Application Structure

- [Runtime choices and dependencies](user/runtime-and-dependencies.md)
- [Cloud Native and native images](user/cloud-native.md)
- [XIS Boot applications with `@XISBootApplication`](user/annotations.md#class-annotations)
- [Spring Boot integration](user/runtime-and-dependencies.md#runtime-choice)
- [Groovy 4+ controllers and forms](user/groovy.md)
- [Kotlin controllers, forms, and native executables](user/kotlin.md)
- [Template location, `@HtmlFile`, `@DefaultHtmlFile`, generated templates, generated tests](user/template-location-and-mapping.md)
- [Static resources](user/runtime-and-dependencies.md#static-resources)
- [Reusable web artifacts with replaceable templates](user/advanced/reusable-web-artifacts.md)
- [Microfrontend Architecture and distributed mode](user/advanced/microfrontend-architecture.md)

### Pages, Components, And UI Composition

- [Pages with `@Page` and `@WelcomePage`](user/core-model.md#pages)
- [Frontlets with `@Frontlet`](user/core-model.md#frontlets)
- [Frontlet containers](user/core-model.md#frontlet-containers)
- [Includes with `@Include`](user/core-model.md#includes)
- [Modals with `@Modal`](user/modals.md)
- [Routers with `@Router` and `@Route`](user/routers.md)
- [Page and frontlet titles with `@Title`](user/annotations.md#method-annotations)
- [CSS files with `@CssFile`](user/annotations.md#class-annotations)

### Controller Data And Actions

- [Model data with `@ModelData`](user/core-model.md#model-data)
- [Form data with `@FormData`](user/forms-and-validation.md#basic-form-binding)
- [Actions with `@Action`](user/core-model.md#actions)
- [Action results as model data with `@Action` plus `@ModelData`](user/annotations.md#action-results-as-model-data)
- [Shared values with `@SharedValue`](user/annotations.md#shared-values)
- [Path variables with `@PathVariable`](user/annotations.md#parameter-field-and-record-component-annotations)
- [Query parameters with `@QueryParameter`](user/annotations.md#parameter-field-and-record-component-annotations)
- [Action parameters with `@ActionParameter`](user/annotations.md#parameter-field-and-record-component-annotations)
- [Frontlet parameters with `@FrontletParameter`](user/annotations.md#parameter-field-and-record-component-annotations)
- [Modal parameters with `@ModalParameter`](user/annotations.md#parameter-field-and-record-component-annotations)
- [Client id with `@ClientId`](user/annotations.md#parameter-field-and-record-component-annotations)
- [User id with `@UserId`](user/annotations.md#parameter-field-and-record-component-annotations)
- [Current user context with `UserContext`](user/annotations.md#parameter-field-and-record-component-annotations)

### Navigation And Responses

- [Page links and `xis:page`](user/navigation.md#page-links)
- [Action links, buttons, and `xis:action`](user/navigation.md#action-links-and-buttons)
- [Form actions and validation-aware submit behavior](user/navigation.md#form-actions)
- [Returning page classes](user/navigation.md#return-a-page-class)
- [Returning `PageResponse`](user/navigation.md#return-pageresponse)
- [Returning `PageUrlResponse`](user/navigation.md#return-pageurlresponse)
- [Frontlet navigation and `FrontletResponse`](user/navigation.md#frontlet-navigation)
- [Returning `ModalResponse`](user/navigation.md#return-modalresponse)
- [Deep linking](user/navigation.md#deep-linking)
- [`void` actions and automatic refresh](user/navigation.md#void-actions)

### Templates, Tags, And Attributes

- [Expression language](user/templates.md#expression-language)
- [Property and index access](user/templates.md#property-and-index-access)
- [Operators and conditions](user/templates.md#operators-and-conditions)
- [Built-in EL functions](user/templates.md#built-in-el-functions)
- [Formatting helpers such as `formatDate`, `formatDateTime`, and `formatTime`](user/templates.md#built-in-el-functions)
- [Iteration with `xis:foreach`, `xis:repeat`, and `<xis:foreach>`](user/templates.md#iteration)
- [Conditions with `xis:if` and `<xis:if>`](user/templates.md#conditions)
- [XIS navigation tags and attributes](user/tags-and-attributes.md#page-navigation)
- [XIS action tags and attributes](user/tags-and-attributes.md#actions)
- [Drag and drop attributes `xis:drag` and `xis:drop`](user/tags-and-attributes.md#drag-and-drop-actions)
- [Modal tag and attribute syntax](user/tags-and-attributes.md#modals)
- [Frontlet tag and attribute syntax](user/tags-and-attributes.md#frontlets)
- [Includes](user/tags-and-attributes.md#includes)
- [Forms](user/tags-and-attributes.md#forms)
- [Validation message tags and attributes](user/tags-and-attributes.md#validation-messages)
- [Error highlighting with `xis:error-class` and `xis:error-style`](user/tags-and-attributes.md#validation-messages)
- [Dynamic classes with `xis:class`](user/tags-and-attributes.md#dynamic-classes)
- [Client state and storage bindings](user/tags-and-attributes.md#client-state)
- [Raw trusted content with `xis:raw`](user/tags-and-attributes.md#raw-content)

### Template Attribute Index

- [`xis:src`](user/tags-and-attributes.md#tags-and-attributes)
- [`xis:if`](user/tags-and-attributes.md#conditions)
- [`xis:foreach`](user/tags-and-attributes.md#iteration)
- [`xis:repeat`](user/tags-and-attributes.md#iteration)
- [`xis:page`](user/tags-and-attributes.md#page-navigation)
- [`xis:action`](user/tags-and-attributes.md#actions)
- [`xis:drag`](user/tags-and-attributes.md#drag-and-drop-actions)
- [`xis:drop`](user/tags-and-attributes.md#drag-and-drop-actions)
- [`xis:modal`](user/tags-and-attributes.md#modals)
- [`xis:frontlet-container`](user/tags-and-attributes.md#frontlets)
- [`xis:default-frontlet`](user/tags-and-attributes.md#frontlets)
- [`xis:frontlet`](user/tags-and-attributes.md#frontlets)
- [`xis:target-container`](user/tags-and-attributes.md#frontlets)
- [`xis:include`](user/tags-and-attributes.md#includes)
- [`xis:binding`](user/tags-and-attributes.md#forms)
- [`xis:message-for`](user/tags-and-attributes.md#validation-messages)
- [`xis:error-class`](user/tags-and-attributes.md#validation-messages)
- [`xis:error-binding`](user/tags-and-attributes.md#validation-messages)
- [`xis:error-style`](user/tags-and-attributes.md#validation-messages)
- [`xis:selection-group`](user/tags-and-attributes.md#dynamic-classes)
- [`selection-group`](user/tags-and-attributes.md#dynamic-classes)
- [`xis:selection-class`](user/tags-and-attributes.md#dynamic-classes)
- [`selection-class`](user/tags-and-attributes.md#dynamic-classes)
- [`xis:storage-binding`](user/tags-and-attributes.md#client-state)

### Template Tag Index

- [`<xis:if>`](user/tags-and-attributes.md#conditions)
- [`<xis:foreach>`](user/tags-and-attributes.md#iteration)
- [`<xis:a>`](user/tags-and-attributes.md#page-navigation)
- [`<xis:button>`](user/tags-and-attributes.md#page-navigation)
- [`<xis:action>`](user/tags-and-attributes.md#actions)
- [`<xis:parameter>`](user/tags-and-attributes.md#actions)
- [`<xis:frontlet-container>`](user/tags-and-attributes.md#frontlets)
- [`<xis:frontlet>`](user/tags-and-attributes.md#frontlets)
- [`<xis:include>`](user/tags-and-attributes.md#includes)
- [`<xis:form>`](user/tags-and-attributes.md#forms)
- [`<xis:input>`](user/tags-and-attributes.md#forms)
- [`<xis:textarea>`](user/tags-and-attributes.md#forms)
- [`<xis:select>`](user/tags-and-attributes.md#forms)
- [`<xis:checkbox>`](user/tags-and-attributes.md#forms)
- [`<xis:radio>`](user/tags-and-attributes.md#forms)
- [`<xis:submit>`](user/tags-and-attributes.md#forms)
- [`<xis:message>`](user/tags-and-attributes.md#validation-messages)
- [`<xis:global-messages>`](user/tags-and-attributes.md#validation-messages)
- [`<xis:storage-binding>`](user/tags-and-attributes.md#client-state)
- [`<xis:raw>`](user/tags-and-attributes.md#raw-content)

### Forms, Validation, And Formatting

- [Basic form binding](user/forms-and-validation.md#basic-form-binding)
- [Multiple submit actions](user/forms-and-validation.md#multiple-submit-actions)
- [Nested objects and lists](user/forms-and-validation.md#nested-objects-and-lists)
- [File uploads with `@Upload`](user/forms-and-validation.md#file-uploads)
- [Type conversion](user/forms-and-validation.md#type-conversion)
- [Custom formatters with `@UseFormatter`](user/forms-and-validation.md#custom-formatters)
- [Validation annotations `@Mandatory`, `@AllElementsMandatory`, `@EMail`, `@MinLength`, `@RegExpr`, `@Validate`, `@LabelKey`, `@NullAllowed`](user/annotations.md#parameter-field-and-record-component-annotations)
- [Validation messages, global messages, and field messages](user/forms-and-validation.md)
- [Validation highlighting](user/tags-and-attributes.md#validation-messages)

### Events, Push, And Client Refresh

- [Refresh events](user/events.md)
- [Server-triggered refresh events](user/request-lifecycle.md#server-triggered-refresh-events)
- [`@RefreshOnUpdateEvents`](user/annotations.md#class-annotations)
- [`RefreshEventPublisher`](user/events.md)
- [Publishing to all clients](user/events.md)
- [Publishing to one client](user/events.md)
- [Visible page and frontlet refresh behavior](user/events.md)
- [Scheduled jobs with `@Scheduled`](user/scheduled-jobs.md)

### Client State

- [`@LocalStorage`](user/annotations.md#browser-storage-parameters)
- [`@SessionStorage`](user/annotations.md#browser-storage-parameters)
- [`@ClientStorage`](user/annotations.md#browser-storage-parameters)
- [`@LocalDatabase`](user/annotations.md#method-annotations)
- [Storage bindings in templates](user/tags-and-attributes.md#client-state)
- [Integration-test browser storage](user/advanced/integration-test-browser.md#storage)

### Persistence

- [SQL overview](user/sql.md)
- [SQL `@Entity`](user/sql.md#entity-mapping)
- [SQL `@Column`](user/sql.md#entity-mapping)
- [SQL `@Ignore`](user/sql.md#entity-mapping)
- [SQL `@JsonColumn`](user/sql.md#entity-mapping)
- [SQL `@NoColumn`](user/sql.md#entity-mapping)
- [SQL `@OptionalColumn`](user/sql.md#entity-mapping)
- [SQL `@Repository`](user/sql.md#repositories)
- [SQL `@Param`](user/sql.md#select)
- [SQL `@Select`](user/sql.md#select)
- [SQL `@Insert`](user/sql.md#insert-and-update)
- [SQL `@Update`](user/sql.md#insert-and-update)
- [SQL `@Save`](user/sql.md#save)
- [SQL `@Delete`](user/sql.md#delete)
- [SQL `@Transactional`](user/sql.md#transactions)
- [SQL `@Function`](user/sql.md#functions-and-stored-procedures)
- [SQL `@StoredProcedure`](user/sql.md#functions-and-stored-procedures)
- [MongoDB overview](user/mongodb.md)
- [MongoDB `@MongoDocument`](user/mongodb.md#documents)
- [MongoDB `@MongoId`](user/mongodb.md#documents)
- [MongoDB `@MongoField`](user/mongodb.md#documents)
- [MongoDB `@MongoIgnore`](user/mongodb.md#documents)
- [MongoDB `@MongoRepository`](user/mongodb.md#repositories)
- [MongoDB `@MongoQuery`](user/mongodb.md#repositories)
- [MongoDB `@MongoWatch`](user/mongodb.md#change-streams)

### Security

- [Local authentication](user/security.md#local-authentication)
- [`@Authenticated`](user/security.md#page-and-action-roles)
- [`@Roles`](user/security.md#page-and-action-roles)
- [`@OwnedBy`](user/security.md#ownership-checks)
- [Role-based template visibility](user/security.md#role-based-visibility-in-templates)
- [DTO roles](user/security.md#dto-roles)
- [Custom login template](user/security.md#custom-login-template)
- [External OpenID Connect providers](user/security.md#external-idp)
- [Keycloak](user/security.md#keycloak)
- [Google](user/security.md#google)
- [XIS as an OpenID Connect provider](user/security.md#xis-as-an-openid-connect-provider)
- [SSO in distributed XIS applications](user/security.md#sso-in-distributed-xis-applications)
- [Authentication helper annotations such as `@Login` and `@IDPLoginData`](user/security.md)

### Plain HTTP Endpoints For External Clients

- [Plain endpoint annotations](user/annotations.md#plain-http-endpoint-annotations)
- [`@Controller`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Get`, `@Post`, `@Put`, `@Delete`, `@Head`, `@Options`, `@Trace`](user/annotations.md#plain-http-endpoint-annotations)
- [`@RequestBody`](user/annotations.md#plain-http-endpoint-annotations)
- [`@RequestHeader`, `@ResponseHeader`, `@CookieValue`, `@BearerToken`](user/annotations.md#plain-http-endpoint-annotations)
- [`@UrlParameter` and `one.xis.http.PathVariable`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Produces`](user/annotations.md#plain-http-endpoint-annotations)
- [`@PublicResources`](user/annotations.md#plain-http-endpoint-annotations)

### Context, Configuration, And Extension Points

- [`@Component`, `@Service`, `@DefaultComponent`](user/annotations.md#class-annotations)
- [`@Bean`](user/annotations.md#method-annotations)
- [`@Inject`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Value`](user/annotations.md#configuration-values)
- [`@Init`](user/annotations.md#method-annotations)
- [`@EventListener`](user/annotations.md#method-annotations)
- [`@Scheduled`](user/scheduled-jobs.md)
- [`@Proxy`](user/advanced/custom-proxies.md)
- [`@UseAdvice`](user/advanced/aspects.md)
- [`@ImportInstances`](user/annotations.md#advanced-and-rarely-needed-annotations)
- [Custom proxies](user/advanced/custom-proxies.md)
- [Aspects and interface advice](user/advanced/aspects.md)
- [`@JavascriptExtension`](user/annotations.md#class-annotations)
- [Custom JavaScript extension files](user/advanced/custom-javascript.md#register-extension-files)
- [Custom EL functions](user/advanced/custom-javascript.md#add-custom-el-functions)
- [Submitting XIS forms from JavaScript](user/advanced/custom-javascript.md#submit-a-xis-form-from-javascript)

### Testing And Tools

- [Examples and tests](user/examples-and-tests.md)
- [Integration-test browser model](user/advanced/integration-test-browser.md)
- [Drag and drop integration tests](user/drag-and-drop.md#testing)
- [`@XisBootTest`](user/examples-and-tests.md)
- [`one.xis.test.Mock`, `one.xis.test.Spy`, and `one.xis.test.Captor`](user/examples-and-tests.md)
- [Gradle plugin and tools](user/gradle-plugin.md)
- [`xisTemplates`](user/gradle-plugin.md#xistemplates)
- [`xisTests`](user/gradle-plugin.md#xistests)
- [`xisValidate`](user/gradle-plugin.md#xisvalidate)
- [`xisJar`](user/gradle-plugin.md#xisjar)
- [`xisRun`](user/gradle-plugin.md#xisrun)

## Annotation Index

This index lists the public annotations a user can encounter. When two modules use the same simple name, the package is
shown in the link text.

### XIS Controller API

- [`@Action`](user/annotations.md#method-annotations)
- [`@ActionParameter`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Authenticated`](user/annotations.md#class-annotations)
- [`@ClientId`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@ClientStorage`](user/annotations.md#browser-storage-parameters)
- [`@CssFile`](user/annotations.md#class-annotations)
- [`@DefaultHtmlFile`](user/template-location-and-mapping.md#default-templates)
- [`@FormData`](user/forms-and-validation.md#basic-form-binding)
- [`@Frontlet`](user/core-model.md#frontlets)
- [`@FrontletParameter`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@HtmlFile`](user/template-location-and-mapping.md#explicit-template-with-htmlfile)
- [`@ImportInstances`](user/annotations.md#advanced-and-rarely-needed-annotations)
- [`@Include`](user/core-model.md#includes)
- [`@JavascriptExtension`](user/advanced/custom-javascript.md#register-extension-files)
- [`@LocalDatabase`](user/annotations.md#method-annotations)
- [`@LocalStorage`](user/annotations.md#browser-storage-parameters)
- [`@MainClass`](user/annotations.md#advanced-and-rarely-needed-annotations)
- [`@Modal`](user/modals.md)
- [`@ModelData`](user/core-model.md#model-data)
- [`@NullAllowed`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@OwnedBy`](user/security.md#ownership-checks)
- [`@Page`](user/core-model.md#pages)
- [`@PathVariable`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@QueryParameter`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@RefreshOnUpdateEvents`](user/events.md)
- [`@Roles`](user/security.md#page-and-action-roles)
- [`@Route`](user/routers.md#how-routing-works)
- [`@Router`](user/routers.md)
- [`@SessionStorage`](user/annotations.md#browser-storage-parameters)
- [`@SharedValue`](user/annotations.md#shared-values)
- [`@Title`](user/annotations.md#method-annotations)
- [`@Upload`](user/forms-and-validation.md#file-uploads)
- [`@UseFormatter`](user/forms-and-validation.md#custom-formatters)
- [`@UserId`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@WelcomePage`](user/core-model.md#pages)

### Validation

- [`@AllElementsMandatory`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@EMail`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@LabelKey`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Mandatory`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@MinLength`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@RegExpr`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Validate`](user/annotations.md#parameter-field-and-record-component-annotations)

### Context And Boot

- [`@Bean`](user/annotations.md#method-annotations)
- [`@Component`](user/annotations.md#class-annotations)
- [`@DefaultComponent`](user/annotations.md#class-annotations)
- [`@EventListener`](user/annotations.md#method-annotations)
- [`@Inject`](user/annotations.md#parameter-field-and-record-component-annotations)
- [`@Init`](user/annotations.md#method-annotations)
- [`@Proxy`](user/advanced/custom-proxies.md)
- [`@UseAdvice`](user/advanced/aspects.md)
- [`@Scheduled`](user/scheduled-jobs.md)
- [`@Service`](user/annotations.md#class-annotations)
- [`@Value`](user/annotations.md#configuration-values)
- [`@XISBootApplication`](user/annotations.md#class-annotations)

### Plain HTTP Controller API For External Clients

- [`@Controller`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Get`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Post`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Put`](user/annotations.md#plain-http-endpoint-annotations)
- [`one.xis.http.@Delete`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Head`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Options`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Trace`](user/annotations.md#plain-http-endpoint-annotations)
- [`@RequestBody`](user/annotations.md#plain-http-endpoint-annotations)
- [`@RequestHeader`](user/annotations.md#plain-http-endpoint-annotations)
- [`@ResponseHeader`](user/annotations.md#plain-http-endpoint-annotations)
- [`@CookieValue`](user/annotations.md#plain-http-endpoint-annotations)
- [`@BearerToken`](user/annotations.md#plain-http-endpoint-annotations)
- [`@UrlParameter`](user/annotations.md#plain-http-endpoint-annotations)
- [`one.xis.http.@PathVariable`](user/annotations.md#plain-http-endpoint-annotations)
- [`@Produces`](user/annotations.md#plain-http-endpoint-annotations)
- [`@PublicResources`](user/annotations.md#plain-http-endpoint-annotations)

### SQL

- [`@Column`](user/sql.md#entity-mapping)
- [`one.xis.sql.@Delete`](user/sql.md#delete)
- [`@Entity`](user/sql.md#entity-mapping)
- [`@Function`](user/sql.md#functions-and-stored-procedures)
- [`@Ignore`](user/sql.md#entity-mapping)
- [`@Insert`](user/sql.md#insert-and-update)
- [`@JsonColumn`](user/sql.md#entity-mapping)
- [`@NoColumn`](user/sql.md#entity-mapping)
- [`@OptionalColumn`](user/sql.md#entity-mapping)
- [`@Param`](user/sql.md#select)
- [`@Repository`](user/sql.md#repositories)
- [`@Save`](user/sql.md#save)
- [`@Select`](user/sql.md#select)
- [`@StoredProcedure`](user/sql.md#functions-and-stored-procedures)
- [`@Transactional`](user/sql.md#transactions)
- [`@Update`](user/sql.md#insert-and-update)

### MongoDB

- [`@MongoDocument`](user/mongodb.md#documents)
- [`@MongoField`](user/mongodb.md#documents)
- [`@MongoId`](user/mongodb.md#documents)
- [`@MongoIgnore`](user/mongodb.md#documents)
- [`@MongoQuery`](user/mongodb.md#repositories)
- [`@MongoRepository`](user/mongodb.md#repositories)
- [`@MongoWatch`](user/mongodb.md#change-streams)

### Security And Test Support

- [`@Login`](user/security.md)
- [`@IDPLoginData`](user/security.md)
- [`@XisBootTest`](user/examples-and-tests.md)
- [`one.xis.test.@InTestContext`](user/examples-and-tests.md)
- [`one.xis.test.@Mock`](user/examples-and-tests.md)
- [`one.xis.test.@Spy`](user/examples-and-tests.md)
- [`one.xis.test.@Captor`](user/examples-and-tests.md)

## Framework Developer Documentation

Use this layer when working on XIS itself:

- [Documentation strategy](framework/documentation-strategy.md)
- [Migration map](framework/migration-map.md)
- [Architecture](architecture.md)

Module README files belong to this layer. They may explain internals, design trade-offs, and implementation details.

## Agent Documentation

Use this layer when an automated coding agent works in the repository:

- [Agent documentation rules](agent/documentation-rules.md)

The root [agents.mds](../agents.mds) file summarizes repository-wide working rules.
