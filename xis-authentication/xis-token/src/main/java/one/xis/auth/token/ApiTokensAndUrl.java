package one.xis.auth.token;


import lombok.Data;

@Data
public class ApiTokensAndUrl {
    private ApiTokens apiTokens;
    private String url;
}
