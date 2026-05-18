# Security

[Documentation map](../README.md)

XIS security is role-based. Add `xis-authentication` when pages or actions should require a login. The normal page,
frontlet, action, form, and navigation APIs stay the same.

`build.gradle` for XIS Boot:

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.11.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
    implementation "one.xis:xis-authentication"
}
```

`build.gradle` for Spring:

```groovy
plugins {
    id "java"
    id "org.springframework.boot" version "3.3.0"
    id "io.spring.dependency-management" version "1.1.5"
    id "one.xis.plugin" version "0.11.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "org.springframework.boot:spring-boot-starter-web"
    implementation "one.xis:xis-spring"
    implementation "one.xis:xis-authentication"
}
```

## Local Authentication

For a single application with local users, provide a `UserInfoService`. XIS uses it to validate credentials and load the
user roles. In a XIS Boot application this can be a XIS component. In a Spring application it must be a Spring bean,
because `UserInfoService` implementations are imported from the host framework.

`saveUserInfo` is mainly used by XIS itself after an external OpenID Connect login. If the application provides a custom
`UserInfoService`, XIS decodes the external `id_token`, calls `getUserInfo`, and then calls `saveUserInfo` for new or
updated users before it issues its own local application token. Use that method to map community accounts to local
application accounts, store approval state, or assign application roles. You may also call it from your own user
management code, but it is not only a "manual save" hook.

`UserInfoImpl` is shaped for this handoff. Besides the required local `userId` and optional roles, it contains common
OpenID Connect profile fields such as `email`, `name`, `preferredUsername`, `givenName`, `familyName`, `locale`, and
`pictureUrl`. XIS copies those values from the external provider's `id_token` into `UserInfoImpl` before calling
`saveUserInfo`, so an application can persist Google, Keycloak, or XIS-IDP profile attributes without defining a custom
user class first. Local XIS tokens still use only the stable user id and roles.

```java
package example.security;

import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;
import one.xis.context.Component;

import java.util.Optional;
import java.util.Set;

@Component
class AppUsers implements UserInfoService<UserInfo> {

    @Override
    public boolean validateCredentials(String userId, String password) {
        return userId.equals("alice") && password.equals("secret");
    }

    @Override
    public Optional<UserInfo> getUserInfo(String userId) {
        if (!userId.equals("alice")) {
            return Optional.empty();
        }
        var user = new UserInfoImpl();
        user.setUserId("alice");
        user.setPreferredUsername("alice");
        user.setRoles(Set.of("USER"));
        return Optional.of(user);
    }

    @Override
    public void saveUserInfo(UserInfo userInfo) {
        throw new UnsupportedOperationException();
    }
}
```

The same service in a Spring application uses the Spring stereotype instead:

```java
package example.security;

import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoService;
import org.springframework.stereotype.Component;

@Component
class AppUsers implements UserInfoService<UserInfo> {
    // same methods as above
}
```

When an unauthenticated user opens a protected page, XIS returns a `401` response with a `Location` header. The browser
client follows that location and opens the login page. With local authentication the target is:

```text
/login.html?redirect_uri=...
```

After a successful login, XIS redirects back to the original page.

## Custom Login Template

The login page is a normal XIS template. The only special part is that XIS already provides the controller, form object,
and action behind it. Add a `login.html` resource to the application classpath to override the framework default
template.

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head><title>Login</title></head>
<body>
<form xis:binding="login">
    <h1>Sign in</h1>
    <xis:global-messages/>

    <label for="username" xis:binding="username" xis:error-class="error">Username</label>
    <input xis:binding="username" id="username" type="text"/>
    <xis:message message-for="username"/>

    <label for="password" xis:binding="password" xis:error-class="error">Password</label>
    <input xis:binding="password" id="password" type="password"/>
    <xis:message message-for="password"/>

    <input xis:binding="state" type="hidden"/>
    <button xis:action="login" type="submit">Login</button>
</form>
</body>
</html>
```

