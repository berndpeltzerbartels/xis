package one.xis.ajax;

import lombok.Value;

import java.util.Collection;


@Value
public class ClientAttributes {
    String clientId;
    String userId;
    Collection<String> roles;

}
