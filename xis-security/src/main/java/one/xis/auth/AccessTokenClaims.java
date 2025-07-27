package one.xis.auth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class AccessTokenClaims implements TokenClaims {

    @SerializedName("aud")
    private String audience;

    @SerializedName("scope")
    private String scope;

    @SerializedName("jti")
    private String jwtId;

    @SerializedName("azp")
    private String authorizedParty;

    @SerializedName("realm_access")
    private RealmAccess realmAccess;

    @SerializedName("resource_access")
    private ResourceAccess resourceAccess;
    
    @SerializedName("username")
    private String username;

    @SerializedName("roles")
    private List<String> roles;


    @Data
    public static class RealmAccess {
        @SerializedName("roles")
        private List<String> roles;
    }

    @Data
    public static class ResourceAccess {
        @SerializedName("account")
        private Account account;

        @Data
        public static class Account {
            @SerializedName("roles")
            private List<String> roles;
        }
    }
}
