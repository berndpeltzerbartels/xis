# Changelog

## Unreleased

### Added

- Added the `xis-language-metadata` module to publish XIS template schema metadata separately for IDE and tooling
  integrations.

### Changed

- IDP token generation now applies the configured access, refresh, and ID token validities separately and reports token
  response expiration values as relative durations.
- Moved XIS template schema resources out of `xis-javascript`, removed the obsolete generated HTML schema, and refreshed
  the XIS schema metadata for current template elements and attributes.

## 0.16.2 - 2026-05-30

### Changed

- Renamed the public XIS Theme template namespace from `theme:` to `xt:` so themed templates stay compact without using a
  broad or ambiguous prefix.
- Improved themed navigation styling, including clearer nested navigation groups and more stable hover behavior.
- Clarified the release workflow for agent-driven releases, including ZIP verification, changelog updates, documentation
  version checks, and post-release development version bumps.

### Fixed

- Fixed same-page navigation so clicking a link to the current page refreshes page data without replacing the page with
  incomplete markup.
- Fixed frontend error toasts so core server and connection errors use the active theme styling and avoid duplicate
  theme/core handlers.
- Fixed JavaScript error formatting so unhandled XHR-style values are reported with useful status details instead of
  `[object XMLHttpRequest]`.
- Fixed theme namespace validation tests and E2E theme fixtures for the new `xt:` namespace.
- Made the frontlet E2E click counter wait for the asynchronously loaded frontlet model data before parsing the value.

### Documentation

- Documented reusable JavaScript extension artifacts and noted that the same artifact pattern can ship a custom EL
  function library.
- Updated documented Gradle plugin and dependency examples to `0.16.2`.

## 0.16.1 - 2026-05-29

### Changed

- Removed unused public API annotations and hooks that were not fully supported, including `@MainClass`, `@CssFile`,
  `@JavascriptExtension`, and `@LocalDatabase`.
- Removed the unused `FrontletResponse.reloadFrontlet` shortcut to avoid confusion with targeted container/frontlet
  reloads.
- Clarified distributed configuration by documenting the current `hosts` and `origins` setup for XIS Boot and Spring
  Boot applications.

### Fixed

- Server-side action errors are now surfaced through the built-in toast message UI instead of being left only in the
  rendered page markup.
- `@XisBootTest` integration contexts now support custom annotations in scanned test applications.
- Template validation now rejects unknown `xis:*` elements instead of silently accepting misspelled framework tags.

### Documentation

- Expanded the annotation, tag, attribute, EL function, include, HTTP controller, and response API documentation.
- Documented generated test setup, Spring bean handling in XIS tests, and release E2E expectations.
- Updated documented Gradle plugin and dependency examples to `0.16.1`.

## 0.16.0 - 2026-05-27

### Added

- Added method-level `@ClientState`, `@LocalStorage`, and `@SessionStorage` support so simple client-side values can be
  provided and cleared without a dedicated action parameter or form field.
- Added `ToastMessages` as the explicit action response type for toast output.

### Changed

- Removed the experimental `Frontend` response object because mixing frontend state with targeted frontlet responses made
  action refresh behavior ambiguous.
- Split storage-only lifecycle invocation from model/form-data lifecycle invocation so actions no longer trigger redundant
  model reloads when the current frontlet stays in place.

### Fixed

- Fixed shared value refresh after actions by clearing action-phase shared values before follow-up model/form-data
  invocation.
- Fixed frontlet update event refresh handling and remote frontlet response processing.
- Fixed field clearing for client state and browser storage method values when a returned value contains `null` fields.

### Documentation

- Documented the manual release E2E suites and updated the documented XIS Gradle plugin version to `0.16.0`.

## 0.15.0 - 2026-05-27

### Maintenance

- Documented the release workflow and Git discipline for future agent-driven release work.

## 0.14.0 - 2026-05-27

### Added

- Added explicit parameter scope annotations for controller methods, including `@ActionParameter`, `@FrontletParameter`,
  and `@ModalParameter`, so action, frontlet, and modal values are no longer mixed through one generic parameter channel.
- Added `@ClientState` as the renamed client-side state API, replacing the former client storage naming while keeping the
  request lifecycle explicit.
- Added action-specific business validation through `ValidationFailedException`. Actions can now report global and
  field-bound validation messages without inventing a dedicated validation annotation for one-off business rules.
- Added coverage for repeated anonymous frontlet containers so several instances of the same default frontlet keep their
  own parameters and refresh independently.
- Added an SQL proxy context build regression test for contexts that scan the SQL infrastructure package together with a
  separate application package.

### Fixed

- Fixed frontlet container refresh handling for repeated anonymous containers, especially when update events target all
  instances of one frontlet type.
- Fixed propagation of update event keys when an action response continues into a next controller.
- Improved missing validation message keys by rendering them as `[message.key]` instead of silently returning `null`.
- Tightened template validation around scoped frontend parameters while allowing methods with frontend-provided form data
  and model data to bypass impossible compile-time key checks.

### Documentation

- Documented `ValidationFailedException` as the special-case validation path for action-specific business rules.
- Updated the client state and parameter-scope documentation to match the 0.14.0 API names.

## 0.12.0 - 2026-05-19

### Added

- Added Kotlin support for XIS Boot applications, including component catalog generation, Kotlin-side template resources,
  form/action handling, and XIS Boot Native compilation.

## 0.11.2 - 2026-05-19

### Fixed

