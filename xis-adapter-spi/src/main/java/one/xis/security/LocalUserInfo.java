package one.xis.security;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class LocalUserInfo {
    private String userId;
    private String password;
    private Set<String> roles = Set.of();
    private Map<String, Object> claims = Map.of();
}
