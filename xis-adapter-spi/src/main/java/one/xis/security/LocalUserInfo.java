package one.xis.security;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class LocalUserInfo {
    private String userId;
    private Set<String> roles;
    private Map<String, Object> claims;
}
