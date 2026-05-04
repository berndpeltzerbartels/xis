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

If the application has exactly one external IDP and no custom `UserInfoService`, XIS redirects directly to that IDP login
URL. If the application has local authentication or multiple external IDPs, XIS uses `/login.html` so the user can choose
or use the local login form.

XIS as an IDP is a separate advanced setup and uses additional dependencies. Treat it as a different topic from normal
application authentication.