The form binding, field bindings, hidden `state`, and `login` action name belong to the framework contract. The
surrounding markup, labels, layout, CSS classes, and text are application design.

Local login uses normal XIS validation. The built-in login form object is annotated with `@Login`; when credentials are
wrong, the login action is not executed and a global validation message is returned. Render it with
`<xis:global-messages/>` inside the `login` form. Field messages and error highlighting use the same tags and attributes
as any other form: `xis:message-for`, `xis:error-class`, `xis:error-style`, and `xis:error-binding`.

`<xis:global-messages/>` always renders a list structure when messages exist, even if there is only one message. This
keeps the DOM shape predictable for templates and CSS. If the login page should look like a single alert, style the
generated `ul.error` and `li.error` without bullets.

This is also the pattern for reusable application libraries. A library can provide controllers and default templates as
classpath resources, while applications keep the option to replace those templates with resources of the same name. The
login page is simply the built-in example of that mechanism.

When optional login factors are active, the default login page renders their fields automatically. A custom `login.html`
must render the matching field itself. For TOTP, add a field bound to `totpCode` and guard it with `totpLoginEnabled`:

```html
<div xis:if="${totpLoginEnabled}">
    <label for="totpCode" xis:binding="totpCode" xis:error-class="error">Authenticator code</label>
    <input xis:binding="totpCode" id="totpCode" type="text" inputmode="numeric" autocomplete="one-time-code"/>
    <xis:message message-for="totpCode"/>
</div>
```

If the application also offers external OpenID Connect providers, the login controller exposes `externalIdpIds` and
`externalIdpUrls`. A custom template can render them next to the local login form:

```html
<div xis:repeat="idpId:externalIdpIds">
    <a href="${externalIdpUrls[idpId]}">${idpId}</a>
</div>
```

## Login Variants

### Local Authentication Only

Provide a real `UserInfoService`. When a protected page is opened without a valid login, XIS redirects to:

```text
/login.html?redirect_uri=...
```

The login page renders the local form. A custom `login.html` only needs the `login` form binding, the `username`,
`password`, and hidden `state` fields, and the `login` action.

### Local Authentication And One External OpenID Connect Provider

Provide a real `UserInfoService` and one `ExternalIDPConfig`. When a protected page is opened without a valid login, XIS
still redirects to `/login.html` instead of redirecting directly to the provider.

The login page renders the local form and one provider link. A custom `login.html` should render both the local form and
the `externalIdpIds` / `externalIdpUrls` provider link.

### Local Authentication And Multiple External OpenID Connect Providers

Provide a real `UserInfoService` and multiple `ExternalIDPConfig` instances. When a protected page is opened without a
valid login, XIS redirects to `/login.html`.

The login page renders the local form and one link per provider. A custom template should render the local form and loop
over `externalIdpIds`, using `externalIdpUrls[idpId]` as the link target.

### One External OpenID Connect Provider Without Local Authentication

Provide one `ExternalIDPConfig` and either no `UserInfoService`, or a `UserInfoService` whose `supportsLocalLogin()`
method returns `false`. XIS then redirects directly to that provider when a protected page is opened without a valid
login.

`/login.html` is normally skipped in this setup. If it is opened explicitly, the local form is not rendered because the
active `UserInfoService` does not support local credentials.

### Multiple External OpenID Connect Providers Without Local Authentication

Provide multiple `ExternalIDPConfig` instances and either do not provide a custom `UserInfoService`, or provide one whose
`supportsLocalLogin()` method returns `false`. When a protected page is opened without a valid login, XIS redirects to
`/login.html` so the user can choose the provider.

The login page renders only provider links. A custom `login.html` must render `externalIdpIds` and `externalIdpUrls`; it
should not show a local username/password form unless the application also provides a `UserInfoService` with
`supportsLocalLogin() == true`.

