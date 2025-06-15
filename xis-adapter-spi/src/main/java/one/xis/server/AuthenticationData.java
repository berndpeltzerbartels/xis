package one.xis.server;


import lombok.Data;

@Data
public class AuthenticationData {
    private ApiTokens apiTokens;
    private String url;
}
