package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IDPClientInfoImpl implements IDPClientInfo {
    private String clientId;
    private String clientSecret;
    private Set<String> permittedRedirectUrls;
}
