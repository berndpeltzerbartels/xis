package one.xis.auth;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.Collection;
import java.util.Set;

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

    public void setRoles(Collection<String> roles) {
        if (realmAccess == null) {
            realmAccess = new RealmAccess();
        }
        realmAccess.setRoles(roles);
        if (resourceAccess == null) {
            resourceAccess = new ResourceAccess();
        }
        if (resourceAccess.account == null) {
            resourceAccess.account = new ResourceAccess.Account();
        }
        resourceAccess.account.setRoles(roles);
    }

    public Set<String> getRoles() {
        if (realmAccess != null) {
            return Set.copyOf(realmAccess.getRoles());
        }
        if (resourceAccess != null && resourceAccess.account != null) {
            return Set.copyOf(resourceAccess.account.getRoles());
        }
        return Set.of();
    }


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
