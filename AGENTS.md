# AGENTS

This file is the repo-wide memory for Codex work on XIS. Module-level `AGENTS.md` files may add local rules for
specific packages, but the rules here apply to the whole repository.

## Product Direction

XIS is a Java web framework for building SPA-style applications with plain Java and plain HTML. The public user story
should stay simple: users write Java controllers, pages/frontlets, and templates, and usually do not need a separate
client build or custom JavaScript.

The end-user documentation in `README.md` and `docs/user/` is the primary public knowledge source. Module README files
are mostly for framework development and internal orientation.

Use `frontlet`, not `widget`, in user-facing documentation, public API explanations, and new tests. Remaining `widget`
names should only exist where legacy compatibility or generated output makes that unavoidable.

Distributed application configuration should stay map-based. Users provide maps such as frontlet hosts, frontlet URLs,
page hosts, and allowed origins; XIS derives remote/local decisions from those maps and caches them in its resolver.
Avoid asking users to implement separate boolean lookup methods, because those lookups may hide expensive discovery.
For page host maps, keys are normalized page URLs such as `/product/*/details.html`, not Java class names.
For distributed SSO, the browser must send XIS token cookies to remote XIS runtimes for both XHR and SSE. The JavaScript
client uses credentialed XHR and opens SSE streams for all configured remote hosts. External OIDC callbacks are normalized
to local XIS tokens, so distributed XIS runtimes must share local token verification keys (for example via a shared
`LocalKeyProvider`/keystore setup) if they should accept cookies issued by the shell. The remote runtime's
`ExternalIDPConfig` still matters when that runtime starts or renews an external login flow itself.

External OpenID Connect login is normalized after the callback: XIS reads the ID token and issues local XIS application
tokens. Without a `UserInfoService`, the OIDC `sub` claim becomes the XIS user id; if the provider access token is a
readable JWT with roles in `realm_access.roles` or `resource_access.account.roles`, those roles are copied into the local
XIS token. Opaque provider tokens, such as common Google access tokens, yield empty local roles unless the application
maps the account through `UserInfoService.saveUserInfo`. When a custom `UserInfoService` exists, XIS saves or updates
user info before issuing the local token; this is for profile storage, approval workflows, or role enrichment.
`UserInfoService.supportsLocalLogin()` controls whether XIS offers the local username/password form. A single external
IDP can redirect directly to the provider when no local login is supported.
Use `@Authenticated` for "authenticated user required, no named role required". Empty `@Roles` remains compatible but
should not be shown in new docs or examples. Missing `@Authenticated`/`@Roles` means public access. Authenticated-only
access is the preferred model for community-login applications such as Google sign-in without app-specific roles.
`@Authenticated` may be placed on controllers/frontlets, methods, parameters, or DTO types. If a controller/frontlet is
already annotated, method-level `@Authenticated` on the same controller is redundant and should not be used as a test
proof for method-level behavior. `@Roles` is for named role requirements and should win when combined with
`@Authenticated`.
The EL functions `isUserInRole(role)` and `isUserInRoles(role, ...)` are browser-side visibility helpers only. They may
be used in `xis:if`, including with separate role arguments or an array, but examples must still protect the server-side
controller, frontlet, action, parameter, or DTO with `@Authenticated`/`@Roles`.
For custom browser interactions that need to submit an existing XIS form, use the public JavaScript helper
`XIS.submitForm(htmlFormId, actionName)`. Do not point users at internal handler APIs such as
`app.tagHandlers.getHandler(...)`.

Simple drag and drop is a public template feature, not a full browser DnD abstraction. Use
`xis:drag="from:${field}"` on the source and `xis:drop="move(from, target='${targetField}')"` on the target. The drag
side publishes one named value; the drop side calls a normal `@Action`. Prefer named action parameters in examples, for
example `@Parameter("from") String from` and `@Parameter("target") String target`. Integration tests should
use `one.xis.test.dom.DragAndDrop`, not hidden `Element` convenience methods.

Modals are public API through `@Modal` and `ModalResponse`. Internally they reuse the frontlet rendering pipeline, but
user-facing docs should present them as modal dialogs, not as a frontlet variant. For dynamic values in HTML, prefer
`xis:modal="EditCustomerModal"` plus nested `xis:parameter`; modal controllers receive those values with
`@Parameter`. E2E coverage should include validation staying inside the modal and `reloadParent()` reloading the
page or exactly the frontlet instance that opened the modal.

