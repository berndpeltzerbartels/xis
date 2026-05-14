package one.xis.auth;

import java.util.Set;

public interface UserInfo {
    String getUserId();

    Set<String> getRoles();

    
    String getName();

    String getEmail();

    boolean isEmailVerified();

    String getPreferredUsername();

    String getGivenName();

    String getFamilyName();

    String getLocale();

    String getPictureUrl();

    void setUserId(String userId);

    void setRoles(Set<String> roles);

    void setName(String name);

    void setEmail(String email);

    void setEmailVerified(boolean emailVerified);

    void setPreferredUsername(String preferredUsername);

    void setGivenName(String givenName);

    void setFamilyName(String familyName);

    void setLocale(String locale);

    void setPictureUrl(String pictureUrl);

}