`UserInfoService` is optional when the application only uses external providers. XIS reads the OpenID Connect `id_token`
after the callback and issues its own local application token. If no `UserInfoService` is present, the OpenID Connect
`sub` claim becomes the XIS user id. If the provider access token is a readable JWT with roles in `realm_access.roles`
or `resource_access.account.roles`, XIS copies those roles into the local token. Providers such as Google usually issue
access tokens for their own APIs instead; in that case the local token has no named roles unless the application maps
the account through a `UserInfoService`.

## TOTP Two-Factor Login

Add `xis-totp` when local username/password login should support authenticator-app codes:

```groovy
dependencies {
    implementation "one.xis:xis-authentication"
    implementation "one.xis:xis-totp"
}
```

The module is optional. If it is present, XIS validates the application at startup:

- exactly one `TOTPStore` implementation must exist
- `xis.totp.encryption-key` must be configured

The store belongs to the application because only the application knows where user security data should live. XIS never
hands raw authenticator secrets to the store. It encrypts a generated Base32 secret first and stores only the encrypted
value.

```java
package example.security;

import one.xis.context.Component;
import one.xis.totp.TOTPStore;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

@Component
class AppTotpStore implements TOTPStore {

    private final ConcurrentHashMap<String, String> secrets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> acceptedSteps = new ConcurrentHashMap<>();

    @Override
    public Optional<String> getEncryptedSecret(String userId) {
        return Optional.ofNullable(secrets.get(userId));
    }

    @Override
    public void saveEncryptedSecret(String userId, String encryptedSecret) {
        secrets.put(userId, encryptedSecret);
    }

    @Override
    public OptionalLong getLastAcceptedTimeStep(String userId) {
        Long step = acceptedSteps.get(userId);
        return step == null ? OptionalLong.empty() : OptionalLong.of(step);
    }

    @Override
    public void saveLastAcceptedTimeStep(String userId, long timeStep) {
        acceptedSteps.put(userId, timeStep);
    }
}
```

Configure a stable encryption key and, optionally, the issuer shown in authenticator apps:

```properties
xis.totp.encryption-key=replace-with-a-stable-secret-from-your-environment
xis.totp.issuer=Example CRM
```

When a user has no stored TOTP secret, password login stays password-only. When a secret exists, the login validator
requires the `totpCode` field before it creates the local login code. If the store persists the last accepted time step,
XIS rejects a repeated code from the same 30-second window.

`xis-totp` also contributes an HTTP controller for provisioning:

```text
/xis/totp/qr.svg
```

The endpoint requires an authenticated user. It creates a secret for that user if none exists yet, stores the encrypted
secret through `TOTPStore`, and returns an SVG QR code containing the standard `otpauth://` URI. Applications can show
that SVG on their own account/security page.

`xis-totp` also provides `/totp-setup.html` as a default setup page. It uses the same basic CSS hooks as the default
login page (`container`, `form_container`, `form-group`, `form-control`, `btn`, `btn-primary`) plus TOTP-specific hooks
such as `totp-setup-container`, `totp-setup-form`, `totp-setup-error`, `totp-setup-qr-code`, and
`totp-setup-login-link`. Applications can usually style the built-in page with CSS; add a `totp-setup.html` resource only
when the page structure itself should be replaced.

## Page And Action Roles

Use `@Authenticated` when a page, frontlet, action method, action parameter, or action DTO requires a login but no named role. Use
`@Roles` when at least one named application role is required.

`@Authenticated` is a good fit for community areas, profile pages, carts, or applications where a login is enough:

```java
@Page("/community.html")
@Authenticated
class CommunityPage {
}
```

```java
package example.security;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;
import one.xis.UserId;

@Page("/account.html")
@Roles("USER")
class AccountPage {

    @ModelData("message")
    String message(@UserId String userId) {
        return "Account for " + userId;
    }

    @Action
    @Roles("ADMIN")
    void rebuildIndex() {
        adminService.rebuildIndex();
    }
}
```

Role checks are hierarchical:

