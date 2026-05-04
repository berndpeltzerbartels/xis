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

With exactly one external provider and no local user service, XIS redirects directly to that provider. With multiple
providers, or with local login plus providers, XIS opens `/login.html` so the user can choose.

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

The redirect behavior is:

- local login only: `/login.html`
- local login and one external provider: `/login.html`
- local login and multiple external providers: `/login.html`
- one external provider and no local login: direct redirect to that provider
- multiple external providers and no local login: `/login.html`

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

XIS as an IDP is a separate advanced setup and uses additional dependencies. Treat it as a different topic from normal
application authentication.
