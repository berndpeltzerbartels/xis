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

Distributed application configuration is host-list based. Users provide remote hosts through
`XisDistributedConfig#getHosts()` or the `xis.distributed.hosts` property. The browser loads `/xis/config` from those
hosts, merges the remote client configs, and derives remote page/frontlet routing from the merged metadata. Do not
reintroduce server-side page/frontlet host maps such as frontlet hosts, frontlet URLs, or page hosts.
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
example `@ActionParameter("from") String from` and `@ActionParameter("target") String target`. Integration tests should
use `one.xis.test.dom.DragAndDrop`, not hidden `Element` convenience methods.

Modals are public API through `@Modal` and `ModalResponse`. Internally they reuse the frontlet rendering pipeline, but
user-facing docs should present them as modal dialogs, not as a frontlet variant. For dynamic values in HTML, prefer
`xis:modal="EditCustomerModal"` plus nested `xis:parameter`; modal controllers receive those values with
`@ModalParameter`. E2E coverage should include validation staying inside the modal and `reloadParent()` reloading the
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
runtime coverage. Distributed routing changes should run the distributed E2E suite, because the behavior spans Boot
runtimes, the host-list endpoint, client config merging, CORS, page routing, and frontlet routing.

## Release Rules

Release ZIP deployment is intentionally kept for now. `createReleaseZip` should publish fresh local artifacts for the
current release coordinates, avoid stale `~/.m2` contents, include Gradle plugin marker artifacts, and regenerate
checksums/signatures.

Do not change the release mechanism just because ZIP upload feels old-fashioned. Replacing it already cost time and is
not currently the goal.

Create the feature branch as soon as the work topic is known. Do not keep topic work on `develop`, `main`, or a release
branch while deciding what to do with it.

After a feature branch has been merged into `develop`, delete that feature branch locally and remotely unless there is
an explicit reason to keep it. Keeping merged topic branches around creates avoidable archaeology later, especially
after squash merges make commit identity a poor signal for whether the work is already contained.

Do not treat uncommitted work in the active branch as a cautious holding area. It is dangerous because local experiments
then run against a different state than the one that can be pushed, merged, or released. When work is created, stage it
promptly; commits should normally cover the whole repository state instead of hand-picked partial changes. Only make
partial commits when that has been explicitly agreed for the current task. If the tree is puzzling or dirty, clarify the
state before continuing rather than building release assumptions on uncommitted files.

A release candidate always starts from an up-to-date `develop`, not from a feature branch. Before creating a release
branch, make sure all intended feature branches have been merged into `develop`; any exception must be explicit. Do not
declare a feature branch to be the release candidate by convenience.

The release flow is:

1. Commit the current feature branch as one whole-project state and push it, even if the push may become redundant
   because the branch is merged immediately afterwards.
2. Update `develop`, merge the feature branch into `develop`, push `develop`, and delete the merged feature branch
   locally and remotely. Local `develop` and `origin/develop` should not diverge.
3. Inspect local and remote branches for any remaining unmerged work branches. Merge and delete all intended work
   branches before continuing, or explicitly record why a branch is excluded. Do not create the release branch until
   this check is complete.
4. Create the release branch directly from `develop` and push it immediately. Local and remote release branches should
   not diverge.
5. Verify that the intended release version is set everywhere the release uses it, then run `./gradlew clean build
   publishToMavenLocal`.
6. Run the full available test suite without waiting for a special reminder. Phrases such as "full test" mean all
   framework, integration, JavaScript, plugin, and end-to-end tests that are available for the release. This includes
   manually started E2E repositories that are not wired into the main Gradle build, currently including
   `/Users/bernd/projects/xis-end-to-end-tests`, `/Users/bernd/projects/xis-chess-example-simple-e2e`, and
   `/Users/bernd/projects/xis-crm-example-e2e` when they exist locally.
7. Build the release ZIP from that confirmed release branch. The ZIP task also runs the system tests that verify the
   supported platform variants.
8. If any failure requires a code change, rerun the test that found the failure and every release check that had already
   succeeded before that change. In the end, the ZIP must represent a state that passed the full release check sequence
   without later code changes invalidating earlier results.
9. Bring the confirmed release state to `main` with a squash merge.
10. Upload the release ZIP.
11. Bump `main` to the next development version.
12. Make `develop` match `main` for the next cycle. If rewriting or recreating `develop` could lose useful history,
    create a backup branch first or keep the existing history deliberately. `develop` is public too.

After a correct release, the only intended difference between the release branch and `main` is the post-release version
change on `main`. There should be no intended difference between `main` and `develop` after `develop` has been prepared
for the next cycle.

## Open To-dos

- Consider adding navigation and a small frontlet example to the Quickstart if that improves the "read and build along"
  experience without making the guide too long.
- Keep checking that public annotations are documented in `docs/user/` and have useful Javadoc.