- controller level: login from `@Authenticated`, or at least one role from the controller/frontlet `@Roles`
- method level: login from `@Authenticated`, or at least one role from the method `@Roles`, when present
- parameter/DTO level: login from `@Authenticated` on the parameter or parameter type, or at least one role from a
  `@Roles`-annotated action parameter type, when present

All annotated levels must match. Within one role level, alternatives are allowed.

A missing `@Authenticated` or `@Roles` annotation means public access. A legacy empty `@Roles` annotation still means
authenticated access, but new code should use `@Authenticated` because it states the intent directly.

## Role-based Visibility In Templates

Use `isUserInRole(role)` or `isUserInRoles(role, ...)` in `xis:if` when the UI should only show a control to users with
matching roles:

```html
<button xis:if="isUserInRole('ADMIN')" xis:action="rebuildIndex">Rebuild index</button>
<a xis:if="isUserInRoles('SUPPORT', 'ADMIN')" xis:page="/support.html">Support</a>
```

`isUserInRoles(...)` accepts either separate role arguments or an array expression:

```html
<button xis:if="isUserInRoles(visibleRoles)" xis:action="moderate">Moderate</button>
```

This is frontend code. A hidden button is not the same as a protected action, because a user can still call endpoints or
modify browser code. Always put `@Roles` or `@Authenticated` on the controller, frontlet, action method, action
parameter, or DTO that actually requires protection.

## DTO Roles

`@Authenticated` can be put on an individual action parameter when that parameter should only be deserialized for logged-in
users. `@Roles` belongs on a DTO type used by an action parameter.

```java
@Action
void rememberReturnUrl(@Authenticated @FormData("returnUrl") String returnUrl) {
    navigationService.remember(returnUrl);
}
```

```java
package example.security;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;
import one.xis.Roles;

@Page("/editor.html")
@Roles("USER")
class EditorPage {

    @Action
    void save(@FormData("article") ArticleForm article) {
        articleService.save(article);
    }
}
```

```java
package example.security;

import one.xis.Roles;

@Roles("DATA_EDITOR")
record ArticleForm(String title, String body) {
}
```

The `save` action requires both `USER` and `DATA_EDITOR`: `USER` from the page and `DATA_EDITOR` from the DTO.

## Ownership Checks

Use `@OwnedBy` when submitted form/action data references an object that must belong to the currently authenticated
user. XIS runs the configured `OwnershipGuard` after the object has been deserialized and before the action method is
called. The guard receives the submitted object and the trusted `UserContext`; the application owns the actual lookup or
policy decision.

```java
package example.security;

import one.xis.Action;
import one.xis.FormData;
import one.xis.OwnedBy;
import one.xis.OwnershipGuard;
import one.xis.Page;
import one.xis.Roles;
import one.xis.UserContext;
import one.xis.context.Component;

@Component
class CustomerOwnershipGuard implements OwnershipGuard<CustomerForm> {

    private final CustomerService customerService;

    CustomerOwnershipGuard(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public boolean mayAccess(CustomerForm form, UserContext userContext) {
        return customerService.customerBelongsToUser(form.customerId(), userContext.getUserId());
    }
}

@Page("/customers.html")
@Roles("USER")
class CustomerPage {

    @Action
    void save(@FormData("customer") CustomerForm form) {
        customerService.save(form);
    }
}

@OwnedBy(CustomerOwnershipGuard.class)
record CustomerForm(String customerId, String name) {
}
```

In Spring applications, make the guard a Spring bean, for example with `org.springframework.stereotype.Component`,
instead of `one.xis.context.Component`.

Ownership violations behave like role violations. The action is not called. In a frontend request XIS turns the
security failure into the same login redirect flow used by `@Roles` and `@Authenticated`, so the browser opens the login
page with the current page as `redirect_uri`.

`@OwnedBy` can be placed on the DTO class or on the concrete action parameter:

```java
@Action
void save(@OwnedBy(CustomerOwnershipGuard.class)
          @FormData("customer") CustomerForm form) {
    customerService.save(form);
}
```