- Fixed generated XIS starter tests so they create their integration context in `@BeforeEach` and explicitly register the
  page controller.
- Added plugin functional coverage that generates starter tests in an external Gradle consumer and runs `gradle test` for
  Java and Groovy page setups.
- Fixed SSE reconnect handling after JavaScript encapsulation so actions wait for the restored event stream before
  publishing client events.
- Kept the internal `window.app` object private while exposing small public `window.XIS` hooks for supported event-stream
  checks.
- Fixed and documented Groovy template handling so HTML files next to Groovy controllers are copied into runtime
  resources.

## 0.11.1 - 2026-05-18

### Fixed

- Published `xis-boot-http` as part of the release so versionless XIS dependencies managed by the Gradle plugin resolve
  correctly for external consumers.
- Added release artifact validation and an isolated external-consumer smoke test so Maven Central release zips are checked
  without relying on `mavenLocal()`.

## 0.11.0 - 2026-05-18

### Added

- Added XIS Boot Native support for GraalVM native images, including generated native runners, component catalogs,
  reflection/proxy/resource metadata, and native smoke coverage.
- Added native database support modules for H2, PostgreSQL, MariaDB, and MongoDB.
- Added XIS Theme DSL support for standard application layouts, forms, navigation, validation-aware generated markup,
  and theme validation integration.
- Added SQL date/time mapping coverage for real PostgreSQL and MariaDB system tests.

### Fixed

- Improved SSE emitter registration so multiple browser windows with the same client id can coexist without reconnect
  storms.
- Improved Safari-compatible local authentication callback handling.

## 0.10.0 - 2026-05-16

### Added

- Added backend router controllers with `@Router` and `@Route` for route-only server-side navigation before a page
  controller is selected.
- Added `xisValidate` as the Gradle task entry point for XIS validation checks. It validates XIS template structure,
  required attributes, drag/drop and selection rules, model/form data usage, and supports `--all-errors` to report more
  than the first validation error.
- Expanded `xisTemplates` so generated starter templates include simple output for `@ModelData`, forms for `@FormData`,
  and buttons for standalone `@Action` methods.
- Added Groovy support for the XIS Gradle plugin: `xisTemplates` and `xisTests` now scan Groovy controllers when the Groovy
  plugin is applied, `xisTests` generates Groovy starter tests for Groovy page controllers, and `xisJar` can package
  applications whose `@XISBootApplication` class is written in Groovy.
- Added `xis-boot-test-jupiter` and `xis-boot-starter-test`. Generated tests now use `@XisBootTest`, can receive an
  `IntegrationTestContext` field, and compile in both Java and Groovy variants.
- Renamed the scaffolding Gradle tasks from `templates` and `tests` to `xisTemplates` and `xisTests`.
- Added initial `xis-sql` repository support with `@Repository`, generic `CrudRepository` methods, SQL method
  annotations, entity mapping, and a default XIS Boot `DataSource`.
- Added optional configuration injection with `@Value(mandatory = false)`.
- Added `@Function` and `@StoredProcedure` for SQL repository methods backed by JDBC callable statements.
- Added `@Transactional` for SQL repository methods. Default repository methods can now group several SQL calls into one
  JDBC transaction with commit on normal return and rollback on exceptions.
- Added request-scoped SQL transaction support through `SqlTransaction`. Connections are opened lazily, shared across SQL
  repository calls in the current request/thread, and closed at request end with automatic commit or rollback.
- Added optional HikariCP connection pooling for the default `xis-sql` `DataSource`.
- Added entity-driven `@Insert`, `@Update`, `@Save`, and `@Delete` handling in `xis-sql`, including composite
  primary-key support for annotated repository methods.
- Added initial `xis-mongodb` support with Mongo document mapping, CRUD repositories, explicit `@MongoQuery` methods, and
  `@MongoWatch` change-stream handlers.

### Fixed

- String-valued `@ModelData` methods are no longer treated as navigation results. String navigation is limited to
  actions and router methods.
- `@HtmlFile("...")` template generation now treats relative paths as relative to the controller package.
- Development resources are resolved from source files again. Templates in `src/main/java` or `src/main/resources` are
  reparsed after timestamp changes, and XIS Boot public assets such as CSS are read from source files during development.

### Documentation

- Expanded the `xisTemplates` documentation to describe the controller-first workflow and the generated template mapping
  for model data, form data, validation placeholders, and actions.

## 0.9.3 - 2026-05-09

### Fixed

- XIS Boot now returns framework JSON error responses for unhandled server errors instead of fallback HTML error pages.
  This keeps the browser-side XIS error handling intact instead of replacing the application view with an HTML error
  document.
- The JavaScript client now handles unexpected non-JSON server error responses more robustly.
- The Gradle plugin's `xisJar` task now fails with a clear message when no `@XISBootApplication` class generated the
  boot entry point.
- Shared request values no longer leak into normal controller responses unless explicitly returned as model data.
- SSE reconnect handling is more robust before normal XIS HTTP requests, and short reconnect gaps no longer drop refresh
  events immediately.

### Documentation

- Documented the generated XIS Boot jar name: `build/libs/<project-name>-<version>.jar`.
- `xisJar` now uses Gradle's normal archive name without adding a `-xis` classifier.
- Documented that `@XISBootApplication` drives annotation-processor generation of `one.xis.boot.Runner` for XIS Boot
  executable jars.
- Documented the supported `@Action` + `@ModelData` combination for action results that should be rendered as model data.
