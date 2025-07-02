package one.xis.idp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoImpl implements UserInfo {
    private String userId;
    private String password;
    private Set<String> roles = Set.of();
    private Map<String, Object> claims = Map.of();
}
