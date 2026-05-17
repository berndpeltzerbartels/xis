# Old Documentation Coverage

This file tracks the migration from the old XIS documentation app in `/Users/bernd/projects/xis-docs` to the Markdown
documentation in this repository. It is an editorial checklist, not user documentation.

Status values:

- `covered`: the topic is represented in current Markdown and checked against current code.
- `needs review`: the topic exists in Markdown, but still depends on implementation/product decisions before it can be called complete.
- `needs work`: the topic is missing or clearly incomplete.
- `deferred`: intentionally left for later.
- `obsolete`: old wording or concept no longer matches current XIS.

## Quickstart

| Old chapter | Old source | Current Markdown | Status | Notes |
| --- | --- | --- | --- | --- |
| Installation | `xis/quickstart/content/Installation.*` | `docs/user/quickstart.md`, `docs/user/runtime-and-dependencies.md` | covered | Spring/Boot dependency entry is covered. |
| Main Class | `xis/quickstart/content/MainClass.*` | `docs/user/quickstart.md` | covered | Boot and Spring entry points are covered. |
| Hello World | `xis/quickstart/content/HelloWorld.*` | `README.md`, `docs/user/quickstart.md` | covered | User sees Java and HTML together. |
| Testing | `xis/quickstart/content/Testing.*` | `docs/user/examples-and-tests.md` | covered | `xis-test`, `IntegrationTestContext`, URL-based `openPage`, document interaction, package scanning, and authenticated tests are covered. |
| Running the App | `xis/quickstart/content/RunningTheApp.*` | `docs/user/quickstart.md` | covered | Start/run flow is covered. |
| Welcome Page | `xis/quickstart/content/WelcomePage.*` | `docs/user/quickstart.md`, `docs/user/core-model.md` | covered | `@WelcomePage` is covered. |
| Page Links | `xis/quickstart/content/PageLinks.*` | `docs/user/navigation.md`, `docs/user/tags-and-attributes.md` | covered | Attribute and element syntax are covered. |

## Main User Guide

| Old chapter | Old source | Current Markdown | Status | Notes |
| --- | --- | --- | --- | --- |
| Introduction | `xis/docs/content/Introduction.*` | `README.md`, `docs/user/README.md` | covered | Public entry point exists. |
| Why XIS? | `xis/docs/content/WhyXis.*` | `README.md` | covered | Root README states the server-driven/vertical-slice value proposition without turning the user docs into marketing pages. |
| Hello World! | `xis/docs/content/HelloWorld.*` | `README.md`, `docs/user/quickstart.md` | covered | Covered with copyable files. |
| Installation | `xis/docs/content/Installation.*` | `docs/user/runtime-and-dependencies.md`, `docs/user/quickstart.md` | covered | Direct user dependencies only. |
| The Gradle Plugin | `xis/docs/content/GradlePlugin.*` | `docs/user/template-location-and-mapping.md`, `docs/user/quickstart.md`, `docs/user/examples-and-tests.md` | covered | Current user docs cover plugin dependency use, template generation, and test generation. Runnable-jar wording remains outside the normal user path. |
| XIS Boot | `xis/docs/content/xisboot/*` | `docs/user/runtime-and-dependencies.md`, `docs/user/quickstart.md`, `docs/user/annotations.md` | covered | Main class, scanning, properties, `@Component`, `@Service`, `@Bean`, `@Inject`, `@Init`, `@EventListener`, `@Value`, and singleton model are covered. |
| Pages and Frontlets | `xis/docs/content/pagesandfrontlets/*` | `docs/user/core-model.md`, `docs/user/navigation.md`, `docs/user/templates.md` | covered | Current docs use `frontlet`, with old frontlet wording avoided. |
| Testing | `xis/docs/content/Testing.*` | `docs/user/examples-and-tests.md` | covered | Expanded from old docs with copyable examples and current `frontlet` wording. |
| Template Location | `xis/docs/content/TemplateLocation.*` | `docs/user/template-location-and-mapping.md`, `docs/user/quickstart.md` | covered | Includes side-by-side Java/HTML, resources mirror, `@HtmlFile`, shared templates, `@DefaultHtmlFile`, and `xisTemplates` task. |
| Class Annotations | `xis/docs/content/ClassAnnotations.*` | `docs/user/annotations.md`, workflow chapters | covered | Public class annotations are listed and linked to workflow docs. Old `Frontlet` wording has been replaced by `Frontlet`. |
| Method Annotations | `xis/docs/content/MethodAnnotations.*` | `docs/user/annotations.md`, workflow chapters | covered | `@ModelData`, `@Action`, `@FormData`, `@SharedValue`, title, storage, roles, formatter, and lifecycle usage are covered with current behavior. |
| Template Syntax | `xis/docs/content/templatesyntax/*` | `docs/user/templates.md`, `docs/user/tags-and-attributes.md`, `docs/user/advanced/custom-javascript.md` | covered | Core syntax, tag/attribute dual forms, built-in EL functions, and custom EL functions are covered. |
| Parameter Annotations | `xis/docs/content/ParameterAnnotations.*` | `docs/user/annotations.md`, `docs/user/navigation.md`, `docs/user/forms-and-validation.md`, `docs/user/request-lifecycle.md` | covered | Public parameter annotations are listed; navigation/form/storage examples cover ordinary usage. |
| Frontlet Details | `xis/docs/content/frontlets/*` | `docs/user/navigation.md`, `docs/user/events.md` | covered | Navigation, parameters, containers, update events, and reload semantics are covered with current `frontlet` wording. |
| Request Lifecycle | `xis/docs/content/Lifecycle.*` | `docs/user/request-lifecycle.md`, `docs/architecture.md` | covered | User-facing lifecycle chapter now covers initial load, navigation, actions, forms, shared values, client state, and refresh events. |
| Deep Linking | `xis/docs/content/DeepLinking.*` | `docs/user/navigation.md` | covered | Path/query/deep link behavior is covered. |
| Forms | `xis/docs/content/forms/*` | `docs/user/forms-and-validation.md`, `docs/user/tags-and-attributes.md` | covered | Forms, validation, label keys, records, global messages, and formatters are represented. |
| Formatters | `xis/docs/content/Formatters.*` | `docs/user/forms-and-validation.md`, `docs/user/annotations.md` | covered | Localized number behavior and explicit formatter guidance are covered. |
| XIS-Theme | `xis/docs/content/XisTheme.*` | `docs/user/advanced/theme.md` | covered | Dependency-only CSS loading, variables, logo override, navigation, grid, forms, theme tags, and customization boundary are covered. |
| Security | `xis/docs/content/security/*` | `docs/user/security.md` | covered | Local auth, login variants, external OpenID Connect providers, XIS as IDP, and provider selection are covered. |
| Customizing | `xis/docs/content/customizing/*` | `docs/user/advanced/custom-javascript.md`, `docs/user/tags-and-attributes.md`, `docs/user/forms-and-validation.md` | covered | Custom JavaScript/custom EL functions, selection styling, validation messages, system messages, and theme customization are covered with current selectors and IDs. |
| Distributed Applications | `xis/docs/content/DistributedApplications.*`, `MicroFrontendArchitecture.*` | `docs/architecture.md`, `docs/user/advanced/microfrontend-architecture.md`, `xis-distributed/README.md` | covered | Advanced user docs describe the supported host-mapping model; E2E covers remote page and remote frontlet across two runtimes. |
| Reference | `xis/docs/content/reference/*` | `docs/user/annotations.md`, `docs/user/tags-and-attributes.md` | covered | Public annotation/tag/attribute references are split by workflow and checked against current names. Old `Frontlet`/`Parameter` names are obsolete. |

