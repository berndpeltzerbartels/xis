package one.xis.auth.idp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@IDPLoginData
public class IDPServerLogin {
    private String username;
    private String password;
    private String state;
    private String redirectUri;
}
