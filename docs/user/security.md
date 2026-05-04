# Security

XIS security is role-based. Add `xis-authentication` to the application runtime when pages or actions should require a
login. The normal page, frontlet, action, form, and navigation APIs stay the same.

## Local Authentication

For a single application with local users, provide a `UserInfoService`. XIS uses it to validate credentials and load the
user roles. In a XIS Boot application this can be a XIS component. In a Spring application it must be a Spring bean,
because `UserInfoService` implementations are imported from the host framework.

```java
package example.security;

import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;
import one.xis.context.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class AppUsers implements UserInfoService<UserInfo> {

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
public class AppUsers implements UserInfoService<UserInfo> {
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

The login controller is part of `xis-authentication`, but the HTML template is intentionally replaceable. Add a
`login.html` resource to the application classpath to override the framework default template.

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head><title>Login</title></head>
<body>
<form xis:binding="login">
    <h1>Sign in</h1>
    <label for="username">Username</label>
    <input xis:binding="username" id="username" type="text"/>

    <label for="password">Password</label>
    <input xis:binding="password" id="password" type="password"/>

    <input xis:binding="state" type="hidden"/>
    <button xis:action="login" type="submit">Login</button>
</form>
</body>
</html>
```

The form binding, field bindings, hidden `state`, and `login` action name belong to the framework contract. The
surrounding markup, labels, layout, CSS classes, and text are application design.

If the application also offers external OpenID Connect providers, the login controller exposes `externalIdpIds` and
`externalIdpUrls`. A custom template can render them next to the local login form:

```html
<div xis:repeat="idpId:externalIdpIds">
    <a href="${externalIdpUrls[idpId]}">${idpId}</a>
</div>
```

## Login Variants

### Local Authentication Only

Use `xis-authentication` and provide a real `UserInfoService`. `xis-idp-client` is not needed. When a protected page is
opened without a valid login, XIS redirects to:

```text
/login.html?redirect_uri=...
```

The login page renders the local form. A custom `login.html` only needs the `login` form binding, the `username`,
`password`, and hidden `state` fields, and the `login` action.

### Local Authentication And One External OpenID Connect Provider

Use `xis-authentication`, provide a real `UserInfoService`, add `xis-idp-client`, and provide one `ExternalIDPConfig`.
When a protected page is opened without a valid login, XIS still redirects to `/login.html` instead of redirecting
directly to the provider.

The login page renders the local form and one provider link. A custom `login.html` should render both the local form and
the `externalIdpIds` / `externalIdpUrls` provider link.

### Local Authentication And Multiple External OpenID Connect Providers

Use `xis-authentication`, provide a real `UserInfoService`, add `xis-idp-client`, and provide multiple
`ExternalIDPConfig` instances. When a protected page is opened without a valid login, XIS redirects to `/login.html`.

The login page renders the local form and one link per provider. A custom template should render the local form and loop
over `externalIdpIds`, using `externalIdpUrls[idpId]` as the link target.

### One External OpenID Connect Provider Without Local Authentication

Use `xis-authentication` and `xis-idp-client`, provide one `ExternalIDPConfig`, and do not provide a custom
`UserInfoService`. XIS then redirects directly to that provider when a protected page is opened without a valid login.

`/login.html` is normally skipped in this setup. If it is opened explicitly, the local form is not rendered because the
framework placeholder `UserInfoService` does not validate local credentials.

### Multiple External OpenID Connect Providers Without Local Authentication

Use `xis-authentication` and `xis-idp-client`, provide multiple `ExternalIDPConfig` instances, and do not provide a
custom `UserInfoService`. When a protected page is opened without a valid login, XIS redirects to `/login.html` so the
user can choose the provider.

The login page renders only provider links. A custom `login.html` must render `externalIdpIds` and `externalIdpUrls`; it
should not show a local username/password form unless the application also provides a real `UserInfoService`.

`UserInfoService` is optional when the application only uses external providers. XIS provides a default placeholder that
does not validate local credentials and does not render the local login form.

## Page And Action Roles

Use `@Roles` on pages, frontlets, and action methods.

