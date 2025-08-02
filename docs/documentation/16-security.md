## 16. Security

This chapter introduces the security model of XIS, which is based on JSON Web Tokens (JWT). All apiTokens
mechanisms, including Keycloak and future community logins, are unified under a common token format used internally by
XIS.

---

### 13.1 Concept

XIS uses a custom JWT token for all apiTokens flows. Even when using third-party identity providers such as
Keycloak or future community-based logins (e.g. Google, Facebook), XIS **does not forward their tokens directly**.
Instead, it issues its own signed token upon successful login.

This design follows a widely recommended **best practice**: do not expose tokens from external providers across systems,
but issue and verify your own tokens internally. This allows you to:

- control the lifetime of sessions
- avoid tight coupling with identity provider formats
- add custom claims

---

### 13.2 Built-in Login Page

_(To be implemented)_

---

### 13.3 The `@UserId` Annotation

The `@UserId` annotation allows you to inject the current user's ID into any controller method. The user ID is extracted
from the JWT token and injected automatically.

```java

@Action
public void save(@UserId String userId, @FormData ProductForm form) {
    // Use userId to associate the product with the current user
}
```

---

### 13.4 Using Keycloak

_(To be implemented)_

---

### 13.5 Community Login (Google, Facebook, etc.)

_(To be implemented)_

---

### TODOs

- Implement all mentioned security modules
- Add support for `@UserId` injection
- Add configuration guides for Keycloak
- Implement community login strategies (Google, Facebook, etc.)

[← Custom JavaScript Functions and CSS](15-custom-assets.md) | [Push with Socket.IO →](17-push-socketio.md)