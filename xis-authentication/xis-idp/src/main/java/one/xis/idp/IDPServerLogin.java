package one.xis.idp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IDPServerLogin {
    private String username;
    private String password;
    private String state;
}
