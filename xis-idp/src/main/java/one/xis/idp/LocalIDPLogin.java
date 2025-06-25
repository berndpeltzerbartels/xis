package one.xis.idp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalIDPLogin {
    private String username;
    private String password;
    private String state;
    private String redirectUrl;
}
