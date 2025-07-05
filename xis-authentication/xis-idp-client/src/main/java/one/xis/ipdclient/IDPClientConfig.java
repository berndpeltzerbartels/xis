package one.xis.ipdclient;


import com.nimbusds.jwt.JWTClaimsSet;
import one.xis.ImportInstances;
import one.xis.auth.UserInfoImpl;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;

@ImportInstances
public interface IDPClientConfig {

    String getIdpId();

    String getIdpServerUrl();

    String getClientId();

    String getClientSecret();


    /**
     * Extracts user information from the JWT claims.
     * The default implementation handles standard OIDC claims and a "roles" claim.
     * Override this method for IDPs with custom claim structures (like Keycloak).
     *
     * @param claims The set of claims from the validated JWT.
     * @return UserInfo populated from the claims.
     */
    default UserInfoImpl extractUserInfo(JWTClaimsSet claims) throws ParseException {
        var roles = claims.getStringListClaim("roles");

        return UserInfoImpl.builder()
                .userId(claims.getSubject())
                .email(claims.getStringClaim("email"))
                .roles(roles != null ? new HashSet<>(roles) : Collections.emptySet())
                .build();
    }
}