Nested objects are checked individually while they are deserialized. If a nested object or field has its own `@OwnedBy`,
that nested object must also pass its guard.

XIS does not cache ownership decisions. The framework does not know which fields identify the protected resource, and
all other submitted values may legally change. If a guard needs caching, keep that cache inside the application code
where the stable resource id and operation are known.

## External IDP

External identity providers are supported through OpenID Connect. XIS uses the provider discovery document at
`/.well-known/openid-configuration`, the authorization code flow, the token endpoint, and the provider JWKS endpoint.
Other login protocols such as SAML are not supported.

Provide one or more `ExternalIDPConfig` instances.

```java
package example.security;

import one.xis.auth.idp.ExternalIDPConfig;
import one.xis.context.Component;

@Component
class KeycloakLogin implements ExternalIDPConfig {

    @Override
    public String getIdpId() {
        return "keycloak";
    }

    @Override
    public String getIdpServerUrl() {
        return "http://localhost:8080/realms/xis";
    }

    @Override
    public String getClientId() {
        return "xis-app";
    }

    @Override
    public String getClientSecret() {
        return "change-me";
    }
}
```

In a Spring application the same class should be a Spring bean:

```java
package example.security;

import one.xis.auth.idp.ExternalIDPConfig;
import org.springframework.stereotype.Component;

@Component
class KeycloakLogin implements ExternalIDPConfig {
    // same methods as above
}
```

If the application has exactly one external IDP and no local login, XIS redirects directly to that IDP login URL. If the
application has local authentication or multiple external IDPs, XIS uses `/login.html` so the user can choose or use the
local login form.

`@UserId` receives the OpenID Connect `sub` claim for external providers. That value is usually a stable provider id,
not necessarily the visible login name or email address.

After the callback, XIS always issues its own local application token. With no custom `UserInfoService`, XIS uses the
external `sub` as the local user id. If the provider access token is a readable JWT with roles in `realm_access.roles` or
`resource_access.account.roles`, those roles are copied into the local XIS token. If the provider token is opaque or does
not carry application roles, protect pages with `@Authenticated` or provide a `UserInfoService` that maps the external user
to local roles in `saveUserInfo`.

By default XIS requests the `openid` scope. Providers may require additional scopes for role claims. Override
`getScope()` in `ExternalIDPConfig` when the provider needs them:

```java
@Override
public String getScope() {
    return "openid roles";
}
```

### Keycloak

For Keycloak, create a realm and a confidential OpenID Connect client. The client must allow this redirect URI:

```text
http://localhost:8080/xis/auth/callback/keycloak
```

Use the actual application origin instead of `http://localhost:8080` and the same provider id that `getIdpId()` returns
instead of `keycloak`.

The Keycloak issuer URL is the realm URL:

```text
http://localhost:8080/realms/xis
```

That is the value for `getIdpServerUrl()`. XIS reads the discovery document from:

```text
http://localhost:8080/realms/xis/.well-known/openid-configuration
```

For local development, Keycloak can be started in Docker and import a realm on startup. Mount the realm export into
`/opt/keycloak/data/import` and start Keycloak with `start-dev --import-realm`.

Keycloak publishes the standard OpenID Connect discovery document, so `ExternalIDPConfig` is enough. For role-protected
pages, assign realm roles such as `USER` to the Keycloak user and request a scope that includes role claims, for example
`openid roles`.

### Google

Google publishes an OpenID Connect discovery document. Use this issuer URL:

```text
https://accounts.google.com
```

Configure the Google OAuth client as a web application and add the XIS callback URL as an authorized redirect URI:

```text
http://localhost:8080/xis/auth/callback/google
```

Use the actual application origin instead of `http://localhost:8080` and the same provider id that `getIdpId()` returns
instead of `google`.

Google's token response contains an `id_token`, which is the OpenID Connect identity JWT. The `access_token` is meant for
Google APIs. XIS therefore reads the Google `id_token` after the callback and issues its own application token. For a
simple community login, no `UserInfoService` is required: the Google `sub` claim becomes the XIS user id and the local
token has no named roles.

