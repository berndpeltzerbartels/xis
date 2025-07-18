package one.xis.auth.idp;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IDPUserInfoImpl implements IDPUserInfo {
    private String userId;
    private String clientId;
    private String email;
    private Set<String> roles = new HashSet<>();
    private Map<String, Object> claims = new HashMap<>();
}
