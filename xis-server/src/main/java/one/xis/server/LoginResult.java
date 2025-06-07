package one.xis.server;

import lombok.Data;

@Data
class LoginResult {
    private final String code;
    private final String state;
    private final String nextUrl;
}
