# Old Documentation Coverage

This file tracks the migration from the old XIS documentation app in `/Users/bernd/projects/xis-docs` to the Markdown
documentation in this repository. It is an editorial checklist, not user documentation.

Status values:

- `covered`: the topic is represented in current Markdown and checked against current code.
- `needs review`: the topic exists in Markdown, but the old chapter should still be compared for missing explanations or examples.
- `needs work`: the topic is missing or clearly incomplete.
- `deferred`: intentionally left for later.
- `obsolete`: old wording or concept no longer matches current XIS.

`xis-theme` is intentionally deferred until the end.

## Quickstart

| Old chapter | Old source | Current Markdown | Status | Notes |
| --- | --- | --- | --- | --- |
| Installation | `xis/quickstart/content/Installation.*` | `docs/user/quickstart.md`, `docs/user/runtime-and-dependencies.md` | covered | Spring/Boot dependency entry is covered. |
| Main Class | `xis/quickstart/content/MainClass.*` | `docs/user/quickstart.md` | covered | Boot and Spring entry points are covered. |
| Hello World | `xis/quickstart/content/HelloWorld.*` | `README.md`, `docs/user/quickstart.md` | covered | User sees Java and HTML together. |
| Testing | `xis/quickstart/content/Testing.*` | `docs/user/examples-and-tests.md` | needs review | Test docs exist, but old examples should be compared when the test chapter is finalized. |
| Running the App | `xis/quickstart/content/RunningTheApp.*` | `docs/user/quickstart.md` | covered | Start/run flow is covered. |
| Welcome Page | `xis/quickstart/content/WelcomePage.*` | `docs/user/quickstart.md`, `docs/user/core-model.md` | covered | `@WelcomePage` is covered. |
| Page Links | `xis/quickstart/content/PageLinks.*` | `docs/user/navigation.md`, `docs/user/tags-and-attributes.md` | covered | Attribute and element syntax are covered. |

## Main User Guide

| Old chapter | Old source | Current Markdown | Status | Notes |
| --- | --- | --- | --- | --- |
| Introduction | `xis/docs/content/Introduction.*` | `README.md`, `docs/user/README.md` | covered | Public entry point exists. |
| Why XIS? | `xis/docs/content/WhyXis.*` | `README.md` | needs review | Marketing/positioning is lighter in Markdown. This is acceptable for now, but compare before public polish. |
| Hello World! | `xis/docs/content/HelloWorld.*` | `README.md`, `docs/user/quickstart.md` | covered | Covered with copyable files. |
| Installation | `xis/docs/content/Installation.*` | `docs/user/runtime-and-dependencies.md`, `docs/user/quickstart.md` | covered | Direct user dependencies only. |
| The Gradle Plugin | `xis/docs/content/GradlePlugin.*` | `docs/user/template-location-and-mapping.md`, `docs/user/quickstart.md` | needs review | Template task is covered; other plugin behavior should be compared later. |
| XIS Boot | `xis/docs/content/xisboot/*` | `docs/user/runtime-and-dependencies.md`, `docs/user/quickstart.md`, `docs/user/annotations.md` | needs review | Main class and properties are covered; context annotations need a final pass. |
| Pages and Widgets | `xis/docs/content/pagesandwidgets/*` | `docs/user/core-model.md`, `docs/user/navigation.md`, `docs/user/templates.md` | covered | Current docs use `frontlet`, with old widget wording avoided. |
| Testing | `xis/docs/content/Testing.*` | `docs/user/examples-and-tests.md` | needs review | Needs a final comparison when test docs are expanded. |
| Template Location | `xis/docs/content/TemplateLocation.*` | `docs/user/template-location-and-mapping.md`, `docs/user/quickstart.md` | covered | Includes side-by-side Java/HTML, resources mirror, `@HtmlFile`, shared templates, `@DefaultHtmlFile`, and `templates` task. |
| Class Annotations | `xis/docs/content/ClassAnnotations.*` | `docs/user/annotations.md`, workflow chapters | needs review | Reference exists; compare old narrative for examples. |
| Method Annotations | `xis/docs/content/MethodAnnotations.*` | `docs/user/annotations.md`, workflow chapters | needs review | Reference exists; storage/update-event old examples contain obsolete assumptions and must not be copied blindly. |
| Template Syntax | `xis/docs/content/templatesyntax/*` | `docs/user/templates.md`, `docs/user/tags-and-attributes.md`, `docs/user/advanced/README.md` | needs review | Core syntax is covered; advanced custom JavaScript/functions pages still need migration. |
| Parameter Annotations | `xis/docs/content/ParameterAnnotations.*` | `docs/user/annotations.md`, `docs/user/navigation.md`, `docs/user/forms-and-validation.md` | needs review | Most public annotations are listed; compare old examples for missing cases. |
| Widget Details | `xis/docs/content/widgets/*` | `docs/user/navigation.md`, `docs/user/advanced/refresh-events.md` | covered | Navigation, parameters, containers, update events, and reload semantics are covered with current `frontlet` wording. |
| Request Lifecycle | `xis/docs/content/Lifecycle.*` | `docs/architecture.md` | needs review | Architecture has lifecycle-level material, but not yet a user-facing lifecycle chapter. |
| Deep Linking | `xis/docs/content/DeepLinking.*` | `docs/user/navigation.md` | covered | Path/query/deep link behavior is covered. |
| Forms | `xis/docs/content/forms/*` | `docs/user/forms-and-validation.md`, `docs/user/tags-and-attributes.md` | covered | Forms, validation, label keys, records, global messages, and formatters are represented. |
| Formatters | `xis/docs/content/Formatters.*` | `docs/user/forms-and-validation.md`, `docs/user/annotations.md` | covered | Localized number behavior and explicit formatter guidance are covered. |
| XIS-Theme | `xis/docs/content/XisTheme.*` | `docs/user/advanced/README.md` | deferred | Do this last. |
| Security | `xis/docs/content/security/*` | `docs/user/security.md` | covered | Local auth, login variants, external OpenID Connect providers, XIS as IDP, and provider selection are covered. |
| Customizing | `xis/docs/content/customizing/*` | `docs/user/advanced/README.md` | needs work | Custom JavaScript, selectable/highlighting, and system errors are not fully migrated. |
| Distributed Applications | `xis/docs/content/DistributedApplications.*`, `MicroFrontendArchitecture.*` | `docs/architecture.md`, `docs/user/advanced/README.md` | needs review | Architecture exists; user-facing distributed docs still need careful migration. |
| Reference | `xis/docs/content/reference/*` | `docs/user/annotations.md`, `docs/user/tags-and-attributes.md` | needs review | Current references are split by workflow; final pass should ensure no public annotation/tag/attribute is missing. |

