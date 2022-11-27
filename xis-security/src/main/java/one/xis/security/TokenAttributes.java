package one.xis.security;

import lombok.Value;

import java.util.Collection;


@Value
public class TokenAttributes {
    String userId;
    Collection<String> roles;
}