For dynamic browser resource attributes, prefer XIS-prefixed attributes such as
`<img xis:src="/img/${file}" alt="">`. XIS removes `xis:src` during initialization and then writes the evaluated value
to the real `src`, avoiding early browser requests for raw template expressions.

## Public Dependencies

Only mention artifacts that end users should normally declare directly:

- `xis-boot`
- `xis-spring`
- `xis-test` only when the application does not use the XIS Gradle plugin and wants the lower-level integration-test API
- `xis-boot-starter-test` only when the application does not use the XIS Gradle plugin and wants `@XisBootTest` or generated-test style
- `xis-authentication`
- `xis-idp-server`
- `xis-theme`
- `xis-distributed`
- `xis-sql`
- `xis-mongodb`

Other modules are internal or transitive unless a specific user-facing reason says otherwise. Do not document internal
SPI modules as user dependencies.
When the XIS Gradle plugin is used, do not tell users to add `xis-test` or `xis-boot-starter-test`; the plugin configures
the matching test dependency.

The XIS Gradle plugin should align versionless `one.xis:*` dependencies to its own version, including Spring apps that
also use Spring dependency management.
For XIS Boot, `xisJar` relies on the annotation processor-generated `one.xis.boot.Runner`; user applications need
exactly one `@XISBootApplication` class so the generated runner can delegate to `XISBootRunner`.
The Gradle plugin also provides `xisRun` for XIS Boot projects. It depends on `xisJar` and starts the generated
jar, so changes to the XIS Boot jar path should keep both tasks aligned.

Unhandled controller exceptions must be logged server-side before they are converted into the `ErrorResponse` that is
sent to the frontend. The frontend error transport is useful for users, but it must not be the only place where a stack
trace is visible during development.

SSE must reconnect on terminal failures and be checked before normal XIS HTTP requests, but failed SSE reconnects must
not block those requests. If `/xis/events` cannot authenticate renewed tokens, it should return `401` and open no stream;
normal page/action/model requests remain responsible for the visible login/forbidden flow. Do not catch unrelated
runtime failures in the SSE authentication path, because real backend errors must stay visible.
Refresh events must not become a lossy channel during short reconnect gaps. `SseService` keeps recently known clients
for a small TTL and queues targeted, user-targeted, and all-client events until the client reconnects. Permanent
disconnects are naturally forgotten after that reconnect window. Keep this TTL defined through
`ClientConfigService.PENDING_EVENT_TTL_SECONDS` so the server queue window and the browser reconnect configuration stay
aligned.

## Documentation Rules

Documentation must support a user who has no forum, examples repository, or external articles to consult. Prefer
working examples over abstract descriptions.

When a feature supports both XIS tags and `xis:*` attributes, show both forms where the distinction matters. Users should
be able to choose XML-like tags or design-tool-friendly attributes without guessing whether one form is supported.

Keep advanced topics separate when they would distract from building a first useful app. Quickstart material should be
compact, but it must go beyond Hello World.

## Testing Rules

When docs contain examples that users are likely to copy, prefer backing them with integration or E2E tests. Avoid
testing every internal detail twice, but cover public behavior and both supported template syntaxes where regressions are
plausible.

Use E2E tests for browser-visible behavior, distributed app behavior, authentication redirects, and cases where the real
client/runtime interaction matters. Use `xis-integration-tests` for fast framework-level behavior and custom JavaScript
runtime coverage.

## Release Rules

Release ZIP deployment is intentionally kept for now. `createReleaseZip` should publish fresh local artifacts for the
current release coordinates, avoid stale `~/.m2` contents, include Gradle plugin marker artifacts, and regenerate
checksums/signatures.

Do not change the release mechanism just because ZIP upload feels old-fashioned. Replacing it already cost time and is
not currently the goal.

## Open To-dos

- Consider adding navigation and a small frontlet example to the Quickstart if that improves the "read and build along"
  experience without making the guide too long.
- Keep checking that public annotations are documented in `docs/user/` and have useful Javadoc.
