package one.xis.security;

import lombok.Data;

@Data
public class Login {
    private String username;
    private String password;
    private String state;
}