## Reference Detail

| Old reference | Old source | Current Markdown | Status | Notes |
| --- | --- | --- | --- | --- |
| Class Annotation Reference | `xis/docs/content/reference/ClassAnnotationReference.*` | `docs/user/annotations.md` | needs review | Old `Widget` wording is obsolete; current docs must use `Frontlet`. |
| Method Annotation Reference | `xis/docs/content/reference/MethodAnnotationReference.*` | `docs/user/annotations.md` | needs review | Old lists include obsolete/moved concepts; verify against current public annotations. |
| Parameter Annotation Reference | `xis/docs/content/reference/ParameterAnnotationReference.*` | `docs/user/annotations.md`, `docs/user/navigation.md` | needs review | Needs final public annotation scan. |
| Field Annotation Reference | `xis/docs/content/reference/FieldAnnotationReference.*` | `docs/user/forms-and-validation.md`, `docs/user/annotations.md` | needs review | Validation annotations are documented; compare for missing examples. |
| XIS Boot Annotation Reference | `xis/docs/content/reference/XisBootAnnotationReference.*` | `docs/user/runtime-and-dependencies.md`, `docs/user/annotations.md` | needs review | Boot-specific annotations are lightly covered. |
| Framework Tags Reference | `xis/docs/content/reference/FrameworkTagsReference.*` | `docs/user/tags-and-attributes.md` | covered | Tag/attribute dual forms are represented. |
| Framework Attributes Reference | `xis/docs/content/reference/FrameworkAttributesReference.*` | `docs/user/tags-and-attributes.md` | covered | Attribute syntax is represented; check current runtime before adding any old-only attributes. |

## Open Follow-Up

| Topic | Status | Notes |
| --- | --- | --- |
| `xis-theme` | deferred | Explicitly left until the end. |
| Custom JavaScript | needs work | Advanced user page still needed. |
| Selectable/highlighting | needs work | Depends on current theme/client behavior. |
| System error handling | needs work | Basic `system-messages` mention exists; full customization docs still missing. |
| Distributed applications | needs review | Architecture docs exist, user flow still needs care. |
| Testing chapter | needs review | Existing `examples-and-tests.md` is a policy page more than a user guide. |
