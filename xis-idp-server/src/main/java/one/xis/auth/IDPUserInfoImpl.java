package one.xis.auth;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IDPUserInfoImpl implements IDPUserInfo {
    private String userId;
    private String clientId;
}
