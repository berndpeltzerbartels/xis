package one.xis.idp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IDPUserInfoImpl implements IDPUserInfo {
    private String userId;
    private String clientId;
    private String password;
    private String email;
    private Set<String> roles;
    private Map<String, Object> claims;

}
