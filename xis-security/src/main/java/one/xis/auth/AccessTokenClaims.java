package one.xis.auth;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.Collection;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccessTokenClaims extends TokenClaims {

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
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RealmAccess {
        @SerializedName("roles")
        private Collection<String> roles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceAccess {
        @SerializedName("account")
        private Account account;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Account {
            @SerializedName("roles")
            private Collection<String> roles;
        }
    }

}
