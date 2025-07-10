package one.xis.idp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IDPClientInfoImpl implements IDPClientInfo {
    private String clientId;
    private String clientSecret;
    private Collection<String> permittedRedirectUrls;
}
