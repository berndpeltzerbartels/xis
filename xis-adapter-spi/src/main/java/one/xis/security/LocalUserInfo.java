package one.xis.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalUserInfo {
    private String userId;
    private String password;
    private Set<String> roles = Set.of();
    private Map<String, Object> claims = Map.of();
}
