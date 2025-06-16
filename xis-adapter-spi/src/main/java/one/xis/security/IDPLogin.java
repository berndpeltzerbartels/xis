package one.xis.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IDPLogin {
    private String username;
    private String password;
    private String state;
    private String redirectUrl;
}
