package one.xis.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginResponseTest {

    @Test
    void encodesStateAndCodeForCallbackUrl() {
        var response = new LoginResponse("abc+/=", "code+/=");

        assertEquals("/xis/auth/callback/local?state=abc%2B%2F%3D&code=code%2B%2F%3D", response.getRedirectUrl());
    }
}