Use a `UserInfoService` only when the application wants to store or enrich the Google user, for example for an approval
workflow, profile data, or application roles. Use a concrete `UserInfo` implementation such as `UserInfoImpl` as the
generic type. During the callback, XIS copies Google profile claims from the `id_token` into that object before
`saveUserInfo` is called.

```java
package example.security;

import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;
import one.xis.context.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
class GoogleUsers implements UserInfoService<UserInfoImpl> {

    private final Map<String, UserInfoImpl> users = new ConcurrentHashMap<>();

    @Override
    public boolean supportsLocalLogin() {
        return false;
    }

    @Override
    public boolean validateCredentials(String userId, String password) {
        return false;
    }

    @Override
    public Optional<UserInfoImpl> getUserInfo(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void saveUserInfo(UserInfoImpl userInfo) {
        users.put(userInfo.getUserId(), userInfo);
    }
}
```

For a real application, replace the in-memory map with your user database or approval workflow. `supportsLocalLogin()`
returns `false` here because Google is the only login method; XIS should not render a local username/password form. If
the application only needs a community login, leave the `UserInfoService` out and protect pages with `@Authenticated`. If
the application needs named roles, provide a `UserInfoService` and assign roles in `saveUserInfo`. XIS calls
`saveUserInfo` during the Google callback before it creates the local XIS token, so role assignments made there are part
of the token used for the redirected page.

The client id and client secret from Google are returned by `getClientId()` and `getClientSecret()`. For normal Google
sign-in, request:

```java
@Override
public String getScope() {
    return "openid profile email";
}
```

Google also uses the standard OpenID Connect discovery document.

## XIS As An OpenID Connect Provider

XIS can also run as an OpenID Connect provider. Use this setup when one application should own authentication and issue
tokens for other applications.

This is useful when authentication is more than a simple password check. For example, an account may need a
pre-registration step, documents may need to be checked, or a human reviewer may need to approve the account before the
user can log in. In that case the IDP application can own the registration and approval workflow. The consuming
applications only need to trust the tokens issued by the XIS IDP.

It is also useful for SSO across multiple applications. Several XIS applications, or applications written in other
languages, can use the same XIS IDP as their OpenID Connect provider. The application-specific login logic stays in one
place, while every client application receives standard access, refresh, and ID tokens.

Another reason is token normalization. The IDP application decides how users are authenticated and which claims are
issued. Client applications then see one predictable token format from the XIS IDP instead of having to know the
application-specific account model behind it.

To implement a XIS IDP, add `xis-idp-server` and provide an `IDPService`. The service is responsible for:

- validating user credentials
- returning user information
- returning access-token and ID-token claims
- registering allowed clients through `IDPClientInfo`
- validating client secrets

In a Spring application, the service can be a Spring bean. In a XIS Boot application, use a XIS component instead.

