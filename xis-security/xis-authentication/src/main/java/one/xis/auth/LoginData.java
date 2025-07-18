package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Login
@NoArgsConstructor
@AllArgsConstructor
class LoginData {
    private String username;
    private String password;
    private String state;
}
