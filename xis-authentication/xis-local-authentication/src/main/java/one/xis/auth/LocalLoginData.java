package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@LocalLogin
@NoArgsConstructor
@AllArgsConstructor
class LocalLoginData {
    private String username;
    private String password;
    private String redirectUri;
}
