# Changelog

## Unreleased

### Added

- Added Kotlin support for XIS Boot applications, including component catalog generation, Kotlin-side template resources,
  form/action handling, and XIS Boot Native compilation.

## 0.11.2 - 2026-05-19

### Fixed

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