## Reference Detail

| Old reference | Old source | Current Markdown | Status | Notes |
| --- | --- | --- | --- | --- |
| Class Annotation Reference | `xis/docs/content/reference/ClassAnnotationReference.*` | `docs/user/annotations.md` | covered | Current reference uses `@Frontlet`, `@Include`, `@Roles`, `@RefreshOnUpdateEvents`, template annotations, and Boot entry annotations. |
| Method Annotation Reference | `xis/docs/content/reference/MethodAnnotationReference.*` | `docs/user/annotations.md` | covered | Current reference reflects public method-level annotations and avoids old `GlobalVariable`/frontlet wording. |
| Parameter Annotation Reference | `xis/docs/content/reference/ParameterAnnotationReference.*` | `docs/user/annotations.md`, `docs/user/navigation.md` | covered | Current reference reflects `@Parameter`, storage, `@ClientId`, `@UserId`, validation, formatter, and HTTP-controller parameters. |
| Field Annotation Reference | `xis/docs/content/reference/FieldAnnotationReference.*` | `docs/user/forms-and-validation.md`, `docs/user/annotations.md` | covered | Validation annotations, records, `@LabelKey`, custom validators, and message resolution are documented. |
| XIS Boot Annotation Reference | `xis/docs/content/reference/XisBootAnnotationReference.*` | `docs/user/runtime-and-dependencies.md`, `docs/user/annotations.md` | covered | XIS Boot context annotations and properties are covered for normal user use. |
| Framework Tags Reference | `xis/docs/content/reference/FrameworkTagsReference.*` | `docs/user/tags-and-attributes.md` | covered | Tag/attribute dual forms are represented. |
| Framework Attributes Reference | `xis/docs/content/reference/FrameworkAttributesReference.*` | `docs/user/tags-and-attributes.md` | covered | Attribute syntax is represented; check current runtime before adding any old-only attributes. |

## Open Follow-Up

| Topic | Status | Notes |
| --- | --- | --- |
| `xis-theme` | covered | User-facing theme documentation exists. |
| Custom JavaScript | covered | `docs/user/advanced/custom-javascript.md` covers META-INF extensions and custom EL functions. |
| Selectable/highlighting | covered | `docs/user/tags-and-attributes.md` covers `xis:selection-group` and `xis:selection-class`; runtime supports plain and `xis:` attributes via `DomNormalizer`. |
| System error handling | covered | `docs/user/forms-and-validation.md` and `docs/user/tags-and-attributes.md` distinguish form/global messages from `#system-messages`. Old `#messages` wording is obsolete. |
| Distributed applications | deferred | Old page was WIP; keep as architecture/advanced until public runtime behavior and tests are finalized. |
| Testing chapter | covered | `docs/user/examples-and-tests.md` is now a user guide for `xis-test`, not only a policy page. |