```java
package example.idp;

import one.xis.auth.AccessTokenClaims;
import one.xis.auth.IDPClientInfo;
import one.xis.auth.IDPClientInfoImpl;
import one.xis.auth.IDPService;
import one.xis.auth.IDPUserInfo;
import one.xis.auth.IDPUserInfoImpl;
import one.xis.auth.IDTokenClaims;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
class AppIDPService implements IDPService {

    private final Map<String, String> passwords = Map.of("alice", "secret");
    private final Map<String, IDPClientInfo> clients = Map.of(
            "orders-app",
            new IDPClientInfoImpl(
                    "orders-app",
                    "orders-secret",
                    Set.of("http://localhost:8080/xis/auth/callback/xis-idp")
            )
    );

    @Override
    public boolean validateCredentials(String username, String password) {
        return Objects.equals(passwords.get(username), password);
    }

    @Override
    public Optional<IDPUserInfo> userInfo(String userId) {
        if (!passwords.containsKey(userId)) {
            return Optional.empty();
        }
        return Optional.of(new IDPUserInfoImpl(userId, "orders-app"));
    }

    @Override
    public Optional<AccessTokenClaims> accessTokenClaims(String userId) {
        if (!passwords.containsKey(userId)) {
            return Optional.empty();
        }
        var claims = new AccessTokenClaims();
        claims.setUsername(userId);
        claims.setRoles(List.of("USER"));
        return Optional.of(claims);
    }

    @Override
    public Optional<IDTokenClaims> idTokenClaims(String userId) {
        if (!passwords.containsKey(userId)) {
            return Optional.empty();
        }
        var claims = new IDTokenClaims();
        claims.setPreferredUsername(userId);
        return Optional.of(claims);
    }

    @Override
    public Optional<IDPClientInfo> findClientInfo(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    @Override
    public boolean validateClientSecret(String clientId, String clientSecret) {
        return findClientInfo(clientId)
                .map(client -> Objects.equals(client.getClientSecret(), clientSecret))
                .orElse(false);
    }
}
```

`IDPUserInfo` connects the authenticated user to the client application. `IDPClientInfo` registers a client id, client
secret, and the exact callback URLs that may receive authorization codes. The consuming XIS application uses the matching
client id, client secret, and callback URL in its `ExternalIDPConfig`.

`accessTokenClaims` and `idTokenClaims` only need to fill the application-specific claims, such as username, display
name, email, and roles. XIS fills the technical token fields such as `sub`, `iss`, `iat`, `exp`, `nbf`, and `client_id`
when the token is issued.

The IDP publishes the OpenID Connect discovery document, JWKS, login page, and token endpoint. Client applications then
configure the XIS IDP like any other external OpenID Connect provider by using `ExternalIDPConfig` and the IDP base URL.

## SSO In Distributed XIS Applications

Distributed XIS applications can share one login across a shell application and remote XIS runtimes. The browser logs in
at one application, receives the XIS `access_token` and `refresh_token` cookies, and sends those cookies to the remote XIS
runtime when a remote page, frontlet, action, or SSE event stream is used.

After an external OpenID Connect callback, XIS issues local XIS tokens. That is intentional: the application can map or
enrich external identities once and then work with one XIS token shape. In a distributed XIS application, every
participating XIS runtime that should accept the same login must therefore be able to validate those local XIS tokens.
Use a shared `LocalKeyProvider` setup, for example a shared keystore or mounted key material, instead of the default
in-memory development keys.

For XIS as an IDP, this is the intended setup:

- the shell application uses `xis-authentication` and `xis-idp-client`
- each remote XIS application also uses `xis-authentication` and configures the same XIS IDP issuer
- distributed routing comes from `xis-distributed`
- remote hosts are listed in the distributed configuration so XIS opens remote XIS requests and remote SSE connections
- participating XIS applications use shared local token keys so they can validate the XIS cookies issued by the shell

The remote application does not receive the shell application's server-side state. It receives the same token cookies and
validates the XIS token signature locally. Its `ExternalIDPConfig` is still needed when the remote application may start
or renew an external login flow itself.

For Keycloak, use the same principle. Every participating XIS application must be able to validate the same Keycloak
issuer and then issue or accept local XIS tokens consistently. If an application should use Keycloak roles directly, the
Keycloak access token must be a JWT with the role claims XIS expects. Configure redirect URIs for applications that may
initiate login directly.

For different domains, remember normal browser cookie rules. The browser only sends cookies to hosts for which the cookie
is valid. Same-site local development such as different `localhost` ports works naturally; separate production domains
may require a shared parent domain and matching cookie configuration. If that is not possible, use a central XIS IDP or
another broker pattern and keep application domains planned around browser cookie rules.

The built-in IDP login flow is still a XIS login flow backed by `IDPService`. If the IDP application needs a more complex
interactive process, such as multi-step registration or manual account approval, model that process in the IDP
application and allow credential validation only after the account is ready.