```java
package example.security;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.Roles;
import one.xis.UserId;

@Page("/account.html")
@Roles("USER")
public class AccountPage {

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

- controller level: at least one role from the controller/frontlet `@Roles`
- method level: at least one role from the method `@Roles`, when present
- DTO level: at least one role from a `@Roles`-annotated action parameter type, when present

All non-empty levels must match. Within one level, alternatives are allowed.

## DTO Roles

`@Roles` cannot be put on individual parameters. It can be put on a DTO type used by an action parameter.

```java
package example.security;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;
import one.xis.Roles;

@Page("/editor.html")
@Roles("USER")
public class EditorPage {

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
public record ArticleForm(String title, String body) {
}
```

The `save` action requires both `USER` and `DATA_EDITOR`: `USER` from the page and `DATA_EDITOR` from the DTO.

## External IDP

External identity providers are supported through OpenID Connect. XIS uses the provider discovery document at
`/.well-known/openid-configuration`, the authorization code flow, the token endpoint, and the provider JWKS endpoint.
Other login protocols such as SAML are not supported by this module.

Add `xis-idp-client` in addition to `xis-authentication`, then provide one or more `ExternalIDPConfig` instances.

```java
package example.security;

import one.xis.auth.idp.ExternalIDPConfig;
import one.xis.context.Component;

@Component
public class KeycloakLogin implements ExternalIDPConfig {

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
public class KeycloakLogin implements ExternalIDPConfig {
    // same methods as above
}
```

If the application has exactly one external IDP and no custom `UserInfoService`, XIS redirects directly to that IDP login
URL. If the application has local authentication or multiple external IDPs, XIS uses `/login.html` so the user can choose
or use the local login form. The provider must issue JWT access tokens that contain the user id in `sub` and roles in
`realm_access.roles` or `resource_access.account.roles`.

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

No Keycloak-specific XIS artifact is needed. Keycloak publishes the standard OpenID Connect discovery document, so
`ExternalIDPConfig` is enough.

### Google

Google can also be used as an external OpenID Connect provider. Configure the Google OAuth client as a web application,
add the XIS callback URL as an authorized redirect URI, and use the Google issuer URL:

```text
https://accounts.google.com
```

The client id and client secret from Google are returned by `getClientId()` and `getClientSecret()`. Google roles are not
application roles; for role-based pages and actions, map the authenticated user to application roles in your own user
management or use a provider that emits role claims in the access token.

No Google-specific XIS artifact is needed. Google also uses the standard OpenID Connect discovery document.

## XIS As An OpenID Connect Provider

XIS can also run as an OpenID Connect provider. This is an advanced setup and uses `xis-idp-server`, not
`xis-idp-client`. Use it when one application should own authentication and issue tokens for other applications.

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

The IDP publishes the OpenID Connect discovery document, JWKS, login page, and token endpoint. Client applications then
configure the XIS IDP like any other external OpenID Connect provider by using `ExternalIDPConfig` and the IDP base URL.

The built-in IDP login flow is still a XIS login flow backed by `IDPService`. If the IDP application needs a more complex
interactive process, such as multi-step registration or manual account approval, model that process in the IDP
application and allow credential validation only after the account is ready.

### XIS IDP And Upstream Providers

The current `xis-idp-server` module is an OpenID Connect provider, but it is not a built-in OpenID Connect broker. It
does not automatically show upstream provider links on `/idp/login.html`, redirect to Google or Keycloak from the IDP
login page, handle an upstream callback inside the IDP, and then issue a XIS token for the downstream application.

The external-provider support documented above belongs to applications that use `xis-authentication` together with
`xis-idp-client`. In that setup the application itself redirects to an external OpenID Connect provider and processes
the callback at `/xis/auth/callback/{providerId}`.

XIS as IDP can still be the right place for a later broker design: the IDP could authenticate against one or more
upstream OpenID Connect providers, map the external identity to its own account and approval model, and then issue its
own tokens to client applications. That requires additional IDP-side code for provider selection, upstream callback
handling, state mapping, and user/claim mapping. It is therefore not documented as a ready-to-use feature yet.
